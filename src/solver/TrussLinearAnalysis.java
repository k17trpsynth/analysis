package solver;


import structure.Member;
import data.StructureDataset;
import matrix.TrussStiffnessMatrix;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;

public class TrussLinearAnalysis {

    private StructureDataset structureDataset;
    private int size;
    private int freeDispSize;
    private HashMap<Integer, boolean[]> confinementMap;
    private ArrayList<Integer> nodeOrder;
    private ArrayList<Integer> elementOrder;
    private DMatrixRMaj f;
    private DMatrixRMaj d;
    private DMatrixSparseCSC K;

    public TrussLinearAnalysis(StructureDataset input) {
        this(input, 1);
    }

    TrussLinearAnalysis(StructureDataset input, double delta) {
        this.structureDataset = input;
        this.size = input.getSize();
        this.freeDispSize = input.getFreeDispSize();
        this.confinementMap = input.getConfinements();
        this.nodeOrder = new ArrayList<>();
        this.elementOrder = new ArrayList<>();

        input.getNodes().keySet().forEach((nodeNum) -> {
            this.nodeOrder.add(nodeNum);
        });

        this.f = new DMatrixRMaj(this.freeDispSize, 1);
        this.d = new DMatrixRMaj(this.freeDispSize, 1);
        input.getElements().keySet().forEach((elementNum) -> {
            this.elementOrder.add(elementNum);
        });
        this.K = new DMatrixSparseCSC(this.freeDispSize, this.freeDispSize);
        this.createStiffnessMatrix();
        this.setForce(delta);
    }

    public void solve() {
        LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver = LinearSolverFactory_DSCC.qr(FillReducing.NONE);
        //System.out.println("f = ");
        //this.f.print();
        //System.out.println("K = ");
        //this.K.print();
        //DMatrixRMaj KInv = new DMatrixRMaj(this.freeDispSize, this.freeDispSize);
        //CommonOps_DSCC.invert(this.K, KInv);
        //System.out.println("KInv = ");
        //KInv.print();
        solver.setA(this.K);
        solver.solve(this.f, this.d);
        this.updateState();
    }

    public StructureDataset exportDataset() {
        return this.structureDataset;
    }

    public void updateState() {
        this.updateAxialForce();
        this.updateNode();
    }

    public void updateAxialForce() {
        double[] dAll = new double[3 * this.size];
        int count = 0;
        for (int i = 0; i < this.nodeOrder.size(); i++) {
            boolean[] conf = this.confinementMap.get(this.nodeOrder.get(i));
            for (int j = 0; j < 3; j++) {
                if (Objects.nonNull(conf) && !conf[j]) {
                    dAll[3 * i + j] = 0;
                } else {
                    dAll[3 * i + j] = this.d.get(count);
                    count++;
                }
            }
        }

        this.structureDataset.getElements().keySet().forEach((elementNum) -> {
            Member mem = this.structureDataset.getElements().get(elementNum);
            double l = (mem.getNodeJ()[0] - mem.getNodeI()[0]) / mem.getL();
            double m = (mem.getNodeJ()[1] - mem.getNodeI()[1]) / mem.getL();
            double n = (mem.getNodeJ()[2] - mem.getNodeI()[2]) / mem.getL();
            double dL = 0;
            dL -= l * dAll[3 * this.nodeOrder.indexOf(mem.getIndexI())];
            dL -= m * dAll[3 * this.nodeOrder.indexOf(mem.getIndexI()) + 1];
            dL -= n * dAll[3 * this.nodeOrder.indexOf(mem.getIndexI()) + 2];
            dL += l * dAll[3 * this.nodeOrder.indexOf(mem.getIndexJ())];
            dL += m * dAll[3 * this.nodeOrder.indexOf(mem.getIndexJ()) + 1];
            dL += n * dAll[3 * this.nodeOrder.indexOf(mem.getIndexJ()) + 2];
            double axialForce = this.structureDataset.getAxialForce(elementNum);
            double dN = mem.getE(axialForce / mem.getA()) * mem.getA() / mem.getL() * dL;
            this.structureDataset.addAxialForce(elementNum, dN);
        });
        //System.out.println("n = [");
        //this.n.keySet().forEach((elementNum) -> {
        //    System.out.println(this.n.get(elementNum) * 1e-3);
        //});
        //System.out.println("]");
    }

    public void updateNode() {
        ArrayList<int[]> dispArray = new ArrayList<>();
        for (int i = 0; i < this.nodeOrder.size(); i++) {
            int nodeNum = this.nodeOrder.get(i);
            System.out.println("i = " + i + ", nodeNum = " + nodeNum);
            for (int j = 0; j < 3; j++) {
                if (this.confinementMap.containsKey(nodeNum)) {
                    if (this.confinementMap.get(nodeNum)[j]) {
                        dispArray.add(new int[]{nodeNum, j});
                    }
                } else {
                    dispArray.add(new int[]{nodeNum, j});
                }
            }
        }
        for (int i = 0; i < this.freeDispSize; i++) {
            int nodeNum = dispArray.get(i)[0];
            int direction = dispArray.get(i)[1];
            switch (direction) {
                case 0:
                    this.structureDataset.moveNode(nodeNum, this.d.get(i), 0, 0);
                    break;
                case 1:
                    this.structureDataset.moveNode(nodeNum, 0, this.d.get(i), 0);
                    break;
                case 2:
                    this.structureDataset.moveNode(nodeNum, 0, 0, this.d.get(i));
                    break;
                default:
                    break;
            }
        }
    }

    public void setForce(double delta) {
        DMatrixRMaj fAll = new DMatrixRMaj(3 * this.size, 1);

        if (Objects.nonNull(this.structureDataset.getConcentratedLoads())) {

            this.structureDataset.getConcentratedLoads().keySet().forEach((nodeNum) -> {
                double[] concentratedLoad = this.structureDataset.getConcentratedLoads().get(nodeNum);
                for (int i = 0; i < 3; i++) {
                    fAll.set(3 * this.nodeOrder.indexOf(nodeNum) + i, delta * concentratedLoad[i]);
                }
            });
        }

        int count = 0;
        for (int nodeNum : this.nodeOrder) {
            for (int i = 0; i < 3; i++) {
                if (this.confinementMap.containsKey(nodeNum) && !this.confinementMap.get(nodeNum)[i]) {
                } else {
                    f.set(count, fAll.get(3 * this.nodeOrder.indexOf(nodeNum) + i));
                    count++;
                }
            }
        }
    }

    public void createStiffnessMatrix() {
        DMatrixRMaj KAll = new DMatrixRMaj(3 * this.size, 3 * this.size);
        this.structureDataset.getElements().keySet().forEach((Integer elementNum) -> {
            Member mem = this.structureDataset.getElements().get(elementNum);
            double sigma = this.structureDataset.getAxialForce(elementNum) / mem.getA();
            TrussStiffnessMatrix kiGlobal = new TrussStiffnessMatrix(mem, sigma);
            DMatrixRMaj Ki = new DMatrixRMaj(3 * this.size, 3 * this.size);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    Ki.set(3 * this.nodeOrder.indexOf(mem.getIndexI()) + i, 3 * this.nodeOrder.indexOf(mem.getIndexI()) + j, kiGlobal.get(i, j));
                    Ki.set(3 * this.nodeOrder.indexOf(mem.getIndexI()) + i, 3 * this.nodeOrder.indexOf(mem.getIndexJ()) + j, kiGlobal.get(i, 3 + j));
                    Ki.set(3 * this.nodeOrder.indexOf(mem.getIndexJ()) + i, 3 * this.nodeOrder.indexOf(mem.getIndexI()) + j, kiGlobal.get(3 + i, j));
                    Ki.set(3 * this.nodeOrder.indexOf(mem.getIndexJ()) + i, 3 * this.nodeOrder.indexOf(mem.getIndexJ()) + j, kiGlobal.get(3 + i, 3 + j));
                }
            }
            CommonOps_DDRM.addEquals(KAll, Ki);
        });

        int countRow = 0;
        int countColumn = 0;
        for (int i = 0; i < 3 * this.size; i++) {
            for (int j = 0; j < 3 * this.size; j++) {
                int nodeI = this.nodeOrder.get(i / 3);
                int nodeJ = this.nodeOrder.get(j / 3);
                if ((this.confinementMap.containsKey(nodeI) && !this.confinementMap.get(nodeI)[i % 3])
                        || (this.confinementMap.containsKey(nodeJ) && !this.confinementMap.get(nodeJ)[j % 3])) {
                } else {
                    this.K.set(countRow, countColumn, KAll.get(i, j));
                    countColumn++;
                    if (countColumn >= this.freeDispSize) {
                        countColumn = 0;
                        countRow++;
                    }
                }
            }
        }
    }
}

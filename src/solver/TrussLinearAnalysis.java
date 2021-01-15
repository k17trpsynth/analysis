package solver;

import structure.Member;
import data.StructureDataset;
import matrix.TrussStiffnessMatrix;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import matrix.DMatrixGeneral;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

public class TrussLinearAnalysis {

    private StructureDataset structureDataset;
    private double delta;
    private int size;
    private int freeDispSize;
    private HashMap<Integer, boolean[]> confinementMap;
    private ArrayList<Integer> nodeOrder;
    private ArrayList<Integer> elementOrder;
    private DMatrixRMaj f;
    private DMatrixRMaj d;
    private DMatrixGeneral K;

    public TrussLinearAnalysis(StructureDataset input, boolean isInEquilibrium) {
        this(input, 1, isInEquilibrium);
    }

    public TrussLinearAnalysis(StructureDataset input, double delta, boolean isInEquilibrium) {
        this.structureDataset = input;
        this.delta = delta;
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
        this.K = new DMatrixGeneral(this.freeDispSize, this.freeDispSize);
        this.createStiffnessMatrix();
        if (isInEquilibrium) {
            this.setForce();
        } else {
            this.setDisequilibriumForce();
        }
    }

    public void solve() {
        /*
        System.out.println("f = ");
        this.f.print();
        System.out.println("K = ");
        this.K.print();
        DMatrixRMaj KInv = new DMatrixRMaj(this.freeDispSize, this.freeDispSize);
        CommonOps_DDRM.invert(this.K, KInv);
        System.out.println("K-1 = ");
        KInv.print();
         */
        DMatrixGeneral KPlus = this.K.generalizedInverse();
        /*
        System.out.println("K+ = ");
        KPlus.print();
         */
 /*
        solver.setA(this.K);
        solver.solve(this.f, this.d);
         */
        //this.stretchNodes();
        CommonOps_DDRM.mult(KPlus, this.f, this.d);
        this.updateState();
    }

    public StructureDataset exportDataset() {
        return this.structureDataset;
    }

    /*
    public void stretchNodes() {
        System.out.println("sretch nodes.");
        while (true) {
            double alpha = 0.01;
            DMatrixGeneral KPlus = K.generalizedInverse();
            DMatrixRMaj KKPlus = CommonOps_DDRM.mult(K, KPlus, null);
            DMatrixRMaj IKKPlus = CommonOps_DDRM.add(-1, CommonOps_DDRM.identity(this.freeDispSize), KKPlus, null);
            DMatrixRMaj f2 = CommonOps_DDRM.mult(IKKPlus, this.f, null);
            //System.out.println("f2 = ");
            //f2.print();
            boolean isStretchable = (CommonOps_DDRM.elementSumAbs(f2) > 1);
            System.out.println("is stretchable = " + isStretchable);
            if (isStretchable) {
                CommonOps_DDRM.scale(alpha, f2);
                this.d = f2;
                this.updateNodes();
                this.createStiffnessMatrix();
            } else {
                break;
            }
        }
        while (!this.structureDataset.isInEquilibrium()) {
            System.out.println("not in equilibrium.");
            DMatrixGeneral KPlus = K.generalizedInverse();
            CommonOps_DDRM.mult(KPlus, this.f, this.d);
            this.updateNodes();
        }
    }
     */
    public void updateState() {
        this.updateLoad();
        this.updateAxialForces();
        this.updateNodes();
    }

    public void updateLoad() {
        System.out.println("Update load.");
        for (int nodeNum : this.structureDataset.getTotalLoads().keySet()) {
            double[] totalLoad = this.structureDataset.getTotalLoads().get(nodeNum);
            double dnx = this.delta * totalLoad[0];
            double dny = this.delta * totalLoad[1];
            double dnz = this.delta * totalLoad[2];
            this.structureDataset.addConcentratedLoad(nodeNum, dnx, dny, dnz);
        }
    }

    public void updateAxialForces() {
        System.out.println("Update axial forces.");
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
            double[] cos = new double[3];
            for (int i = 0; i < 3; i++) {
                cos[i] = (this.structureDataset.getNodes().get(mem.getIndexJ())[i] - this.structureDataset.getNodes().get(mem.getIndexI())[i]) / mem.getL();
            }
            double dL = 0;
            for (int i = 0; i < 3; i++) {
                dL -= cos[i] * dAll[3 * this.nodeOrder.indexOf(mem.getIndexI()) + i];
                dL += cos[i] * dAll[3 * this.nodeOrder.indexOf(mem.getIndexJ()) + i];
            }
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

    public void updateNodes() {
        System.out.println("Update nodes.");
        ArrayList<int[]> dispArray = new ArrayList<>();
        for (int i = 0; i < this.nodeOrder.size(); i++) {
            int nodeNum = this.nodeOrder.get(i);
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

    public void setForce() {
        DMatrixRMaj fAll = new DMatrixRMaj(3 * this.size, 1);

        if (Objects.nonNull(this.structureDataset.getTotalLoads())) {
            this.structureDataset.getTotalLoads().keySet().forEach((nodeNum) -> {
                double[] totalLoad = this.structureDataset.getTotalLoads().get(nodeNum);
                for (int i = 0; i < 3; i++) {
                    fAll.set(3 * this.nodeOrder.indexOf(nodeNum) + i, this.delta * totalLoad[i]);
                }
            });
        }

        int count = 0;
        for (int nodeNum : this.nodeOrder) {
            for (int i = 0; i < 3; i++) {
                if (this.confinementMap.containsKey(nodeNum) && !this.confinementMap.get(nodeNum)[i]) {
                } else {
                    this.f.set(count, fAll.get(3 * this.nodeOrder.indexOf(nodeNum) + i));
                    count++;
                }
            }
        }
    }

    public void setDisequilibriumForce() {
        DMatrixRMaj fAll = new DMatrixRMaj(3 * this.size, 1);
        HashMap<Integer, double[]> disequilibriumForce = this.structureDataset.calculateDisequilibriumForce();

        if (Objects.nonNull(disequilibriumForce)) {
            disequilibriumForce.keySet().forEach((nodeNum) -> {
                double[] disequilibriumF = disequilibriumForce.get(nodeNum);
                for (int i = 0; i < 3; i++) {
                    fAll.set(3 * this.nodeOrder.indexOf(nodeNum) + i, this.delta * disequilibriumF[i]);
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
            TrussStiffnessMatrix kiGlobal = new TrussStiffnessMatrix(mem, this.structureDataset.getNodes(), sigma);
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


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

    private InputDataset input;
    private OutputDataset output;
    private int size;
    private int freeDispSize;
    private ArrayList<Integer> nodeOrder;
    private DMatrixRMaj f;
    private DMatrixRMaj d;
    private HashMap<Integer, Double> n;
    private DMatrixSparseCSC K;

    TrussLinearAnalysis(InputDataset input) {
        this.input = input;
        this.output = new OutputDataset(input);
        this.size = input.getSize();
        this.freeDispSize = input.getFreeDispSize();
        this.nodeOrder = new ArrayList<>();

        input.getNodes().keySet().forEach((nodeNum) -> {
            this.nodeOrder.add(nodeNum);
        });

        this.f = new DMatrixRMaj(this.freeDispSize, 1);
        this.d = new DMatrixRMaj(this.freeDispSize, 1);
        this.n = new HashMap<>();
        this.input.getElements().keySet().forEach((elementNum) -> {
            this.n.put(elementNum, this.input.getElements().get(elementNum).getN0());
        });
        this.K = new DMatrixSparseCSC(this.freeDispSize, this.freeDispSize);
    }

    public void solve() {
        this.createStiffnessMatrix();
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
        this.updateAxialForce();
    }

    public DMatrixRMaj getDisplacements() {
        return this.d;
    }

    public HashMap<Integer, Double> getAxialForces() {
        return this.n;
    }

    public void updateAxialForce() {
        double[] dAll = new double[3 * this.size];
        int count = 0;
        for (int i = 0; i < nodeOrder.size(); i++) {
            boolean[] conf = this.input.getConfinements().get(nodeOrder.get(i));
            for (int j = 0; j < 3; j++) {
                if (Objects.nonNull(conf) && !conf[j]) {
                    dAll[3 * i + j] = 0;
                } else {
                    dAll[3 * i + j] = this.d.get(count);
                    count++;
                }
            }
        }

        this.input.getElements().keySet().forEach((elementNum) -> {
            Member mem = this.input.getElements().get(elementNum);
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
            double dN = mem.getE(this.n.get(elementNum) / mem.getA()) * mem.getA() / mem.getL() * dL;
            this.n.replace(elementNum, this.n.get(elementNum) + dN);
        });
        //System.out.println("n = [");
        //this.n.keySet().forEach((elementNum) -> {
        //    System.out.println(this.n.get(elementNum) * 1e-3);
        //});
        //System.out.println("]");
    }

    public OutputDataset export() {
        this.output.setDisplacements(this.d);
        this.output.setForces(this.n);
        return this.output;
    }

    public void setForce() {
        this.setForce(1);
    }

    public void setForce(double delta) {
        DMatrixRMaj fAll = new DMatrixRMaj(3 * this.size, 1);

        if (Objects.nonNull(this.input.getConcentratedLoads())) {
            DMatrixRMaj fConcentratedGlobal = new DMatrixRMaj(3 * this.size, 1);

            this.input.getConcentratedLoads().keySet().forEach((nodeNum) -> {
                double[] concentratedLoad = this.input.getConcentratedLoads().get(nodeNum);
                for (int i = 0; i < 3; i++) {
                    fConcentratedGlobal.set(3 * this.nodeOrder.indexOf(nodeNum) + i, delta * concentratedLoad[i]);
                }
            });

            CommonOps_DDRM.addEquals(fAll, fConcentratedGlobal);
        }

        if (Objects.nonNull(this.input.getGravityLoad())) {
            DMatrixRMaj gravityGlobal = new DMatrixRMaj(this.input.getGravityLoad());

            this.input.getElements().keySet().forEach((Integer elementNum) -> {
                Member mem = this.input.getElements().get(elementNum);
                double l = (mem.getNodeJ()[0] - mem.getNodeI()[0]) / mem.getL();
                double m = (mem.getNodeJ()[1] - mem.getNodeI()[1]) / mem.getL();
                double n = (mem.getNodeJ()[2] - mem.getNodeI()[2]) / mem.getL();
                SmallTransformationMatrix tInvT = new SmallTransformationMatrix(l, m, n);
                CommonOps_DDRM.transpose(tInvT);
                CommonOps_DDRM.invert(tInvT);
                DMatrixRMaj gravityLocal = new DMatrixRMaj(3, 1);
                CommonOps_DDRM.mult(tInvT, gravityGlobal, gravityLocal);
                DMatrixRMaj fGravityLocal = new DMatrixRMaj(3, 1);
                final double g = 9.8;

                fGravityLocal.set(0, delta * mem.getRho() * 1e-6 * mem.getA() * g * gravityLocal.get(0) * mem.getL() / 2);
                fGravityLocal.set(1, delta * mem.getRho() * 1e-6 * mem.getA() * g * gravityLocal.get(1) * mem.getL() / 2);
                fGravityLocal.set(2, delta * mem.getRho() * 1e-6 * mem.getA() * g * gravityLocal.get(2) * mem.getL() / 2);

                DMatrixRMaj fGravityGlobalElement = new DMatrixRMaj(3, 1);
                SmallTransformationMatrix tT = new SmallTransformationMatrix(l, m, n);
                CommonOps_DDRM.transpose(tT);
                CommonOps_DDRM.mult(tT, fGravityLocal, fGravityGlobalElement);

                DMatrixRMaj fGravityGlobal = new DMatrixRMaj(3 * this.size, 1);
                for (int i = 0; i < 3; i++) {
                    fGravityGlobal.set(3 * this.nodeOrder.indexOf(mem.getIndexI()) + i, fGravityGlobalElement.get(i));
                    fGravityGlobal.set(3 * this.nodeOrder.indexOf(mem.getIndexJ()) + i, fGravityGlobalElement.get(i));
                }
                CommonOps_DDRM.addEquals(fAll, fGravityGlobal);
            });
        }

        int count = 0;
        for (int nodeNum : this.nodeOrder) {
            for (int i = 0; i < 3; i++) {
                if (this.input.getConfinements().containsKey(nodeNum) && !this.input.getConfinements().get(nodeNum)[i]) {
                } else {
                    f.set(count, fAll.get(3 * this.nodeOrder.indexOf(nodeNum) + i));
                    count++;
                }
            }
        }
    }

    public void createStiffnessMatrix() {
        DMatrixRMaj KAll = new DMatrixRMaj(3 * this.size, 3 * this.size);
        this.input.getElements().keySet().forEach((Integer elementNum) -> {
            Member mem = this.input.getElements().get(elementNum);
            double sigma = this.n.get(elementNum) / mem.getA();
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
                if ((this.input.getConfinements().containsKey(this.nodeOrder.get(i / 3)) && !this.input.getConfinements().get(this.nodeOrder.get(i / 3))[i % 3])
                        || (this.input.getConfinements().containsKey(this.nodeOrder.get(j / 3)) && !this.input.getConfinements().get(this.nodeOrder.get(j / 3))[j % 3])) {
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

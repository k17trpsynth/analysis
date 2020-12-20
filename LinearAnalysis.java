
import java.util.ArrayList;
import java.util.Objects;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;

public class LinearAnalysis {

    private InputDataset input;
    private OutputDataset output;
    private int size;
    private int freeDispSize;
    private ArrayList<Integer> nodeOrder;
    private DMatrixRMaj f;
    private DMatrixRMaj d;
    private DMatrixSparseCSC K;

    LinearAnalysis(InputDataset input) {
        this.input = input;
        this.output = new OutputDataset(input);
        this.size = input.getSize();
        this.freeDispSize = 6 * this.size;

        for (int nodeNum : input.getConfinements().keySet()) {
            for (int i = 0; i < 6; i++) {
                if (!input.getConfinements().get(nodeNum)[i]) {
                    this.freeDispSize--;
                }
            }
        }

        this.nodeOrder = new ArrayList<>();

        for (int nodeNum : input.getNodes().keySet()) {
            this.nodeOrder.add(nodeNum);
        }

        this.f = new DMatrixRMaj(this.freeDispSize, 1);
        this.d = new DMatrixRMaj(this.freeDispSize, 1);
        this.K = new DMatrixSparseCSC(this.freeDispSize, this.freeDispSize);
    }

    public void solve() {
        this.setForce();
        this.createStiffnessMatrix();
        LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver = LinearSolverFactory_DSCC.qr(FillReducing.NONE);
        System.out.println("f = ");
        this.f.print();
        System.out.println("K = ");
        this.K.print();
        DMatrixRMaj KInv = new DMatrixRMaj(this.freeDispSize, this.freeDispSize);
        CommonOps_DSCC.invert(this.K, KInv);
        System.out.println("KInv = ");
        KInv.print();
        solver.setA(this.K);
        solver.solve(this.f, this.d);
    }

    public OutputDataset export() {
        this.output.setDisplacements(this.d);
        return this.output;
    }

    public void setForce() {
        DMatrixRMaj fAll = new DMatrixRMaj(6 * this.size, 1);

        if (Objects.nonNull(this.input.getConcentratedLoads())) {
            DMatrixRMaj fConcentratedGlobal = new DMatrixRMaj(6 * this.size, 1);

            for (Integer nodeNum : this.input.getConcentratedLoads().keySet()) {
                double[] concentratedLoad = this.input.getConcentratedLoads().get(nodeNum);
                for (int i = 0; i < 3; i++) {
                    fConcentratedGlobal.set(6 * this.nodeOrder.indexOf(nodeNum) + i, concentratedLoad[i]);
                }
            }

            CommonOps_DDRM.addEquals(fAll, fConcentratedGlobal);
        }

        if (Objects.nonNull(this.input.getGravityLoad())) {
            DMatrixRMaj gravityGlobal = new DMatrixRMaj(this.input.getGravityLoad());

            this.input.getElements().keySet().forEach((Integer elementNum) -> {
                Member mem = this.input.getElements().get(elementNum);
                double l = (mem.getNodeI()[0] - mem.getNodeJ()[0]) / mem.getL();
                double m = (mem.getNodeI()[1] - mem.getNodeJ()[1]) / mem.getL();
                double n = (mem.getNodeI()[2] - mem.getNodeJ()[2]) / mem.getL();
                double theta = mem.getTheta();
                SmallTransformationMatrix tInvT = new SmallTransformationMatrix(l, m, n, theta);
                CommonOps_DDRM.transpose(tInvT);
                CommonOps_DDRM.invert(tInvT);
                DMatrixRMaj gravityLocal = new DMatrixRMaj(3, 1);
                CommonOps_DDRM.mult(tInvT, gravityGlobal, gravityLocal);
                DMatrixRMaj fGravityLocal = new DMatrixRMaj(12, 1);

                fGravityLocal.set(0, gravityLocal.get(0) * mem.getL() / 2);
                fGravityLocal.set(1, gravityLocal.get(1) * mem.getL() / 2);
                fGravityLocal.set(2, gravityLocal.get(2) * mem.getL() / 2);
                fGravityLocal.set(3, 0);
                fGravityLocal.set(4, gravityLocal.get(2) * Math.pow(mem.getL(), 2) / 12);
                fGravityLocal.set(5, -gravityLocal.get(1) * Math.pow(mem.getL(), 2) / 12);
                fGravityLocal.set(6, gravityLocal.get(0) * mem.getL() / 2);
                fGravityLocal.set(7, gravityLocal.get(1) * mem.getL() / 2);
                fGravityLocal.set(8, gravityLocal.get(2) * mem.getL() / 2);
                fGravityLocal.set(9, 0);
                fGravityLocal.set(10, -gravityLocal.get(2) * Math.pow(mem.getL(), 2) / 12);
                fGravityLocal.set(11, gravityLocal.get(1) * Math.pow(mem.getL(), 2) / 12);

                DMatrixRMaj fGravityGlobalElement = new DMatrixRMaj(12, 1);
                TransformationMatrix T = new TransformationMatrix(l, m, n, theta);
                DMatrixSparseCSC TT = new DMatrixSparseCSC(12, 12);
                CommonOps_DSCC.transpose(T, TT, null);
                CommonOps_DSCC.mult(TT, fGravityLocal, fGravityGlobalElement);

                DMatrixRMaj fGravityGlobal = new DMatrixRMaj(6 * this.size, 1);
                for (int i = 0; i < 6; i++) {
                    fGravityGlobal.set(6 * this.nodeOrder.indexOf(mem.getIndexI()) + i, fGravityGlobalElement.get(i));
                    fGravityGlobal.set(6 * this.nodeOrder.indexOf(mem.getIndexJ()) + i, fGravityGlobalElement.get(6 + i));
                }
                CommonOps_DDRM.addEquals(fAll, fGravityGlobal);
            });
        }

        int count = 0;
        for (int nodeNum : this.nodeOrder) {
            for (int i = 0; i < 6; i++) {
                if (this.input.getConfinements().containsKey(nodeNum) && !this.input.getConfinements().get(nodeNum)[i]) {
                    continue;
                } else {
                    f.set(count, fAll.get(6 * this.nodeOrder.indexOf(nodeNum) + i));
                    count++;
                }
            }
        }
    }

    public void createStiffnessMatrix() {
        DMatrixRMaj KAll = new DMatrixRMaj(6 * this.size, 6 * this.size);
        this.input.getElements().keySet().forEach((Integer elementNum) -> {
            Member mem = this.input.getElements().get(elementNum);
            ElementStiffnessMatrix ki = new ElementStiffnessMatrix(mem);
            DMatrixSparseCSC kiTmp = new DMatrixSparseCSC(12, 12);
            DMatrixSparseCSC kiGlobal = new DMatrixSparseCSC(12, 12);
            double l = (mem.getNodeJ()[0] - mem.getNodeI()[0]) / mem.getL();
            double m = (mem.getNodeJ()[1] - mem.getNodeI()[1]) / mem.getL();
            double n = (mem.getNodeJ()[2] - mem.getNodeI()[2]) / mem.getL();
            double theta = mem.getTheta();
            TransformationMatrix T = new TransformationMatrix(l, m, n, theta);
            DMatrixSparseCSC TT = new DMatrixSparseCSC(12, 12);
            CommonOps_DSCC.transpose(T, TT, null);
            CommonOps_DSCC.mult(ki, T, kiTmp);
            CommonOps_DSCC.mult(TT, kiTmp, kiGlobal);
            DMatrixRMaj Ki = new DMatrixRMaj(6 * this.size, 6 * this.size);
            for (int i = 0; i < 6; i++) {
                if (i >= 3) {
                    if (!this.input.getConnections().containsKey(elementNum)
                            || (this.input.getConnections().containsKey(elementNum) && !this.input.getConnections().get(elementNum)[0][i - 3])) {
                        for (int j = 0; j < 6; j++) {
                            Ki.set(6 * this.nodeOrder.indexOf(mem.getIndexI()) + i, 6 * this.nodeOrder.indexOf(mem.getIndexI()) + j, kiGlobal.get(i, j));
                            Ki.set(6 * this.nodeOrder.indexOf(mem.getIndexI()) + i, 6 * this.nodeOrder.indexOf(mem.getIndexJ()) + j, kiGlobal.get(i, 6 + j));
                        }
                    }
                    if (!this.input.getConnections().containsKey(elementNum)
                            || (this.input.getConnections().containsKey(elementNum) && !this.input.getConnections().get(elementNum)[1][i - 3])) {
                        for (int j = 0; j < 6; j++) {
                            Ki.set(6 * this.nodeOrder.indexOf(mem.getIndexJ()) + i, 6 * this.nodeOrder.indexOf(mem.getIndexI()) + j, kiGlobal.get(6 + i, j));
                            Ki.set(6 * this.nodeOrder.indexOf(mem.getIndexJ()) + i, 6 * this.nodeOrder.indexOf(mem.getIndexJ()) + j, kiGlobal.get(6 + i, 6 + j));
                        }
                    }
                } else {
                    for (int j = 0; j < 6; j++) {
                        Ki.set(6 * this.nodeOrder.indexOf(mem.getIndexI()) + i, 6 * this.nodeOrder.indexOf(mem.getIndexI()) + j, kiGlobal.get(i, j));
                        Ki.set(6 * this.nodeOrder.indexOf(mem.getIndexI()) + i, 6 * this.nodeOrder.indexOf(mem.getIndexJ()) + j, kiGlobal.get(i, 6 + j));
                        Ki.set(6 * this.nodeOrder.indexOf(mem.getIndexJ()) + i, 6 * this.nodeOrder.indexOf(mem.getIndexI()) + j, kiGlobal.get(6 + i, j));
                        Ki.set(6 * this.nodeOrder.indexOf(mem.getIndexJ()) + i, 6 * this.nodeOrder.indexOf(mem.getIndexJ()) + j, kiGlobal.get(6 + i, 6 + j));
                    }
                }
            }
            CommonOps_DDRM.addEquals(KAll, Ki);
        });

        int countRow = 0;
        int countColumn = 0;
        for (int i = 0; i < 6 * this.size; i++) {
            for (int j = 0; j < 6 * this.size; j++) {
                if ((this.input.getConfinements().containsKey(this.nodeOrder.get(i / 6)) && !this.input.getConfinements().get(this.nodeOrder.get(i / 6))[i % 6])
                        || (this.input.getConfinements().containsKey(this.nodeOrder.get(j / 6)) && !this.input.getConfinements().get(this.nodeOrder.get(j / 6))[j % 6])) {
                    continue;
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

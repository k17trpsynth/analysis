
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

@SuppressWarnings({"serial", "unchecked"})
public class TrussStiffnessMatrix extends DMatrixSparseCSC {

    TrussStiffnessMatrix(Member mem, double sigma) {
        super(6, 6);

        double l = (mem.getNodeJ()[0] - mem.getNodeI()[0]) / mem.getL();
        double m = (mem.getNodeJ()[1] - mem.getNodeI()[1]) / mem.getL();
        double n = (mem.getNodeJ()[2] - mem.getNodeI()[2]) / mem.getL();

        DMatrixSparseCSC t = new DMatrixSparseCSC(6, 1);
        DMatrixSparseCSC tT = new DMatrixSparseCSC(1, 6);
        DMatrixSparseCSC T = new DMatrixSparseCSC(6, 6);
        t.set(0, 0, -l);
        t.set(1, 0, -m);
        t.set(2, 0, -n);
        t.set(3, 0, l);
        t.set(4, 0, m);
        t.set(5, 0, n);
        CommonOps_DSCC.transpose(t, tT, null);
        CommonOps_DSCC.mult(t, tT, T);
        CommonOps_DSCC.add(1, T, 1, this, this, null, null);

        double E = mem.getE(sigma);
        double A = mem.getA();
        double L = mem.getL();

        double kn = E * A / L;

        CommonOps_DSCC.scale(kn, this, this);

        DMatrixSparseCSC Kg = new DMatrixSparseCSC(6, 6);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 3; k++) {
                    if ((i == 0 && j == 0) || (i == 1 && j == 1)) {
                        Kg.set(3 * i + k, 3 * j + k, 1);
                    } else {
                        Kg.set(3 * i + k, 3 * j + k, -1);
                    }
                }
            }
        }

        CommonOps_DSCC.scale(-1, T, T);
        CommonOps_DSCC.add(1, Kg, 1, T, Kg, null, null);

        double N0 = sigma * A;
        double kn0 = N0 / L;

        CommonOps_DSCC.scale(kn0, Kg, Kg);
        CommonOps_DSCC.add(1, this, 1, Kg, this, null, null);
    }
}

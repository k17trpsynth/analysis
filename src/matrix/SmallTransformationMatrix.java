package matrix;


import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

@SuppressWarnings({"serial", "unchecked"})
public class SmallTransformationMatrix extends DMatrixRMaj {

    public SmallTransformationMatrix(double l, double m, double n) {
        this(l, m, n, 0);
    }

    public SmallTransformationMatrix(double l, double m, double n, double theta) {
        super(3, 3);
        DMatrixRMaj t1 = new DMatrixRMaj(3, 3);
        DMatrixRMaj t2 = new DMatrixRMaj(3, 3);

        t1.set(0, 0, 1);
        t1.set(1, 1, Math.cos(theta));
        t1.set(1, 2, Math.sin(theta));
        t1.set(2, 1, -Math.sin(theta));
        t1.set(2, 2, Math.cos(theta));

        if (l == 0 && m == 0) {
            t2.set(0, 2, n);
            t2.set(1, 0, n);
            t2.set(2, 1, 1);
        } else {
            double lmLength = Math.sqrt(Math.pow(l, 2) + Math.pow(m, 2));
            t2.set(0, 0, l);
            t2.set(0, 1, m);
            t2.set(0, 2, n);
            t2.set(1, 0, -m / lmLength);
            t2.set(1, 1, l / lmLength);
            t2.set(2, 0, -l * n / lmLength);
            t2.set(2, 1, -m * n / lmLength);
            t2.set(2, 2, lmLength);
        }

        DMatrixRMaj t = new DMatrixRMaj(3, 3);
        CommonOps_DDRM.mult(t1, t2, t);
        super.set(t);
    }
}

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.simple.SimpleMatrix;

@SuppressWarnings({"serial", "unchecked"})
public class TransformationMatrix extends DMatrixSparseCSC {

    public static void main(String[] args) {
        TransformationMatrix m = new TransformationMatrix(0, 0, 1, Math.PI);
        m.print();
    }

    TransformationMatrix(double l, double m, double n, double theta) {
        super(12, 12);

        SimpleMatrix t1 = new SimpleMatrix(3, 3);
        SimpleMatrix t2 = new SimpleMatrix(3, 3);

        t1.set(0, 0, 1);
        t1.set(1, 1, Math.cos(theta));
        t1.set(1, 2, Math.sin(theta));
        t1.set(2, 1, -Math.sin(theta));
        t1.set(2, 2, Math.cos(theta));

        if (l == 0 || m == 0) {
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

        SimpleMatrix t = t1.mult(t2);

        for (int s = 0; s < 4; s++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    super.set(3 * s + i, 3 * s + j, t.get(i, j));
                }
            }
        }

    }
}

package matrix;

import matrix.SmallTransformationMatrix;
import org.ejml.data.DMatrixSparseCSC;

@SuppressWarnings({"serial", "unchecked"})
public class TransformationMatrix extends DMatrixSparseCSC {

    public static void main(String[] args) {
        TransformationMatrix m = new TransformationMatrix(0, 0, 1, Math.PI);
        m.print();
    }

    public TransformationMatrix(double l, double m, double n, double theta) {
        super(12, 12);
        SmallTransformationMatrix t = new SmallTransformationMatrix(l, m, n, theta);

        for (int s = 0; s < 4; s++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    super.set(3 * s + i, 3 * s + j, t.get(i, j));
                }
            }
        }
    }
}

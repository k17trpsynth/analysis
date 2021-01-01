package matrix;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.SingularOps_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;

public class DMatrixGeneral extends DMatrixRMaj {

    public static void main(String[] args) {
        double[][] array = new double[][]{
            new double[]{1, 3, 2, 1},
            new double[]{2, 2, 2, 0},
            new double[]{3, 1, 2, -1},};
        DMatrixGeneral mat = new DMatrixGeneral(array.length, array[0].length);
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                mat.set(i, j, array[i][j]);
            }
        }
        mat.print();
        mat.generalizedInverse().print();
    }

    public DMatrixGeneral(int rowNum, int colNum) {
        super(rowNum, colNum);
    }

    public DMatrixGeneral generalizedInverse() {
        SingularValueDecomposition_F64<DMatrixRMaj> svd = DecompositionFactory_DDRM.svd(this.getNumRows(), this.getNumCols(), true, true, true);
        if (!svd.decompose(this)) {
            throw new RuntimeException("Decomposition failed.");
        }
        DMatrixRMaj U = svd.getU(null, false);
        DMatrixRMaj W = svd.getW(null);
        DMatrixRMaj V = svd.getV(null, false);

        int rank = SingularOps_DDRM.rank(this);

        DMatrixRMaj U1 = new DMatrixRMaj(this.getNumRows(), rank);
        DMatrixRMaj V1 = new DMatrixRMaj(this.getNumCols(), rank);

        for (int i = 0; i < this.getNumRows(); i++) {
            for (int j = 0; j < rank; j++) {
                U1.set(i, j, U.get(i, j));
            }
        }
        for (int i = 0; i < this.getNumCols(); i++) {
            for (int j = 0; j < rank; j++) {
                V1.set(i, j, V.get(i, j));
            }
        }

        DMatrixGeneral W1Inv = new DMatrixGeneral(rank, rank);
        DMatrixGeneral V1WInv = new DMatrixGeneral(this.getNumCols(), rank);
        DMatrixGeneral inverse = new DMatrixGeneral(this.getNumCols(), this.getNumRows());
        for (int i = 0; i < rank; i++) {
            W1Inv.set(i, i, 1 / W.get(i, i));
        }
        CommonOps_DDRM.mult(V1, W1Inv, V1WInv);
        CommonOps_DDRM.multTransB(V1WInv, U1, inverse);
        return inverse;
    }
}

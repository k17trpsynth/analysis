
import java.util.HashMap;
import org.ejml.data.DMatrixRMaj;

public class OutputDataset {
    private InputDataset input;
    private int size;
    private DMatrixRMaj d;

    OutputDataset(InputDataset input) {
        this.input = input;
        this.size = input.getSize();
        this.d = new DMatrixRMaj(6 * this.size, 1);
    }

    public void setDisplacements(DMatrixRMaj d) {
        this.d = d;
    }

    public DMatrixRMaj getDisplacements() {
        return this.d;
    }

    public HashMap<Integer, double[]> getConcentratedLoads() {
        return this.input.getConcentratedLoads();
    }
}

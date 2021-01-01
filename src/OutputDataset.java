
import java.util.HashMap;
import org.ejml.data.DMatrixRMaj;

public class OutputDataset {
    private StructureDataset input;
    private int size;
    private DMatrixRMaj d;
    private HashMap<Integer, Double> n;

    OutputDataset(StructureDataset input) {
        this.input = input;
        this.size = input.getSize();
        this.d = new DMatrixRMaj(3 * this.size, 1);
        this.n = new HashMap<>();
    }

    public void setDisplacements(DMatrixRMaj d) {
        this.d = d;
    }

    public void setForces(HashMap<Integer, Double> n) {
        this.n = n;
    }

    public DMatrixRMaj getDisplacements() {
        return this.d;
    }

    public HashMap<Integer, Double> getForces() {
        return this.n;
    }

    public HashMap<Integer, double[]> getConcentratedLoads() {
        return this.input.getConcentratedLoads();
    }
}

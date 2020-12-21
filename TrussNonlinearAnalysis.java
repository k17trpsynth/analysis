
import java.util.HashMap;
import java.util.Objects;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;

public class TrussNonlinearAnalysis {

    private InputDataset input;
    private OutputDataset output;
    private int freeDispSize;
    private int stepSize;
    private DMatrixRMaj d;
    private HashMap<Integer, Double> n;
    private TrussLinearAnalysis linearAnalysis;
    private double delta;

    public TrussNonlinearAnalysis(InputDataset input, double delta) {
        this.input = input;
        this.output = new OutputDataset(input);
        this.freeDispSize = input.getFreeDispSize();
        this.stepSize = (int) (1 / delta);
        this.d = new DMatrixRMaj(this.freeDispSize, 1);
        this.n = new HashMap<>();
        this.linearAnalysis = new TrussLinearAnalysis(input);
        if (delta <= 0) {
            System.out.println("Increment parameter should be bigger than 0: " + delta);
            System.exit(1);
        } else if (delta > 1) {
            System.out.println("Increment parameter should be smaller than 1: " + delta);
            System.exit(1);
        }
        this.delta = delta;
    }

    public void setForce() {
        linearAnalysis.setForce(this.delta);
    }

    public void solve() {
        int step = 1;
        while (step <= this.stepSize) {
            System.out.println("step: " + step + " / " + stepSize);
            linearAnalysis.solve();
            CommonOps_DDRM.add(this.d, linearAnalysis.getDisplacements(), this.d);
            step++;
        }
        this.n = linearAnalysis.getAxialForces();
    }

    public OutputDataset export() {
        this.output.setDisplacements(this.d);
        this.output.setForces(this.n);
        return this.output;
    }

}

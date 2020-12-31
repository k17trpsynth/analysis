
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import org.ejml.data.DMatrixRMaj;
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
        String baseDir = new File(".").getAbsoluteFile().getParent();
        String outDir = baseDir + "/out/";
        try {
            FileWriter writer = new FileWriter(new File(outDir + "axial_force.csv"));
            int step = 1;
            while (step <= this.stepSize) {
                System.out.println("step: " + step + " / " + stepSize);
                linearAnalysis.solve();
                CommonOps_DDRM.add(this.d, linearAnalysis.getDisplacements(), this.d);
                for (int elementNum : linearAnalysis.getAxialForces().keySet()) {
                    writer.write(Double.toString((linearAnalysis.getAxialForces().get(elementNum) - this.input.getElements().get(elementNum).getN0()) * 1e-3));
                    writer.write(",");
                }
                writer.write("\n");
                step++;
            }
            writer.close();
            this.n = linearAnalysis.getAxialForces();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public OutputDataset export() {
        this.output.setDisplacements(this.d);
        this.output.setForces(this.n);
        return this.output;
    }

}

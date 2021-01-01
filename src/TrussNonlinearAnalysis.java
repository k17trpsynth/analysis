
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TrussNonlinearAnalysis {

    private StructureDataset structureDataset;
    private final double delta;

    public TrussNonlinearAnalysis(StructureDataset input, double delta) {
        this.structureDataset = input;
        if (delta <= 0) {
            System.out.println("Increment parameter should be bigger than 0: " + delta);
            System.exit(1);
        } else if (delta > 1) {
            System.out.println("Increment parameter should be smaller than 1: " + delta);
            System.exit(1);
        }
        this.delta = delta;
    }

    public void solve() {
        int stepSize = (int) (1 / this.delta);
        String baseDir = new File(".").getAbsoluteFile().getParent();
        String outDir = baseDir + "/out/";
        try {
            FileWriter writer = new FileWriter(new File(outDir + "axial_force.csv"));
            int step = 1;
            while (step <= stepSize) {
                TrussLinearAnalysis linearAnalysis = new TrussLinearAnalysis(structureDataset, this.delta);
                System.out.println("step: " + step + " / " + stepSize);
                linearAnalysis.solve();
                this.structureDataset = linearAnalysis.exportDataset();
                for (int elementNum : this.structureDataset.getElements().keySet()) {
                    writer.write(Double.toString(this.structureDataset.getAxialForce(elementNum) * 1e-3));
                    System.out.println("N = " + this.structureDataset.getAxialForce(elementNum));
                    writer.write(",");
                }
                writer.write("\n");
                step++;
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StructureDataset exportDataset() {
        return this.structureDataset;
    }

}

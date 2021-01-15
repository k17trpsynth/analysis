package solver;

import data.StructureDataset;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

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

    public void solve(FileWriter writer) {
        int stepSize = (int) (1 / this.delta);
        String baseDir = new File(".").getAbsoluteFile().getParent();
        String outDir = baseDir + "/out/";
        try {
            int step = 1;
            FileWriter dispWriter = new FileWriter(new File(outDir + "displacement.csv"));
            while (step <= stepSize) {
                TrussLinearAnalysis linearAnalysis = new TrussLinearAnalysis(this.structureDataset, this.delta, true);
                System.out.println("step: " + step + " / " + stepSize);
                linearAnalysis.solve();
                this.structureDataset = linearAnalysis.exportDataset();
                /*
                System.out.println("is in equilibrium = " + this.structureDataset.isInEquilibrium());
                while (!this.structureDataset.isInEquilibrium()) {
                    TrussLinearAnalysis analysis = new TrussLinearAnalysis(this.structureDataset, 1, false);
                    analysis.solve();
                    this.structureDataset = analysis.exportDataset();
                }
*/
                double norm = 0;
                for (double x : this.structureDataset.getConcentratedLoads().get(11)) {
                    norm += Math.pow(x, 2);
                }
                norm = Math.sqrt(norm);
                if (Objects.nonNull(writer)) {
                    writer.write(Double.toString(norm * 1e-3));
                    writer.write(",");
                    for (int elementNum : this.structureDataset.getElements().keySet()) {
                        writer.write(Double.toString(this.structureDataset.getAxialForce(elementNum) * 1e-3));
                        //System.out.println("N = " + this.structureDataset.getAxialForce(elementNum));
                        writer.write(",");
                    }
                    writer.write("\n");
                }
                for (int nodeNum : this.structureDataset.getNodes().keySet()) {
                    for (int i = 0; i < 3; i++) {
                        double coord = this.structureDataset.getNodes().get(nodeNum)[i];
                        dispWriter.write(coord + ",");
                    }
                }
                dispWriter.write("\n");
                step++;
            }
            dispWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StructureDataset exportDataset() {
        return this.structureDataset;
    }

}

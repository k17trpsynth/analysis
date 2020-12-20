
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestAnalysis {

    public static void main(String[] args) {
        InputDataset input = new InputDataset();
        input.setMaterial("steel", "linear", new double[]{205000}, 0.3, 7.8);
        input.setSection("circle", "circular", new double[]{10});

        input.setNode(1, 0, 0, 0);
        input.setNode(2, 3000, 0, 0);
        input.setNode(3, 3000, 3000, 0);
        input.setNode(4, 0, 3000, 0);
        input.setNode(5, 1500, 1500, 3000);

        input.setElement(1, "steel", "circle", 1, 2, 0, 1000);
        input.setElement(2, "steel", "circle", 2, 3, 0, 0);
        input.setElement(3, "steel", "circle", 3, 4, 0, 0);
        input.setElement(4, "steel", "circle", 4, 1, 0, 0);
        input.setElement(5, "steel", "circle", 5, 1, 0, 0);
        input.setElement(6, "steel", "circle", 5, 2, 0, 0);
        input.setElement(7, "steel", "circle", 5, 3, 0, 0);
        input.setElement(8, "steel", "circle", 5, 4, 0, 0);

        input.setConfinement(1, 1, 1, 1, 0, 0, 0);
        input.setConfinement(2, 1, 1, 1, 0, 0, 0);
        input.setConfinement(3, 0, 0, 1, 0, 0, 0);
        input.setConfinement(4, 0, 0, 1, 0, 0, 0);

        input.addGravityLoad(0, 0, -0.1);

        TrussLinearAnalysis analysis = new TrussLinearAnalysis(input);
        analysis.solve();
        OutputDataset output = analysis.export();
        FileWriter writer = null;
        try {
            String baseDir = "/Users/Kota/Documents/architecture/kawaguchi_lab/white_rhino/";
            writer = new FileWriter(new File(baseDir + "displacement.csv"));

            for (int i = 0; i < input.getFreeDispSize(); i++) {
                writer.write(Double.toString(output.getDisplacements().get(i)) + "\n");
            }
            writer.close();

            writer = new FileWriter(new File(baseDir + "axial_force.csv"));
            for (int elementNum : input.getElements().keySet()) {
                writer.write(Double.toString(output.getForces().get(elementNum)) + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

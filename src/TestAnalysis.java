
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestAnalysis {

    public static void main(String[] args) {
        StructureDataset input = new StructureDataset();
        input.setMaterial("steel", "linear", new double[]{205000}, 0.3, 7.8);
        input.setMaterial("steel_wire", "nonlinear", new double[]{0, 205000, 0, 0}, 0.3, 7.8);
        input.setSection("compression", "circular", new double[]{10});
        input.setSection("tension", "circular", new double[]{1});

        input.setNode(1, 0, 0, 0);
        input.setNode(2, 3000, 0, 0);
        input.setNode(3, 3000, 3000, 0);
        input.setNode(4, 0, 3000, 0);
        input.setNode(5, 1500, 1500, 3000);

        input.setElement(1, "steel_wire", "tension", 1, 2, 0);
        input.setElement(2, "steel_wire", "tension", 2, 3, 0);
        input.setElement(3, "steel_wire", "tension", 3, 4, 0);
        input.setElement(4, "steel_wire", "tension", 4, 1, 0);
        input.setElement(5, "steel", "compression", 5, 1, 0);
        input.setElement(6, "steel", "compression", 5, 2, 0);
        input.setElement(7, "steel", "compression", 5, 3, 0);
        input.setElement(8, "steel", "compression", 5, 4, 0);

        input.setConfinement(1, 1, 1, 1, 0, 0, 0);
        input.setConfinement(2, 1, 1, 1, 0, 0, 0);
        input.setConfinement(3, 0, 0, 1, 0, 0, 0);
        input.setConfinement(4, 0, 0, 1, 0, 0, 0);

        input.addGravityLoad(0, 0, -0.1);

        double delta = 0.1;
        //TrussLinearAnalysis analysis = new TrussLinearAnalysis(input);
        TrussNonlinearAnalysis analysis = new TrussNonlinearAnalysis(input, delta);
        analysis.solve();
        StructureDataset output = analysis.exportDataset();
        FileWriter writer = null;
        try {
            String baseDir = "/Users/Kota/Documents/architecture/kawaguchi_lab/white_rhino/";

            writer = new FileWriter(new File(baseDir + "axial_force.csv"));
            for (int elementNum : input.getElements().keySet()) {
                writer.write(Double.toString(output.getAxialForce(elementNum)) + "\n");
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

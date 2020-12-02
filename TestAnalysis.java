
public class TestAnalysis {

    public static void main(String[] args) {
        InputDataset input = new InputDataset();
        input.setMaterial("steel", 205000, 78000, 7.8);
        input.setSection("circle", "circular", new double[]{100});
        input.setNode(1, 0, 0, 0);
        input.setNode(2, 0, 1000, 0);
        input.setNode(3, 2000, 1000, 0);
        input.setNode(4, 2000, 0, 0);
        input.setElement(1, "steel", "circle", 1, 2, 0);
        input.setElement(2, "steel", "circle", 2, 3, 0);
        input.setElement(3, "steel", "circle", 3, 4, 0);
        input.setConfinement(1, 0, 0, 1, 0, 0, 0);
        input.setConfinement(4, 1, 1, 1, 0, 0, 0);
        input.addConcentratedLoad(2, 1000, 0, 0);
        LinearAnalysis la = new LinearAnalysis(input);
        la.solve();
        OutputDataset output = la.export();
        for (int i = 0; i < 12; i++) {
            System.out.println("displacement = " + output.getDisplacements().get(i));
        }
        System.out.println("load = " + output.getConcentratedLoads().get(2)[2]);
    }
}

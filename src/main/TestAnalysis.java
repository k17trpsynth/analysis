package main;

import data.StructureDataset;
import solver.TrussNonlinearAnalysis;
import solver.TrussLinearAnalysis;

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

        input.setElement(1, "steel", "compression", 1, 2, 0);
        input.setElement(2, "steel", "compression", 2, 3, 0);
        input.setElement(3, "steel", "compression", 3, 4, 0);
        input.setElement(4, "steel", "compression", 4, 1, 0);
        input.setElement(5, "steel", "compression", 5, 1, 0);
        input.setElement(6, "steel", "compression", 5, 2, 0);
        input.setElement(7, "steel", "compression", 5, 3, 0);
        input.setElement(8, "steel", "compression", 5, 4, 0);

        input.setConfinement(1, 1, 1, 1, 0, 0, 0);
        input.setConfinement(2, 1, 1, 1, 0, 0, 0);
        input.setConfinement(3, 0, 0, 1, 0, 0, 0);
        input.setConfinement(4, 0, 0, 1, 0, 0, 0);

        input.addTotalLoad(5, 0, 0, -100 * 1e3);
        input.addGravityLoad(0, 0, -1);

        double delta = 0.1;
        TrussLinearAnalysis analysis = new TrussLinearAnalysis(input, true);
        //TrussNonlinearAnalysis analysis = new TrussNonlinearAnalysis(input, delta);
        analysis.solve();
        StructureDataset output = analysis.exportDataset();
        System.out.println("Load = ");
        for (int nodeNum : output.getConcentratedLoads().keySet()) {
            System.out.print("[");
            for (int i = 0; i < 3; i++) {
                System.out.print(output.getConcentratedLoads().get(nodeNum)[i] + ", ");
            }
            System.out.println("]");
        }
        System.out.println("Axial Force =");
        for (int elementNum : output.getElements().keySet()) {
            System.out.println(output.getAxialForce(elementNum));
        }
        System.out.println("is in equilibrium = " + output.isInEquilibrium());
    }
}

package main;

import data.StructureDataset;
import solver.TrussNonlinearAnalysis;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import solver.TrussLinearAnalysis;

public class WhiteRhino2Analysis {

    public static void main(String[] args) {

        try {
            String baseDir = new File(".").getAbsoluteFile().getParent();
            String dataDir = baseDir + "/data/";
            String outDir = baseDir + "/out/";
            BufferedReader reader = new BufferedReader(new FileReader(new File(dataDir + "coords_measured.csv")));
            HashMap<Integer, ArrayList<Double>> coords = new HashMap<>();
            String line;
            while (Objects.nonNull(line = reader.readLine())) {
                ArrayList<Double> coord = new ArrayList<>();
                int nodeNum = Integer.parseInt(line.split(",")[0].replaceAll(" ", ""));
                coord.add(Double.parseDouble(line.split(",")[1]));
                coord.add(Double.parseDouble(line.split(",")[2]));
                coord.add(Double.parseDouble(line.split(",")[3]));
                coords.put(nodeNum, coord);
                System.out.println(nodeNum + " -> " + coord);
            }
            reader.close();

            reader = new BufferedReader(new FileReader(new File(dataDir + "prestresses_measured.csv")));
            HashMap<String, Double> prestresses = new HashMap<>();
            while (Objects.nonNull(line = reader.readLine())) {
                String code = line.split(",")[0].replaceAll(" ", "");
                double prestress = Double.parseDouble(line.split(",")[1]);
                prestresses.put(code, prestress);
                System.out.println(code + " -> " + prestress);
            }
            reader.close();

            StructureDataset input = new StructureDataset();
            input.setMaterial("steel", "linear", new double[]{205000}, 0.3, 7.8);
            //input.setMaterial("steel_wire", "nonlinear", new double[]{0, 205000, 0, 0}, 0.3, 7.8);
            //input.setMaterial("steel_wire", "nonlinear", new double[]{0, 205000, 0, 0}, 0.3, 7.8);
            input.setMaterial("steel_wire", "linear", new double[]{205000}, 0.3, 7.8);

            input.setSection("S1", "circular", new double[]{216.3, 12.7});
            input.setSection("T1-1", "circular", new double[]{36});
            input.setSection("T1-2", "circular", new double[]{38});
            input.setSection("T1-3", "circular", new double[]{32});
            input.setSection("T1-4", "circular", new double[]{28});
            input.setSection("T1-5", "circular", new double[]{28});
            input.setSection("T2-1", "circular", new double[]{14.5});
            input.setSection("T2-2", "circular", new double[]{14.5});
            input.setSection("T2-3", "circular", new double[]{20.2});
            input.setSection("T2-4", "circular", new double[]{21.9});
            input.setSection("T3-1", "circular", new double[]{32});
            input.setSection("T3-2", "circular", new double[]{32});
            input.setSection("T3-3", "circular", new double[]{28});
            input.setSection("T3-4", "circular", new double[]{25});
            input.setSection("T3-5", "circular", new double[]{28});
            input.setSection("T4-1", "circular", new double[]{24.9});
            input.setSection("T4-2", "circular", new double[]{24.9});
            input.setSection("T4-3", "circular", new double[]{27.5});
            input.setSection("T4-4", "circular", new double[]{24.9});
            input.setSection("T4-5", "circular", new double[]{24.9});
            input.setSection("T5-1", "circular", new double[]{24.9});
            input.setSection("T5-2", "circular", new double[]{24.9});
            input.setSection("T5-3", "circular", new double[]{24.9});
            input.setSection("T5-4", "circular", new double[]{24.9});
            input.setSection("T5-5", "circular", new double[]{24.9});

            for (int nodeNum : coords.keySet()) {
                double x = coords.get(nodeNum).get(0);
                double y = coords.get(nodeNum).get(1);
                double z = coords.get(nodeNum).get(2);
                input.setNode(nodeNum, x, y, z);
            }

            input.setElement(1, "steel", "S1", 1, 6, 0);
            input.setElement(2, "steel", "S1", 2, 7, 0);
            input.setElement(3, "steel", "S1", 3, 8, 0);
            input.setElement(4, "steel", "S1", 4, 9, 0);
            input.setElement(5, "steel", "S1", 5, 10, 0);

            input.setElement(6, "steel_wire", "T1-1", 1, 7, 0);
            input.setElement(7, "steel_wire", "T1-2", 2, 8, 0);
            input.setElement(8, "steel_wire", "T1-3", 3, 9, 0);
            input.setElement(9, "steel_wire", "T1-4", 4, 10, 0);
            input.setElement(10, "steel_wire", "T1-5", 5, 6, 0);

            input.setElement(11, "steel_wire", "T2-1", 1, 8, 0);
            input.setElement(12, "steel_wire", "T2-2", 3, 10, 0);
            input.setElement(13, "steel_wire", "T2-3", 4, 6, 0);
            input.setElement(14, "steel_wire", "T2-4", 5, 7, 0);

            input.setElement(15, "steel_wire", "T3-1", 6, 7, 0);
            input.setElement(16, "steel_wire", "T3-2", 7, 8, 0);
            input.setElement(17, "steel_wire", "T3-3", 8, 9, 0);
            input.setElement(18, "steel_wire", "T3-4", 9, 10, 0);
            input.setElement(19, "steel_wire", "T3-5", 10, 6, 0);

            input.setElement(20, "steel_wire", "T4-1", 1, 2, 0);
            input.setElement(21, "steel_wire", "T4-2", 2, 3, 0);
            input.setElement(22, "steel_wire", "T4-3", 3, 4, 0);
            input.setElement(23, "steel_wire", "T4-4", 4, 5, 0);
            input.setElement(24, "steel_wire", "T4-5", 5, 1, 0);

            input.setElement(25, "steel_wire", "T5-1", 6, 11, 0);
            input.setElement(26, "steel_wire", "T5-2", 7, 11, 0);
            input.setElement(27, "steel_wire", "T5-3", 8, 11, 0);
            input.setElement(28, "steel_wire", "T5-4", 9, 11, 0);
            input.setElement(29, "steel_wire", "T5-5", 10, 11, 0);

            FileWriter writer3 = new FileWriter(new File(outDir + "length_analysis.csv"));
            for (int elementNum : input.getElements().keySet()) {
                writer3.write(elementNum + "," + input.getElements().get(elementNum).getL() + "\n");
            }
            writer3.close();

            input.setElementLength(6, 6045.6);
            input.setElementLength(7, 6069);
            input.setElementLength(8, 6404.9);
            input.setElementLength(9, 6657.8);
            input.setElementLength(10, 6448.5);

            input.setElementLength(11, 5894.5);
            input.setElementLength(12, 6452.9);
            input.setElementLength(13, 6405.2);
            input.setElementLength(14, 6015.3);

            input.setElementLength(15, 1997.4);
            input.setElementLength(16, 1996.3);
            input.setElementLength(17, 1995.6);
            input.setElementLength(18, 1998);
            input.setElementLength(19, 1997.6);

            input.setElementLength(20, 5999.9);
            input.setElementLength(21, 6000);
            input.setElementLength(22, 6000.1);
            input.setElementLength(23, 6000);
            input.setElementLength(24, 5999.8);

            input.setElementLength(25, 1973.5);
            input.setElementLength(26, 1972.4);
            input.setElementLength(27, 1971.7);
            input.setElementLength(28, 1967.7);
            input.setElementLength(29, 1973);

            writer3 = new FileWriter(new File(outDir + "length_actual.csv"));
            for (int elementNum : input.getElements().keySet()) {
                writer3.write(elementNum + "," + input.getElements().get(elementNum).getL() + "\n");
            }
            writer3.close();

            input.setAxialForce(1, prestresses.get("S1-1") * 1e3);
            input.setAxialForce(2, prestresses.get("S1-2") * 1e3);
            input.setAxialForce(3, prestresses.get("S1-3") * 1e3);
            input.setAxialForce(4, prestresses.get("S1-4") * 1e3);
            input.setAxialForce(5, prestresses.get("S1-5") * 1e3);

            input.setAxialForce(6, prestresses.get("T1-1") * 1e3);
            input.setAxialForce(7, prestresses.get("T1-2") * 1e3);
            input.setAxialForce(8, prestresses.get("T1-3") * 1e3);
            input.setAxialForce(9, prestresses.get("T1-4") * 1e3);
            input.setAxialForce(10, prestresses.get("T1-5") * 1e3);

            input.setAxialForce(11, prestresses.get("T2-1") * 1e3);
            input.setAxialForce(12, prestresses.get("T2-2") * 1e3);
            input.setAxialForce(13, prestresses.get("T2-3") * 1e3);
            input.setAxialForce(14, prestresses.get("T2-4") * 1e3);

            input.setAxialForce(15, prestresses.get("T3-1") * 1e3);
            input.setAxialForce(16, prestresses.get("T3-2") * 1e3);
            input.setAxialForce(17, prestresses.get("T3-3") * 1e3);
            input.setAxialForce(18, prestresses.get("T3-4") * 1e3);
            input.setAxialForce(19, prestresses.get("T3-5") * 1e3);

            input.setAxialForce(20, prestresses.get("T4-1") * 1e3);
            input.setAxialForce(21, prestresses.get("T4-2") * 1e3);
            input.setAxialForce(22, prestresses.get("T4-3") * 1e3);
            input.setAxialForce(23, prestresses.get("T4-4") * 1e3);
            input.setAxialForce(24, prestresses.get("T4-5") * 1e3);

            input.setAxialForce(25, prestresses.get("T5-1") * 1e3);
            input.setAxialForce(26, prestresses.get("T5-2") * 1e3);
            input.setAxialForce(27, prestresses.get("T5-3") * 1e3);
            input.setAxialForce(28, prestresses.get("T5-4") * 1e3);
            input.setAxialForce(29, prestresses.get("T5-5") * 1e3);

            /*
            input.setConfinement(1, 0, 1, 1, 0, 0, 0);
            input.setConfinement(2, 0, 0, 1, 0, 0, 0);
            input.setConfinement(3, 1, 0, 1, 0, 0, 0);
            input.setConfinement(4, 1, 0, 1, 0, 0, 0);
            input.setConfinement(5, 0, 0, 1, 0, 0, 0);
            */
            input.setConfinement(1, 1, 1, 1, 0, 0, 0);
            input.setConfinement(2, 1, 1, 1, 0, 0, 0);
            input.setConfinement(3, 1, 1, 1, 0, 0, 0);
            input.setConfinement(4, 1, 1, 1, 0, 0, 0);
            input.setConfinement(5, 1, 1, 1, 0, 0, 0);

            //input.addTotalLoad(11, -36.1 * 49.2 / 265.2 * 1e3, 13.3 * 49.2 / 265.2 * 1e3, -262.3 * 49.2 / 265.2 * 1e3);
            //input.addTotalLoad(11, -36.1 * 1e3, 13.3 * 1e3, -262.3 * 1e3);
            //input.addTotalLoad(11, -11.3 * 1e3, 0, -85.1 * 1e3);
            input.addGravityLoad(0, 0, -1);

            //double alpha = 46.3 / 85.9;
            //double alpha = 400 / 85.9;
            double alpha = 83 / 85.9;
            input.addTotalLoad(11, -11.3 * alpha * 1e3, 0, -85.1 * alpha * 1e3);
            for (int nodeNum : input.getTotalLoads().keySet()) {
                input.addConcentratedLoad(
                        nodeNum,
                        input.getTotalLoads().get(nodeNum)[0],
                        input.getTotalLoads().get(nodeNum)[1],
                        input.getTotalLoads().get(nodeNum)[2]
                );
            }

            // 不釣り合い力の確認
            HashMap<Integer, double[]> disequilibriumForceMap = input.calculateDisequilibriumForce();
            FileWriter writer2 = new FileWriter(new File(outDir + "disequilibrium_force.csv"));
            for (int nodeNum : disequilibriumForceMap.keySet()) {
                double[] disequilibriumForce = disequilibriumForceMap.get(nodeNum);
                writer2.write(nodeNum + ",");
                for (int i = 0; i < 3; i++) {
                    if (!input.getConfinements().containsKey(nodeNum)
                            || input.getConfinements().get(nodeNum)[i]) {
                        writer2.write(disequilibriumForce[i] * 1e-3 + ",");
                    } else {
                        writer2.write(0 + ",");
                    }
                }
                writer2.write("\n");
            }
            writer2.close();


            /*
            while (!input.isInEquilibrium()) {
                TrussLinearAnalysis analysis = new TrussLinearAnalysis(input, 1, false);
                analysis.solve();
                input = analysis.exportDataset();
            }
            */

            /*
            // ↓↓水谷さん説明用↓↓
            FileWriter writerTmp = new FileWriter(new File(outDir + "modified_coords.csv"));
            for (int nodeNum : input.getNodes().keySet()) {
                double[] node = input.getNodes().get(nodeNum);
                writerTmp.write(nodeNum + "," + node[0] + "," + node[1] + "," + node[2] + "\n");
            }
            writerTmp.close();
            // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
            */

            double delta = 1;

            /*
            input.clearTotalLoad();
            input.addGravityLoad(0, 0, 1);
            alpha = -83 / 85.9;
            input.addTotalLoad(11, -11.3 * alpha * 1e3, 0, -85.1 * alpha * 1e3);
            TrussNonlinearAnalysis analysis1 = new TrussNonlinearAnalysis(input, delta);
            analysis1.solve(null);
            input = analysis1.exportDataset();
             */
            //input.clearConcentratedLoad();
            input.clearTotalLoad();
            //input.addGravityLoad(0, 0, -1);
            //input.addTotalLoad(11, -36.1 * 1e3, 13.3 * 1e3, -262.3 * 1e3);
            alpha = (132.2 - 83) / 265.2;
            input.addTotalLoad(11, alpha * -36.1 * 1e3, alpha * 13.3 * 1e3, alpha * -262.3 * 1e3);

            FileWriter writer = new FileWriter(new File(outDir + "axial_force.csv"));
            writer.write(Double.toString(83));
            writer.write(",");
            for (int elementNum : input.getElements().keySet()) {
                writer.write(Double.toString(input.getAxialForce(elementNum) * 1e-3));
                writer.write(",");
            }
            writer.write("\n");
            TrussNonlinearAnalysis analysis2 = new TrussNonlinearAnalysis(input, delta);
            analysis2.solve(writer);
            input = analysis2.exportDataset();

            /*
            for (int elementNum : input.getElements().keySet()) {
                writer.write(Double.toString(input.getAxialForce(elementNum) * 1e-3) + "\n");
            }
             */
            writer.close();

            // 不釣り合い力の確認
            disequilibriumForceMap = input.calculateDisequilibriumForce();
            writer2 = new FileWriter(new File(outDir + "disequilibrium_force_after.csv"));
            for (int nodeNum : disequilibriumForceMap.keySet()) {
                double[] disequilibriumForce = disequilibriumForceMap.get(nodeNum);
                writer2.write(nodeNum + ",");
                for (int i = 0; i < 3; i++) {
                    if (!input.getConfinements().containsKey(nodeNum)
                            || input.getConfinements().get(nodeNum)[i]) {
                        writer2.write(disequilibriumForce[i] * 1e-3 + ",");
                    } else {
                        writer2.write(0 + ",");
                    }
                }
                writer2.write("\n");
            }
            writer2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

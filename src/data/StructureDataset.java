package data;

import structure.Member;
import structure.Material;
import structure.section.Section;
import structure.section.CircularSection;
import structure.model.MaterialModel;
import structure.model.LinearModel;
import structure.model.BiLinearModel;
import java.util.HashMap;

public class StructureDataset {

    private HashMap<String, Material> materialMap;
    private HashMap<String, Section> sectionMap;
    private HashMap<Integer, double[]> nodeMap;
    private HashMap<Integer, Member> elementMap;
    private HashMap<Integer, double[]> concentratedLoadMap;
    private HashMap<Integer, boolean[][]> connectionMap;
    private HashMap<Integer, boolean[]> confinementMap;
    private HashMap<Integer, Double> axialForceMap;
    private int freeDispSize;

    public StructureDataset() {
        this.materialMap = new HashMap<>();
        this.sectionMap = new HashMap<>();
        this.nodeMap = new HashMap<>();
        this.elementMap = new HashMap<>();
        this.concentratedLoadMap = new HashMap<>();
        this.connectionMap = new HashMap<>();
        this.confinementMap = new HashMap<>();
        this.axialForceMap = new HashMap<>();
    }

    public void setMaterial(String key, String materialType, double[] params, double gamma, double rho) {
        MaterialModel model;
        if (materialType.equals("linear")) {
            model = new LinearModel(params[0]);
        } else if (materialType.equals("nonlinear")) {
            model = new BiLinearModel(params[0], params[1], params[2], params[3]);
        } else {
            model = null;
            System.out.println("Material type: \"" + materialType + "\" is invalid.");
            System.exit(1);
        }
        this.materialMap.put(key, new Material(model, gamma, rho));
    }

    public void setSection(String key, String sectionType, double[] params) {
        if (sectionType.equals("circular")) {
            double d = params[0];
            if (params.length == 1) {
                this.sectionMap.put(key, new CircularSection(d));
            } else if (params.length == 2) {
                double t = params[1];
                this.sectionMap.put(key, new CircularSection(d, t));
            }
        } else {
            System.out.println("Section type: \"" + sectionType + "\" is invalid.");
            System.exit(1);
        }
    }

    public void setNode(int nodeNum, double x, double y, double z) {
        this.nodeMap.put(nodeNum, new double[]{x, y, z});
    }

    public void moveNode(int nodeNum, double dx, double dy, double dz) {
        double[] movedCoord = new double[]{
            this.nodeMap.get(nodeNum)[0] + dx,
            this.nodeMap.get(nodeNum)[1] + dy,
            this.nodeMap.get(nodeNum)[2] + dz,};
        this.nodeMap.replace(nodeNum, movedCoord);
    }

    public void setElement(int elementNum, String materialKey, String sectionKey, int i, int j, double theta) {
        this.elementMap.put(elementNum, new Member(this.materialMap.get(materialKey), this.sectionMap.get(sectionKey), i, j, this.nodeMap.get(i), this.nodeMap.get(j), theta));
    }

    public void addConcentratedLoad(int nodeNum, double x, double y, double z) {
        if (this.concentratedLoadMap.containsKey(nodeNum)) {
            this.concentratedLoadMap.get(nodeNum)[0] += x;
            this.concentratedLoadMap.get(nodeNum)[1] += y;
            this.concentratedLoadMap.get(nodeNum)[2] += z;
        } else {
            this.concentratedLoadMap.put(nodeNum, new double[]{x, y, z});
        }
    }

    public void addGravityLoad(double x, double y, double z) {
        double[] coords = new double[]{x, y, z};
        this.getElements().keySet().forEach((Integer elementNum) -> {
            Member mem = this.getElements().get(elementNum);
            double[] fGravityGlobalElement = new double[3];
            final double g = 9.8;
            for (int i = 0; i < 3; i++) {
                fGravityGlobalElement[i] = mem.getRho() * 1e-6 * mem.getA() * g * coords[i] * mem.getL() / 2;
            }

            if (this.concentratedLoadMap.containsKey(mem.getIndexI())) {
                for (int i = 0; i < 3; i++) {
                    this.concentratedLoadMap.get(mem.getIndexI())[i] += fGravityGlobalElement[i];
                }
            } else {
                this.concentratedLoadMap.put(mem.getIndexI(), fGravityGlobalElement);
            }
        });
    }

    public void setConnection(int elementNum, int isThetaIxFree, int isThetaIyFree, int isThetaIzFree, int isThetaJxFree, int isThetaJyFree, int isThetaJzFree) {
        int[] isNodeFree = new int[]{isThetaIxFree, isThetaIyFree, isThetaIzFree, isThetaJxFree, isThetaJyFree, isThetaJzFree};
        boolean[][] freeFlagList = new boolean[2][3];

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                switch (isNodeFree[3 * i + j]) {
                    case 0:
                        freeFlagList[i][j] = true;
                        break;
                    case 1:
                        freeFlagList[i][j] = false;
                        break;
                    default:
                        System.out.println("Connection parameter: \"" + isNodeFree[i] + "\" is invalid.");
                        break;
                }
            }
        }

        this.connectionMap.put(elementNum, freeFlagList);
    }

    public void setConfinement(int nodeNum, int isXFree, int isYFree, int isZFree, int isThetaXFree, int isThetaYFree, int isThetaZFree) {
        int[] isNodeFree = new int[]{isXFree, isYFree, isZFree, isThetaXFree, isThetaYFree, isThetaZFree};
        boolean[] freeFlagList = new boolean[6];

        for (int i = 0; i < 6; i++) {
            switch (isNodeFree[i]) {
                case 0:
                    freeFlagList[i] = true;
                    break;
                case 1:
                    freeFlagList[i] = false;
                    break;
                default:
                    System.out.println("Confinement parameter: \"" + isNodeFree[i] + "\" is invalid.");
                    break;
            }
        }

        this.confinementMap.put(nodeNum, freeFlagList);
    }

    public void setAxialForce(int elementNum, double axialForce) {
        this.axialForceMap.put(elementNum, axialForce);
    }

    public void addAxialForce(int elementNum, double axialForce) {
        this.axialForceMap.replace(elementNum, this.axialForceMap.get(elementNum) + axialForce);
    }

    public Material getMaterial(String key) {
        return this.materialMap.get(key);
    }

    public Section getSection(String key) {
        return this.sectionMap.get(key);
    }

    public HashMap<Integer, double[]> getNodes() {
        return this.nodeMap;
    }

    public HashMap<Integer, Member> getElements() {
        return this.elementMap;
    }

    public int getSize() {
        return this.nodeMap.size();
    }

    public HashMap<Integer, double[]> getConcentratedLoads() {
        return this.concentratedLoadMap;
    }

    public HashMap<Integer, boolean[][]> getConnections() {
        return this.connectionMap;
    }

    public HashMap<Integer, boolean[]> getConfinements() {
        return this.confinementMap;
    }

    public double getAxialForce(int elementNum) {
        return this.axialForceMap.get(elementNum);
    }

    public int getFreeDispSize() {
        this.freeDispSize = 3 * this.getSize();
        for (int nodeNum : this.getConfinements().keySet()) {
            for (int i = 0; i < 3; i++) {
                if (!this.getConfinements().get(nodeNum)[i]) {
                    this.freeDispSize--;
                }
            }
        }
        return this.freeDispSize;
    }
}

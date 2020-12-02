
import java.util.HashMap;

public class InputDataset {

    private HashMap<String, Material> materialMap;
    private HashMap<String, Section> sectionMap;
    private HashMap<Integer, double[]> nodeMap;
    private HashMap<Integer, Member> elementMap;
    private HashMap<Integer, double[]> concentratedLoadMap;
    private double[] gravityLoad;
    private HashMap<Integer, boolean[][]> connectionMap;
    private HashMap<Integer, boolean[]> confinementMap;

    InputDataset() {
        this.materialMap = new HashMap<>();
        this.sectionMap = new HashMap<>();
        this.nodeMap = new HashMap<>();
        this.elementMap = new HashMap<>();
        this.concentratedLoadMap = new HashMap<>();
        this.connectionMap = new HashMap<>();
        this.confinementMap = new HashMap<>();
    }

    public void setMaterial(String key, double E, double G, double rho) {
        this.materialMap.put(key, new Material(E, G, rho));
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
        }
    }

    public void setNode(int nodeNum, double x, double y, double z) {
        this.nodeMap.put(nodeNum, new double[]{x, y, z});
    }

    public void setElement(int elementNum, String materialKey, String sectionKey, int i, int j, double theta) {
        this.elementMap.put(elementNum, new Member(this.materialMap.get(materialKey), this.sectionMap.get(sectionKey), i, j, this.nodeMap.get(i), this.nodeMap.get(j), theta));
    }

    public void addConcentratedLoad(int nodeNum, double x, double y, double z) {
        if (this.concentratedLoadMap.containsKey(nodeNum)) {
            x += this.concentratedLoadMap.get(nodeNum)[0];
            y += this.concentratedLoadMap.get(nodeNum)[1];
            z += this.concentratedLoadMap.get(nodeNum)[2];
        }
        this.concentratedLoadMap.put(nodeNum, new double[]{x, y, z});
    }

    public void addGravityLoad(double x, double y, double z) {
        this.gravityLoad[0] += x;
        this.gravityLoad[1] += y;
        this.gravityLoad[2] += z;
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

    public double[] getGravityLoad() {
        return this.gravityLoad;
    }

    public HashMap<Integer, boolean[][]> getConnections() {
        return this.connectionMap;
    }

    public HashMap<Integer, boolean[]> getConfinements() {
        return this.confinementMap;
    }
}

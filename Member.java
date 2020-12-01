public class Member {

    private final double E;
    private final double G;
    private final double A;
    private final double Iy;
    private final double Iz;
    private final double J;
    private final double L;
    private double[] nodeI;
    private double[] nodeJ;
    private double theta;

    Member(Material mat, Section sec, double[] nodeI, double[] nodeJ, double theta) {
        this.E = mat.getE();
        this.G = mat.getG();
        this.A = sec.getA();
        this.Iy = sec.getIy();
        this.Iz = sec.getIz();
        this.J = sec.getJ();
        this.L = Math.sqrt(Math.pow(nodeI[0] - nodeJ[0], 2) + Math.pow(nodeI[1] - nodeJ[1], 2) + Math.pow(nodeI[2] - nodeJ[2], 2));
        this.nodeI = nodeI;
        this.nodeJ = nodeJ;
        this.theta = theta;
    }

    public double getE() {
        return this.E;
    }

    public double getG() {
        return this.G;
    }

    public double getA() {
        return this.A;
    }

    public double getIy() {
        return this.Iy;
    }

    public double getIz() {
        return this.Iz;
    }

    public double getJ() {
        return this.J;
    }

    public double getL() {
        return this.L;
    }

    public double[] getNodeI() {
        return this.nodeI;
    }

    public double[] getNodeJ() {
        return this.nodeJ;
    }

    public double getTheta() {
        return this.theta;
    }
}

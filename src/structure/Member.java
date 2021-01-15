package structure;


import structure.section.Section;

public class Member {

    private final Material mat;
    private final double A;
    private final double Iy;
    private final double Iz;
    private final double J;
    private double L;
    private final int indexI;
    private final int indexJ;
    private double theta;

    public Member(Material mat, Section sec, int indexI, int indexJ, double[] nodeI, double[] nodeJ, double theta) {
        this.mat = mat;
        this.A = sec.getA();
        this.Iy = sec.getIy();
        this.Iz = sec.getIz();
        this.J = sec.getJ();
        this.L = Math.sqrt(Math.pow(nodeI[0] - nodeJ[0], 2) + Math.pow(nodeI[1] - nodeJ[1], 2) + Math.pow(nodeI[2] - nodeJ[2], 2));
        this.indexI = indexI;
        this.indexJ = indexJ;
        this.theta = theta;
    }

    public double getE(double sigma) {
        return this.mat.getE(sigma);
    }

    public double getG(double sigma) {
        return this.mat.getG(sigma);
    }

    public double getRho() {
        return this.mat.getRho();
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

    // In case the length of the member is different from the distance between nodes.
    public void setL(double L) {
        this.L = L;
    }

    public double getL() {
        return this.L;
    }

    public int getIndexI() {
        return this.indexI;
    }

    public int getIndexJ() {
        return this.indexJ;
    }

    public double getTheta() {
        return this.theta;
    }
}

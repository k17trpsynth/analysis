public class Member {
    private final double E;
    private final double G;
    private final double A;
    private final double Iy;
    private final double Iz;
    private final double J;
    private final double L;

    Member(double E, double G, double A, double Iy, double Iz, double J, double L) {
        this.E = E;
        this.G = G;
        this.A = A;
        this.Iy = Iy;
        this.Iz = Iz;
        this.J = J;
        this.L = L;
    }

    public double getE() {return this.E;}
    public double getG() {return this.G;}
    public double getA() {return this.A;}
    public double getIy() {return this.Iy;}
    public double getIz() {return this.Iz;}
    public double getJ() {return this.J;}
    public double getL() {return this.L;}
}

public class Material {

    private double E;
    private double G;
    private double rho;

    Material(double E, double G, double rho) {
        this.E = E;
        this.G = G;
        this.rho = rho;
    }

    public double getE() {
        return this.E;
    }

    public double getG() {
        return this.G;
    }

    public double getRho() {
        return this.rho;
    }
}

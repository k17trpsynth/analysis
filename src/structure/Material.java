package structure;


import structure.model.MaterialModel;

public class Material {

    private MaterialModel model;
    private double gamma;
    private double rho;

    public Material(MaterialModel model, double gamma, double rho) {
        this.model = model;
        this.gamma = gamma;
        this.rho = rho;
    }

    public double getE(double sigma) {
        return this.model.getE(sigma);
    }

    public double getG(double sigma) {
        return this.getE(sigma) / 2 / (1 + gamma);
    }

    public double getRho() {
        return this.rho;
    }
}

package structure.model;


public class LinearModel extends MaterialModel {

    private double E;

    public LinearModel(double E) {
        this.E = E;
    }

    public double getE(double sigma) {
        return this.E;
    }
}

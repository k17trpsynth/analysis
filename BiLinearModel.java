
public class BiLinearModel extends MaterialModel {

    private double E1;
    private double E2;
    private double epsilon0;
    private double sigma0;

    public BiLinearModel(double E1, double E2, double epsilon0, double sigma0) {
        this.E1 = E1;
        this.E2 = E2;
        this.epsilon0 = epsilon0;
        this.sigma0 = sigma0;
    }

    public double getE(double sigma) {
        if (sigma < sigma0) {
            return E1;
        } else {
            return E2;
        }
    }

    public double getSigma(double epsilon) {
        double sigma = 0;
        if (epsilon < epsilon0) {
            sigma = E1 * (epsilon - epsilon0) + sigma0;
        } else {
            sigma = E2 * (epsilon - epsilon0) + sigma0;
        }
        return sigma;
    }

    public double getEpsilon(double sigma) {
        double epsilon = 0;
        if (epsilon < epsilon0) {
            if (E1 == 0) {
                epsilon = epsilon0;
            } else {
                epsilon = 1 / E1 * (sigma - sigma0) + epsilon0;
            }
        } else {
            if (E2 == 0) {
                epsilon = epsilon0;
            } else {
                epsilon = 1 / E2 * (sigma - sigma0) + epsilon0;
            }
        }
        return epsilon;
    }
}

public class CircularSection extends Section {

    private double d;
    private double t;

    CircularSection(double d) {
        this(d, d / 2);
    }

    CircularSection(double d, double t) {
        this.d = d;
        this.t = t;
    }


    public double getA() {
        return Math.PI / 4 * (Math.pow(d, 2) - Math.pow(d - 2 * t, 2));
    }

    public double getIy() {
        return Math.PI / 64 * (Math.pow(d, 4) - Math.pow(d - 2 * t, 4));
    }

    public double getIz() {
        return this.getIy();
    }
}

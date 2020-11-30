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
        return Math.PI / 4 * (Math.pow(this.d, 2) - Math.pow(this.d - 2 * this.t, 2));
    }

    public double getIy() {
        return Math.PI / 64 * (Math.pow(this.d, 4) - Math.pow(this.d - 2 * this.t, 4));
    }

    public double getIz() {
        return this.getIy();
    }

    public double getJ() {
	    if (this.t == d / 2) {
		    return Math.PI / 32 * Math.pow(this.d, 4);
	    } else {
		    return 4 * Math.pow(this.getA(), 2) * this.t / (Math.PI * (this.d - this.t));
	    }
    }
}

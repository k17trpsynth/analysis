package matrix;

import structure.Member;
import org.ejml.data.DMatrixSparseCSC;

@SuppressWarnings({"serial", "unchecked"})
public class ElementStiffnessMatrix extends DMatrixSparseCSC {

    public ElementStiffnessMatrix(Member mem) {
        super(12, 12);

        double E = mem.getE(0);
        double G = mem.getG(0);
        double A = mem.getA();
        double Iy = mem.getIy();
        double Iz = mem.getIz();
        double J = mem.getJ();
        double L = mem.getL();

        double kn = E * A / L;
        double kmy1 = 12 * E * Iy / Math.pow(L, 3);
        double kmy2 = 6 * E * Iy / Math.pow(L, 2);
        double kmy3 = 4 * E * Iy / L;
        double kmz1 = 12 * E * Iz / Math.pow(L, 3);
        double kmz2 = 6 * E * Iz / Math.pow(L, 2);
        double kmz3 = 4 * E * Iz / L;
        double kt = G * J / L;

        super.set(0, 0, kn);
        super.set(1, 1, kmz1);
        super.set(1, 5, kmz2);
        super.set(2, 2, kmy1);
        super.set(2, 4, -kmy2);
        super.set(3, 3, kt);
        super.set(4, 2, -kmy2);
        super.set(4, 4, kmy3);
        super.set(5, 1, kmz2);
        super.set(5, 5, kmz3);

        super.set(0, 6 + 0, -kn);
        super.set(1, 6 + 1, -kmz1);
        super.set(1, 6 + 5, kmz2);
        super.set(2, 6 + 2, -kmy1);
        super.set(2, 6 + 4, -kmy2);
        super.set(3, 6 + 3, -kt);
        super.set(4, 6 + 2, kmy2);
        super.set(4, 6 + 4, kmy3);
        super.set(5, 6 + 1, -kmz2);
        super.set(5, 6 + 5, kmz3);

        super.set(6 + 0, 0, -kn);
        super.set(6 + 1, 1, -kmz1);
        super.set(6 + 1, 5, -kmz2);
        super.set(6 + 2, 2, -kmy1);
        super.set(6 + 2, 4, kmy2);
        super.set(6 + 3, 3, -kt);
        super.set(6 + 4, 2, -kmy2);
        super.set(6 + 4, 4, kmy3);
        super.set(6 + 5, 1, kmz2);
        super.set(6 + 5, 5, kmz3);

        super.set(6 + 0, 6 + 0, kn);
        super.set(6 + 1, 6 + 1, kmz1);
        super.set(6 + 1, 6 + 5, -kmz2);
        super.set(6 + 2, 6 + 2, kmy1);
        super.set(6 + 2, 6 + 4, kmy2);
        super.set(6 + 3, 6 + 3, kt);
        super.set(6 + 4, 6 + 2, kmy2);
        super.set(6 + 4, 6 + 4, kmy3);
        super.set(6 + 5, 6 + 1, -kmz2);
        super.set(6 + 5, 6 + 5, kmz3);
    }
}


import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class GraphDrawer {

    public static void main(String[] args) {

        String[] codes = {
            "S1-1",
            "S1-2",
            "S1-3",
            "S1-4",
            "S1-5",
            "T1-1",
            "T1-2",
            "T1-3",
            "T1-4",
            "T1-5",
            "T2-1",
            "T2-2",
            "T2-3",
            "T2-4",
            "T3-1",
            "T3-2",
            "T3-3",
            "T3-4",
            "T3-5",
            "T4-1",
            "T4-2",
            "T4-3",
            "T4-4",
            "T4-5",
            "T5-1",
            "T5-2",
            "T5-3",
            "T5-4",
            "T5-5",};

        try {
            String baseDir = new File(".").getAbsoluteFile().getParent();
            String outDir = baseDir + "/out/";
            String pngDir = "/Users/Kota/Documents/architecture/kawaguchi_lab/white_rhino/";
            BufferedReader reader = new BufferedReader(new FileReader(new File(outDir + "axial_force.csv")));
            String line;
            ArrayList<ArrayList<Double>> axialForces = new ArrayList<>();

            while (Objects.nonNull(line = reader.readLine())) {
                ArrayList<Double> axialForce = new ArrayList<>();
                for (String force : line.split(",")) {
                    axialForce.add(Double.parseDouble(force));
                }
                axialForces.add(axialForce);
            }

            reader.close();

            reader = new BufferedReader(new FileReader(new File(outDir + "axial_force_measured.csv")));
            ArrayList<ArrayList<Double>> axialForcesMeasured = new ArrayList<>();

            while (Objects.nonNull(line = reader.readLine())) {
                ArrayList<Double> axialForce = new ArrayList<>();
                for (String force : line.split(",")) {
                    axialForce.add(Double.parseDouble(force));
                }
                axialForcesMeasured.add(axialForce);
            }

            reader.close();

            int w = 320;
            int h = 320;

            for (int i = 0; i < axialForces.get(0).size(); i++) {
                XYSeriesCollection collection = new XYSeriesCollection();
                XYSeriesCollection collection2 = new XYSeriesCollection();

                XYSeries series = new XYSeries(codes[i] + " analyzed");
                for (int j = 0; j < axialForces.size(); j++) {
                    series.add(49.2 * j * 0.01 + 83, axialForces.get(j).get(i));
                }
                collection.addSeries(series);

                double initialAxialForce = 0;
                int num = 54;
                for (int j = 0; j < num; j++) {
                    initialAxialForce += axialForcesMeasured.get(j).get(i + 1);
                }
                initialAxialForce /= num;

                series = new XYSeries(codes[i] + " measured");
                for (int j = 0; j < axialForcesMeasured.size(); j++) {
                    series.add(-axialForcesMeasured.get(j).get(0), axialForcesMeasured.get(j).get(i + 1));
                }
                collection2.addSeries(series);

                JFreeChart chart = ChartFactory.createScatterPlot(codes[i], "Outer force (kN)", "Axial force (kN)", collection2, PlotOrientation.VERTICAL, true, false, false);
                XYPlot plot = chart.getXYPlot();
                plot.setBackgroundPaint(Color.WHITE);
                plot.setDataset(1, collection);
                XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
                plot.setRenderer(1, renderer);
                renderer.setSeriesPaint(1, ChartColor.BLUE);
                OutputStream output = new FileOutputStream(pngDir + "forcePng/" + codes[i] + ".png");
                ChartUtilities.writeChartAsPNG(output, chart, w, h);
            }

            BufferedImage largeImg = new BufferedImage(w * 5, h * 6, BufferedImage.TYPE_INT_RGB);
            int i = 0;
            for (String code : codes) {
                BufferedImage img = ImageIO.read(new FileInputStream(pngDir + "forcePng/" + code + ".png"));
                Graphics graphics = largeImg.getGraphics();

                if (i == 14) {
                    graphics.fillRect(w * (i % 5), h * (i / 5), w, h);
                    i++;
                }
                graphics.drawImage(img, w * (i % 5), h * (i / 5), null);
                i++;
            }
            ImageIO.write(largeImg, "png", new File(pngDir + "large.png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

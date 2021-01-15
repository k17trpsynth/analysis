package main;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class GraphDrawer {

    public static ArrayList<ArrayList<Double>> readCSV(String path) {
        ArrayList<ArrayList<Double>> list = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
            String line;

            while (Objects.nonNull(line = reader.readLine())) {
                ArrayList<Double> axialForce = new ArrayList<>();
                for (String force : line.split(",")) {
                    axialForce.add(Double.parseDouble(force));
                }
                list.add(axialForce);
            }

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

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

            ArrayList<ArrayList<Double>> axialForces = readCSV(outDir + "axial_force.csv");
            ArrayList<ArrayList<Double>> axialForcesMeasured = readCSV(outDir + "axial_force_measured.csv");
            ArrayList<ArrayList<Double>> axialForcesDesigned = readCSV(outDir + "axial_force_designed.csv");

            int w = 350;
            int h = w;

            String tmpDirPath = pngDir + "forcePng/";
            File tmpDir = new File(tmpDirPath);
            if (!tmpDir.exists()) {
                tmpDir.mkdir();
            }

            double xLower = 80;
            double xUpper = 140;

            for (int i = 0; i < codes.length; i++) {
                XYSeriesCollection lineCollection = new XYSeriesCollection();
                XYSeriesCollection scatterCollection = new XYSeriesCollection();

                /*
                double initialAxialForce = 0;
                int num = 54;
                for (int j = 0; j < num; j++) {
                    initialAxialForce += axialForcesMeasured.get(j).get(i + 1);
                }
                initialAxialForce /= num;
                 */
                XYSeries series = new XYSeries("実測値");
                for (int j = 0; j < axialForcesMeasured.size(); j++) {
                    series.add(-axialForcesMeasured.get(j).get(0), axialForcesMeasured.get(j).get(i + 1));
                }
                scatterCollection.addSeries(series);

                double[] params = Regression.getOLSRegression(scatterCollection, 0);
                double a = params[0];
                double b = params[1];
                series = new XYSeries("実測値回帰");
                for (double x : new double[]{xLower, xUpper}) {
                    series.add(x, a + b * x);
                }
                lineCollection.addSeries(series);

                series = new XYSeries("修正解析値");
                for (int j = 0; j < axialForces.size(); j++) {
                    double load = axialForces.get(j).get(0);
                    if (load >= xLower && load <= xUpper) {
                        series.add(load, axialForces.get(j).get(i + 1));
                    }
                }
                lineCollection.addSeries(series);

                series = new XYSeries("設計時解析値");
                for (int j = 0; j < axialForcesDesigned.size(); j++) {
                    double load = axialForcesDesigned.get(j).get(0);
                    double axialForce = axialForcesDesigned.get(j).get(i + 1);
                    if (load >= xLower && load <= xUpper) {
                        series.add(load, axialForce);
                    }
                }
                lineCollection.addSeries(series);

                JFreeChart chart = ChartFactory.createScatterPlot(codes[i],
                        "External force (kN)",
                        "Axial force (kN)",
                        scatterCollection,
                        PlotOrientation.VERTICAL,
                        true,
                        false,
                        false
                );
                XYPlot plot = chart.getXYPlot();
                /*
                ValueAxis yAxis = plot.getRangeAxis();
                if (i <= 4) {
                    yAxis.setRange(-400, 0);
                } else if (i <= 9) {
                    yAxis.setRange(0, 320);
                } else if (i <= 13) {
                    yAxis.setRange(0, 100);
                } else if (i <= 18) {
                    yAxis.setRange(0, 300);
                } else {
                    yAxis.setRange(0, 100);
                }
                 */
                plot.setBackgroundPaint(Color.WHITE);
                plot.setDataset(1, lineCollection);
                XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
                plot.setRenderer(1, renderer);
                renderer.setSeriesPaint(0, ChartColor.RED);
                renderer.setSeriesPaint(1, ChartColor.BLUE);
                renderer.setSeriesPaint(2, ChartColor.CYAN);
                renderer.setSeriesShapesVisible(0, false);
                renderer.setSeriesShapesVisible(1, false);
                renderer.setSeriesShapesVisible(2, false);
                if (i <= 23) {
                    chart.removeLegend();
                }
                OutputStream output = new FileOutputStream(tmpDirPath + codes[i] + ".png");
                ChartUtilities.writeChartAsPNG(output, chart, w, h);
            }

            BufferedImage largeImg = new BufferedImage(w * 5, h * 6, BufferedImage.TYPE_INT_RGB);
            int i = 0;
            for (String code : codes) {
                String smallPngPath = tmpDirPath + code + ".png";
                BufferedImage img = ImageIO.read(new FileInputStream(smallPngPath));
                Graphics graphics = largeImg.getGraphics();

                if (i == 14) {
                    graphics.fillRect(w * (i % 5), h * (i / 5), w, h);
                    i++;
                }
                graphics.drawImage(img, w * (i % 5), h * (i / 5), null);
                new File(smallPngPath).delete();
                i++;
            }

            tmpDir.delete();
            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
            ImageIO.write(largeImg, "png", new File(pngDir + "large" + dateFormat.format(date) + ".png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

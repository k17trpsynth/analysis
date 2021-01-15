package main;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class BarGraphDrawer {

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

    public static void main(String[] args) {
        ArrayList<String> pathList = new ArrayList<>();
        ArrayList<String> legendList = new ArrayList<>();
        String baseDir = new File(".").getAbsoluteFile().getParent();
        String dataDir = baseDir + "/data/";
        String outDir = baseDir + "/out/";

        String path;

        path = outDir + "length_analysis.csv";
        pathList.add(path);
        path = outDir + "length_actual.csv";
        pathList.add(path);

        legendList.add("節点巻距離");
        legendList.add("実部材長さ");

        BarGraphDrawer drawer = new BarGraphDrawer();
        drawer.createChart(pathList, legendList, null, null, null);
    }

    public void createChart(ArrayList<String> pathList, ArrayList<String> legendList, String title, String xLabel, String yLabel) {
        String pngDir = "/Users/Kota/Documents/architecture/kawaguchi_lab/white_rhino/";
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < pathList.size(); i++) {
            HashMap<Integer, Double> map = readCSV(pathList.get(i));
            for (int elementNum : map.keySet()) {
                dataset.addValue(map.get(elementNum), legendList.get(i), codes[elementNum - 1]);
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(title, xLabel, yLabel, dataset);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setItemMargin(0);
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.RED);
        CategoryAxis xAxis = plot.getDomainAxis();
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        try {
            int w = 600;
            int h = w / 2;
            OutputStream output = new FileOutputStream(pngDir + "member_length.png");
            ChartUtilities.writeChartAsPNG(output, chart, w, h);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ChartFrame frame = new ChartFrame("Frame title", chart, true);
        frame.pack();
        frame.setVisible(true);
    }

    public static HashMap<Integer, Double> readCSV(String path) {
        HashMap<Integer, Double> map = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
            String line;
            while (Objects.nonNull(line = reader.readLine())) {
                int x = Integer.parseInt(line.split(",")[0]);
                double y = Double.parseDouble(line.split(",")[1]);
                map.put(x, y);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }
}

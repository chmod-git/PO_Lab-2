import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;

public class PlotBuilder {
    public static void plotBarGraph(String title, String xLabel, String yLabel,
                                    String series1Name, int[] xData1, double[] yData1,
                                    String series2Name, int[] xData2, double[] yData2) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < xData1.length; i++) {
            dataset.addValue(yData1[i], series1Name, String.valueOf(xData1[i]));
        }
        for (int i = 0; i < xData2.length; i++) {
            dataset.addValue(yData2[i], series2Name, String.valueOf(xData2[i]));
        }

        JFreeChart chart = ChartFactory.createBarChart(title, xLabel, yLabel, dataset);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));

        JFrame frame = new JFrame("Parallelism");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }
}


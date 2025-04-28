package com.sdm.view;

import com.sdm.model.ModelScore;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.style.Styler;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * View class to render model evaluation results as a bar chart.
 */
public final class EvaluationChartHandler {

    private EvaluationChartHandler() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void showModelComparisonChart(List<ModelScore> scores, String metric) {
        final CategoryChart chart = new CategoryChartBuilder()
                .width(1000)
                .height(600)
                .title("Model Comparison – " + metric)
                .xAxisTitle("Models")
                .yAxisTitle(metric)
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);
        chart.getStyler().setToolTipsEnabled(true);

        for (ModelScore score : scores) {
            chart.addSeries(score.modelName, List.of(""), List.of(selectMetric(score, metric)));
        }

        JFrame frame = new JFrame("Model Evaluation Chart");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new XChartPanel<>(chart));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static double selectMetric(ModelScore score, String metric) {
        return switch (metric.toLowerCase()) {
            case "r²", "r2" -> score.rSquared;
            case "mse" -> score.mse;
            case "rmse" -> score.rmse;
            case "mae" -> score.mae;
            case "predicted" -> score.predictedPrice;
            default -> 0;
        };
    }
}

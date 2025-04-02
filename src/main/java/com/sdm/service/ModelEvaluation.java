package com.sdm.service;
import com.sdm.app.App;
import com.sdm.model.ModelScore;
import javax.swing.*;
import org.knowm.xchart.*;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.IntStream;
import java.awt.Font; // For setting font on axis labels
import java.awt.Color; // For custom bar colors
import org.knowm.xchart.style.Styler; 
import java.util.logging.Logger;


@SuppressWarnings({
    "PMD.OnlyOneReturn",
    "PMD.ShortVariable",
    "PMD.LongVariable",
    "PMD.LawOfDemeter",
    "PMD.UseLocaleWithCaseConversions",
    "PMD.ControlStatementBraces",
    "PMD.GuardLogStatement","PMD.AtLeastOneConstructor"
})
public class ModelEvaluation {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    
    public void evaluate(final List<Double> actualPrices,final List<Double> predictedPrices) {
        if (!isValidData(actualPrices, predictedPrices)) return; //  Check validity

        
        LOGGER.info("Evaluating Model...");

        final double mse = calculateMSE(actualPrices, predictedPrices);
        final double rSquared = calculateRSquared(actualPrices, predictedPrices);

        final String message = String.format(" Model Evaluation:\nMSE: %.4f\nR² Score: %.4f", mse, rSquared);
        showMessage(message, "Model Evaluation");

        plotResults(actualPrices, predictedPrices);
    }

    // Check if lists are valid
    public boolean isValidData(final List<Double> actual, final List<Double> predicted) {
        boolean valid = true;
    
        if (actual == null || predicted == null || actual.isEmpty() || predicted.isEmpty()) {
            showMessage(" Error: Price lists cannot be empty!", "Error");
            valid = false;
        } else if (actual.size() != predicted.size()) {
            showMessage(" Error: Actual and predicted lists must have the same length!", "Error");
            valid = false;
        }
    
        return valid;
    }
    

    public double calculateMSE(final List<Double> actual, final List<Double> predicted) {
        return IntStream.range(0, actual.size())
                .mapToDouble(i -> Math.pow(actual.get(i) - predicted.get(i), 2))
                .average().orElse(Double.NaN); //  Returns NaN if empty
    }

    public double calculateRSquared(final List<Double> actual, final List<Double> predicted) {
       final OptionalDouble meanActualOpt = actual.stream().mapToDouble(a -> a).average();
        if (meanActualOpt.isEmpty()) return 1; //  Perfect fit if no variance

        final double meanActual = meanActualOpt.getAsDouble();
        final double totalVariance = actual.stream().mapToDouble(a -> Math.pow(a - meanActual, 2)).sum();
        if (totalVariance == 0) return 1; //  Avoid division by zero (perfect correlation)

        final double explainedVariance = IntStream.range(0, actual.size())
                .mapToDouble(i -> Math.pow(actual.get(i) - predicted.get(i), 2))
                .sum();

        return 1 - (explainedVariance / totalVariance);
    }

    //  Extracted Helper Method for Showing Messages
    private void showMessage(final String message, final String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void plotResults(final List<Double> actualPrices,final List<Double> predictedPrices) {
        final List<Integer> days = IntStream.range(0, actualPrices.size()).boxed().toList();

        final XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title("Actual vs Predicted Prices")
                .xAxisTitle("Days")
                .yAxisTitle("Price")
                .build();

        chart.addSeries("Actual Prices", days, actualPrices);
        chart.addSeries("Predicted Prices", days, predictedPrices);

        SwingUtilities.invokeLater(() -> {
            final JFrame frame = new JFrame("Model Evaluation");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new XChartPanel<>(chart));
            frame.pack();
            frame.setVisible(true);
        });

        //System.out.println(" Model evaluation completed.");
        LOGGER.info(" Model evaluation completed.\"");
    }

    
    public ModelScore evaluateAndReturn(final String modelName, final String timeframe, final List<Double> actual,final List<Double> predicted) {
        if (actual.size() != predicted.size()) {
            throw new IllegalArgumentException("Actual and predicted sizes do not match.");
            }
        final int n = actual.size();
        double sumSqTotal = 0;
        double sumSqResidual = 0;
        double sumAbsoluteError = 0;
        final double mean = actual.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        for (int i = 0; i < n; i++) {
            final double a = actual.get(i);
            final double p = predicted.get(i);
            sumSqTotal += Math.pow(a - mean, 2);
            sumSqResidual += Math.pow(a - p, 2);
            sumAbsoluteError += Math.abs(a - p);
            }

        final double mse = sumSqResidual / n;
        final double rmse = Math.sqrt(mse);
        final double mae = sumAbsoluteError / n;
        final double rSquared = 1 - (sumSqResidual / sumSqTotal);
        final double predictedPrice = predicted.get(predicted.size() - 1); // Latest predicted value


        return new ModelScore(modelName, timeframe, rSquared, mse, rmse, mae,predictedPrice);
        }

    
   



    public void plotModelComparisonChart(final List<ModelScore> scores, final String metric,final Runnable reopenMetricDialog) {
    // Sort in logical order
    scores.sort((a, b) -> {
        final int orderA = modelOrderIndex(a.modelName);
        final int orderB = modelOrderIndex(b.modelName);
        if (orderA != orderB) 
        {
            return Integer.compare(orderA, orderB);
        }
        return Integer.compare(extractDegree(a.modelName), extractDegree(b.modelName));
    });

    final CategoryChart chart = new CategoryChartBuilder()
            .width(1000)
            .height(600)
            .title("Model Comparison – " + metric)
            .xAxisTitle("Models")
            .yAxisTitle(metric)
            .build();

    // All bars share a single x-axis label: "MODELS"
    final List<String> singleLabel = List.of("MODELS");

    /*final Color[] colors = new Color[]{
        new Color(31, 119, 180),   // blue
        new Color(255, 127, 14),   // orange
        new Color(44, 160, 44),    // green
        new Color(214, 39, 40),    // red
        new Color(148, 103, 189),  // purple
        new Color(140, 86, 75),    // brown
        new Color(227, 119, 194),  // pink
        new Color(127, 127, 127),  // gray
        new Color(188, 189, 34),   // olive
        new Color(23, 190, 207),   // teal
        new Color(255, 152, 150),  // salmon
        new Color(174, 199, 232)   // sky blue
    };
    */
    final Color[] colors = {
        new Color(31, 119, 180),
        new Color(255, 127, 14),
        new Color(44, 160, 44),
        new Color(214, 39, 40),
        new Color(148, 103, 189),
        new Color(140, 86, 75),
        new Color(227, 119, 194),
        new Color(127, 127, 127),
        new Color(188, 189, 34),
        new Color(23, 190, 207),
        new Color(255, 152, 150),
        new Color(174, 199, 232)
    };

    // Select metric values
    final List<Double> values = switch (metric.toLowerCase()) {
        case "r²", "r2" -> scores.stream().map(score -> score.rSquared).toList();
        case "mse" -> scores.stream().map(score -> score.mse).toList();
        case "rmse" -> scores.stream().map(score -> score.rmse).toList();
        case "mae" -> scores.stream().map(score -> score.mae).toList();
        case "predicted" -> scores.stream().map(score -> score.predictedPrice).toList();
        default -> throw new IllegalArgumentException("Unsupported metric: " + metric);
    };

    // Add each model as a separate bar in the "MODELS" group
    for (int i = 0; i < scores.size(); i++) {
        chart.addSeries(scores.get(i).modelName, singleLabel, List.of(values.get(i)))
             .setFillColor(colors[i % colors.length]);
    }

    // Chart styling
    chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
    chart.getStyler().setLegendVisible(true);
    chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);
    chart.getStyler().setXAxisLabelRotation(0);
    chart.getStyler().setToolTipsEnabled(true);
    chart.getStyler().setToolTipType(Styler.ToolTipType.xAndYLabels);
    chart.getStyler().setToolTipFont(new Font("SansSerif", Font.PLAIN, 12));
    chart.getStyler().setAxisTickLabelsFont(new Font("SansSerif", Font.PLAIN, 11));
    chart.getStyler().setPlotGridVerticalLinesVisible(false);
    chart.getStyler().setPlotGridHorizontalLinesVisible(true);
    chart.getStyler().setXAxisTicksVisible(false); // Removes repeated tick labels

    // Show chart
    SwingUtilities.invokeLater(() -> {
        final JFrame frame = new JFrame("Model Comparison Chart");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new XChartPanel<>(chart));
        frame.pack();
        frame.setVisible(true);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (reopenMetricDialog != null) reopenMetricDialog.run();
            }
        });
    });
}


    private int modelOrderIndex(final String modelName) {
    if (modelName.startsWith("LinearRegression")) return 1;
    if (modelName.startsWith("MultiFeatureLinearRegression")) return 2;
    if (modelName.startsWith("PolynomialRegression")) return 3;
    if (modelName.startsWith("MultivariatePolyRegression")) return 4;
    if (modelName.startsWith("RidgeRegression")) return 5;
    if (modelName.startsWith("LassoRegression")) return 6;
    return 99; // Unknown model
}

    private int extractDegree(final String modelName) {
    try {
        final int idx = modelName.indexOf("deg=");
        if (idx != -1) {
            return Integer.parseInt(modelName.substring(idx + 4).replaceAll("[^0-9]", ""));
        }
    } catch (NumberFormatException ignored) {}
    return 0;
}


}

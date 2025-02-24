package com.sdm;

import javax.swing.*;
import org.knowm.xchart.*;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

public class ModelEvaluation {
    
    public void evaluate(List<Double> actualPrices, List<Double> predictedPrices) {
        if (!isValidData(actualPrices, predictedPrices)) return; //  Check validity

        System.out.println(" Evaluating Model...");

        double mse = calculateMSE(actualPrices, predictedPrices);
        double rSquared = calculateRSquared(actualPrices, predictedPrices);

        String message = String.format(" Model Evaluation:\nMSE: %.4f\nR² Score: %.4f", mse, rSquared);
        showMessage(message, "Model Evaluation");

        plotResults(actualPrices, predictedPrices);
    }

    // Check if lists are valid
    public boolean isValidData(List<Double> actual, List<Double> predicted) {
        if (actual == null || predicted == null || actual.isEmpty() || predicted.isEmpty()) {
            showMessage(" Error: Price lists cannot be empty!", "Error");
            return false;
        }

        if (actual.size() != predicted.size()) {
            showMessage(" Error: Actual and predicted lists must have the same length!", "Error");
            return false;
        }
        return true;
    }

    public double calculateMSE(List<Double> actual, List<Double> predicted) {
        return IntStream.range(0, actual.size())
                .mapToDouble(i -> Math.pow(actual.get(i) - predicted.get(i), 2))
                .average().orElse(Double.NaN); //  Returns NaN if empty
    }

    public double calculateRSquared(List<Double> actual, List<Double> predicted) {
        OptionalDouble meanActualOpt = actual.stream().mapToDouble(a -> a).average();
        if (meanActualOpt.isEmpty()) return 1; //  Perfect fit if no variance

        double meanActual = meanActualOpt.getAsDouble();
        double totalVariance = actual.stream().mapToDouble(a -> Math.pow(a - meanActual, 2)).sum();
        if (totalVariance == 0) return 1; //  Avoid division by zero (perfect correlation)

        double explainedVariance = IntStream.range(0, actual.size())
                .mapToDouble(i -> Math.pow(actual.get(i) - predicted.get(i), 2))
                .sum();

        return 1 - (explainedVariance / totalVariance);
    }

    //  Extracted Helper Method for Showing Messages
    private void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void plotResults(List<Double> actualPrices, List<Double> predictedPrices) {
        List<Integer> days = IntStream.range(0, actualPrices.size()).boxed().toList();

        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title("Actual vs Predicted Prices")
                .xAxisTitle("Days")
                .yAxisTitle("Price")
                .build();

        chart.addSeries("Actual Prices", days, actualPrices);
        chart.addSeries("Predicted Prices", days, predictedPrices);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Model Evaluation");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new XChartPanel<>(chart));
            frame.pack();
            frame.setVisible(true);
        });

        System.out.println(" Model evaluation completed.");
    }
}

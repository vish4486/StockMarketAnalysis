package com.sdm.service;

import java.util.List;
import java.util.ArrayList;
import com.sdm.utils.LinearAlgebraUtils;
import com.sdm.model.PredictionModel;
import com.sdm.utils.PolynomialFeatureExpander;


@SuppressWarnings({"PMD.ShortVariable","PMD.LongVariable"})
final public class CrossValidator {

    private CrossValidator() {
        throw new UnsupportedOperationException("Utility class");
    }
    

    public static double crossValidateR2(final List<double[]> features, final List<Double> targets, final int degree, final int kFolds) {
       // List<double[]> expanded = PolynomialFeatureExpander.expand(features, degree);
        final List<double[]> expanded = new PolynomialFeatureExpander(degree).expand(features);

        return crossValidateModel(new LinearRegressionOnExpandedFeatures(expanded), expanded, targets, kFolds);
    }

    public static double crossValidateModel(final PredictionModel model, final List<double[]> features,final  List<Double> targets,final int kFolds) {
        final int sampleCount = features.size();
        final int foldSize = sampleCount / kFolds;
        double totalRSquared = 0;

        for (int i = 0; i < kFolds; i++) {
            final int start = i * foldSize;
            final int end = (i + 1 == kFolds) ? sampleCount : (i + 1) * foldSize;

            final List<double[]> testX = features.subList(start, end);
            final List<Double> testY = targets.subList(start, end);

            final List<double[]> trainX = new ArrayList<>(features);
            final List<Double> trainY = new ArrayList<>(targets);
            trainX.subList(start, end).clear();
            trainY.subList(start, end).clear();

            model.train(trainX, trainY);

            final List<Double> predicted = new ArrayList<>();
            for (final double[] x : testX) {
                predicted.add(model.predict(x));
            }

            totalRSquared += rSquared(testY, predicted);
        }

        return totalRSquared / kFolds;
    }

    public static double rSquared(final List<Double> actual, final List<Double> predicted) {
        final double mean = actual.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < actual.size(); i++) {
            final double a = actual.get(i);
            final double p = predicted.get(i);
            ssTot += Math.pow(a - mean, 2);
            ssRes += Math.pow(a - p, 2);
        }
        return 1 - (ssRes / ssTot);
    }

    // Helper model for internal CROSS VALIDATION
    @SuppressWarnings("PMD.UnusedPrivateField")
    private static class LinearRegressionOnExpandedFeatures implements PredictionModel {
        private final List<double[]> preparedFeatures;
        private double[] weights;

        public LinearRegressionOnExpandedFeatures(final List<double[]> preparedFeatures) {
            this.preparedFeatures = preparedFeatures;
        }

        @Override
        public String getName() {
            return "InternalCVLinearRegression";
        }

        @Override
        public boolean supportsMultivariate() {
            return true;
        }

        @Override
        public boolean supportsUnivariate() {
            return false;
        }

        @Override
        public void train(final List<double[]> features,final List<Double> targets) {
            //weights = LinearAlgebraUtils.fitLeastSquares(features, targets);
            weights = LinearAlgebraUtils.fitLeastSquares(features.toArray(new double[0][]),targets);
             }

        @Override
        public double predict(final double[] features) {
            return LinearAlgebraUtils.dot(weights, features);
        }
    }
}

package com.sdm.service;

import java.util.List;
import java.util.ArrayList;
import com.sdm.utils.LinearAlgebraUtils;
import com.sdm.model.PredictionModel;
import com.sdm.utils.PolynomialFeatureExpander;



public class CrossValidator {

    public static double crossValidateR2(List<double[]> features, List<Double> targets, int degree, int kFolds) {
       // List<double[]> expanded = PolynomialFeatureExpander.expand(features, degree);
        List<double[]> expanded = new PolynomialFeatureExpander(degree).expand(features);

        return crossValidateModel(new LinearRegressionOnExpandedFeatures(expanded), expanded, targets, kFolds);
    }

    public static double crossValidateModel(PredictionModel model, List<double[]> features, List<Double> targets, int kFolds) {
        int n = features.size();
        int foldSize = n / kFolds;
        double totalRSquared = 0;

        for (int i = 0; i < kFolds; i++) {
            int start = i * foldSize;
            int end = (i + 1 == kFolds) ? n : (i + 1) * foldSize;

            List<double[]> testX = features.subList(start, end);
            List<Double> testY = targets.subList(start, end);

            List<double[]> trainX = new ArrayList<>(features);
            List<Double> trainY = new ArrayList<>(targets);
            trainX.subList(start, end).clear();
            trainY.subList(start, end).clear();

            model.train(trainX, trainY);

            List<Double> predicted = new ArrayList<>();
            for (double[] x : testX) {
                predicted.add(model.predict(x));
            }

            totalRSquared += rSquared(testY, predicted);
        }

        return totalRSquared / kFolds;
    }

    public static double rSquared(List<Double> actual, List<Double> predicted) {
        double mean = actual.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < actual.size(); i++) {
            double a = actual.get(i);
            double p = predicted.get(i);
            ssTot += Math.pow(a - mean, 2);
            ssRes += Math.pow(a - p, 2);
        }
        return 1 - (ssRes / ssTot);
    }

    // Helper model for internal CROSS VALIDATION
    static class LinearRegressionOnExpandedFeatures implements PredictionModel {
        private final List<double[]> preparedFeatures;
        private double[] weights;

        public LinearRegressionOnExpandedFeatures(List<double[]> preparedFeatures) {
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
        public void train(List<double[]> features, List<Double> targets) {
            //weights = LinearAlgebraUtils.fitLeastSquares(features, targets);
            weights = LinearAlgebraUtils.fitLeastSquares(features.toArray(new double[0][]),targets);
             }

        @Override
        public double predict(double[] features) {
            return LinearAlgebraUtils.dot(weights, features);
        }
    }
}

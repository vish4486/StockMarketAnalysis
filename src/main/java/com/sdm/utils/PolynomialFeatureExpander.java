package com.sdm.utils;

import java.util.ArrayList;
import java.util.List;

public class PolynomialFeatureExpander {
    private final int degree;

    public PolynomialFeatureExpander(int degree) {
        this.degree = degree;
    }

    public List<double[]> expand(List<double[]> inputs) {
        List<double[]> expanded = new ArrayList<>();
        for (double[] input : inputs) {
            expanded.add(expandSingle(input));
        }
        return expanded;
    }

    public double[] expandSingle(double[] input) {
        List<Double> terms = new ArrayList<>();
        generateTerms(input, new double[input.length], 0, degree, terms);
        return terms.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private void generateTerms(double[] input, double[] powers, int pos, int degreeLeft, List<Double> terms) {
        if (pos == input.length) {
            double product = 1.0;
            for (int i = 0; i < input.length; i++) {
                product *= Math.pow(input[i], powers[i]);
            }
            terms.add(product);
            return;
        }

        for (int d = 0; d <= degreeLeft; d++) {
            powers[pos] = d;
            generateTerms(input, powers, pos + 1, degreeLeft - d, terms);
        }
    }
}

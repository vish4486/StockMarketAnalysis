package com.sdm.utils;

import java.util.ArrayList;
import java.util.List;


/**
 * this class Expands a vector of input features into polynomial feature combinations
 * up to a specified total degree.
 * 
 * For example, input [x1, x2] with degree 2 will produce terms like:
 * [1, x1, x2, x1^2, x1*x2, x2^2]
 */
public class PolynomialFeatureExpander {
    private final int degree;  // Max degree for polynomial expansion

    public PolynomialFeatureExpander(final int degree) {
        this.degree = degree;
    }

    
    /**
     * Expands an entire list of input vectors into polynomial features.
     * @param inputs List of input feature arrays
     * @return List of expanded polynomial feature arrays
     */
    public List<double[]> expand(final List<double[]> inputs) {
        final List<double[]> expanded = new ArrayList<>();
        for (final double[] input : inputs) {
            expanded.add(expandSingle(input));
        }
        return expanded;
    }

    
    /**
     * Expands a single input feature vector to all polynomial combinations
     * up to the given degree.
     * 
     * Example:
     * input = [x, y], degree = 2
     * returns: [1, x, y, x^2, xy, y^2]
     */
    public double[] expandSingle(final double[] input) {
        final List<Double> terms = new ArrayList<>();
        generateTerms(input, new double[input.length], 0, degree, terms);
        return terms.stream().mapToDouble(Double::doubleValue).toArray();
    }

    
    /**
     * Recursively generates all combinations of exponents such that the
     * total degree of the term does not exceed the max degree.
     * 
     * Example:
     * For input = [x1, x2], and degree = 2,
     * it will generate combinations like [0,0], [1,0], [0,1], [1,1], [2,0], [0,2]
     */
    private void generateTerms(final double[] input, final double[] powers, final int pos, final int degreeLeft, final List<Double> terms) {
        if (pos == input.length) {
            // Compute the product of input[i]^powers[i] for all i
            double product = 1.0;
            for (int i = 0; i < input.length; i++) {
                product *= Math.pow(input[i], powers[i]);
            }
            terms.add(product);
            return;
        }

        // to Try all power combinations for current variable (from 0 to remaining degree)
        for (int d = 0; d <= degreeLeft; d++) {
            powers[pos] = d;
            generateTerms(input, powers, pos + 1, degreeLeft - d, terms);
        }
    }
}

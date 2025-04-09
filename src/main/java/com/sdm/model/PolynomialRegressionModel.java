package com.sdm.model;
import com.sdm.utils.LinearAlgebraUtils;
import java.util.List;


/**
 * Implements polynomial regression for univariate data (e.g., predicting price over time).
 * The model fits a polynomial of configurable degree using least squares.
 */
@SuppressWarnings({"PMD.ShortVariable","PMD.LongVariable","PMD.AssignmentToNonFinalStatic"})
public class PolynomialRegressionModel implements PredictionModel {
    private double[] coefficients;  // Fitted polynomial coefficients: [a0, a1, ..., an]
    private final int degree;       // Degree of the polynomial
    private boolean trained = false;   // Flag to ensure model is trained before prediction

    // Used to give each model instance a unique ID for easier tracking/debugging
    private final int modelId; 
    private static int counter = 1;

     /**
     * Constructor: specifies the polynomial degree.
     *
     * @param degree The degree of the polynomial to be fit.
     */
    public PolynomialRegressionModel(final int degree) {
        this.degree = degree;
        this.modelId = counter++;
    }

    @Override
    public String getName() {
        return "PolynomialRegression (deg=" + degree + ") #" + modelId;
    }

    @Override
    public boolean supportsUnivariate() {
        return true;
    }

    @Override
    public boolean supportsMultivariate() {
        return false;
    }

    
    /**
     * Trains the model on a sequence of prices using polynomial curve fitting.
     * X values are implicitly 0, 1, 2, ..., n-1 representing time steps.
     *
     * @param prices Historical price data to learn from.
     */
    @Override
    public void train(final List<Double> prices) {
        final int sampleCount = prices.size();
        final double[][] xMatrix = new double[sampleCount][degree + 1];
        final double[][] yMatrix = new double[sampleCount][1];

        // Prepare input matrix X (with powers of i) and output vector Y
        for (int i = 0; i < sampleCount; i++) {
            final double xVal = i;
            for (int j = 0; j <= degree; j++) {
                xMatrix[i][j] = Math.pow(xVal, j);
            }
            yMatrix[i][0] = prices.get(i);
        }

       
       // Apply normal equation: θ = (XᵀX)⁻¹ XᵀY and compute using Utility class methods
       final double[][] xT = LinearAlgebraUtils.transpose(xMatrix);
       final double[][] xTx = LinearAlgebraUtils.multiply(xT, xMatrix);
       final double[][] xTxInv = LinearAlgebraUtils.invert(xTx);
       final double[][] xTy = LinearAlgebraUtils.multiply(xT, yMatrix);
       final double[][] theta = LinearAlgebraUtils.multiply(xTxInv, xTy);


       // Store resulting coefficients
       coefficients = new double[degree + 1];
        for (int i = 0; i <= degree; i++) {
            coefficients[i] = theta[i][0];
        }

        trained = true;
    }

    
    /**
     * Predicts the next price in sequence based on trained polynomial model.
     * Uses the next time index (i.e., length of coefficients) for prediction.
     *
     * @return The predicted future price.
     */
    @Override
    public double predictNext() {
        if (!trained) 
        {
            throw new IllegalStateException("Model is not trained");
        }
        final double xVal = coefficients.length; // Predict next index
        double result = 0;
        for (int i = 0; i < coefficients.length; i++) {
            result += coefficients[i] * Math.pow(xVal, i);
        }
        return result;
    }
}

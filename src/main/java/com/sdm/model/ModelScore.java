package com.sdm.model;


/**
 * this class represents the evaluation metrics for a predictive model
 * after training and forecasting on stock data.
 * 
 * This class is used to store and display the performance 
 * of a model in terms of several key metrics.
 */
public class ModelScore {
    public final String modelName;
    public final String timeframe;
    public final double mse;
    public final double rSquared;
    public final double mae;  
    public final double rmse;  
    public final double predictedPrice;

/**
     * Constructs a new ModelScore object with all key evaluation metrics.
     *
     * @param modelName       Name of the model used
     * @param timeframe       Timeframe of prediction
     * @param rSquared        R² value
     * @param mse             Mean Squared Error
     * @param rmse            Root Mean Squared Error
     * @param mae             Mean Absolute Error
     * @param predictedPrice  The price predicted by the model
     */
public ModelScore(final String modelName, final String timeframe, final double rSquared,final double mse, final double rmse, final double mae, final double predictedPrice)
     {

        this.modelName = modelName;
        this.timeframe = timeframe;
        this.rSquared = rSquared;
        this.mse = mse;
        this.rmse = rmse;
        this.mae = mae;
        this.predictedPrice= predictedPrice;
    }


/**
* Returns a well-formatted summary of the model's evaluation.
* Useful for logging or displaying in GUI dialogs.
*/    
@Override
public String toString() {
    return String.format("%s [%s] ➤ R²: %.4f | MSE: %.4f | RMSE: %.4f | MAE: %.4f | Predicted: %.2f",modelName, timeframe, rSquared, mse, rmse, mae, predictedPrice);
}
    
public double getPredictedPrice() {
    return predictedPrice;
}

//accessors
public String getModelName() { return modelName; }
public String getTimeframe() { return timeframe; }
public double getRSquared() { return rSquared; }
public double getMSE() { return mse; }
public double getRMSE() { return rmse; }
public double getMAE() { return mae; }

}

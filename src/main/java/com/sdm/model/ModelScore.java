package com.sdm.model;

public class ModelScore {
    public final String modelName;
    public final String timeframe;
    public final double mse;
    public final double rSquared;
    public final double mae;  
    public final double rmse;  
    public final double predictedPrice;




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

    @Override
    public String toString() {
        return String.format("%s [%s] ➤ R²: %.4f | MSE: %.4f | RMSE: %.4f | MAE: %.4f | Predicted: %.2f",modelName, timeframe, rSquared, mse, rmse, mae, predictedPrice);
    }
    
    public double getPredictedPrice() {
        return predictedPrice;
        }

}

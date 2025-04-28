package com.sdm.controller;

/**
 * ViewListener acts as a callback interface between the controller and the view (App).
 * It allows the controller to notify the view when certain operations are completed,
 * for example, after prediction or evaluation is done.
 */
public interface ViewListener {

    /**
     * Called when a prediction is successfully completed.
     *
     * @param symbol Stock symbol predicted
     * @param timeframe Timeframe used
     * @param predictedPrice Predicted price value
     */
    void onPredictionCompleted(double predictedPrice);

    /**
     * Called by the controller when a model evaluation has been completed successfully.
     */
    void onEvaluationCompleted();
}

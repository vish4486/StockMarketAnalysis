package com.sdm.view.builder;

import javax.swing.*;
import java.awt.event.ActionListener;

public class ActionButtonPanelBuilder {

    private final JButton saveButton, predictButton, evaluateButton, chartButton, refreshButton;
    private final ActionListener saveAction, predictAction, evaluateAction, chartAction, refreshAction;

    public ActionButtonPanelBuilder(JButton saveButton, JButton predictButton, JButton evaluateButton, JButton chartButton, JButton refreshButton,
                                    ActionListener saveAction, ActionListener predictAction, ActionListener evaluateAction, ActionListener chartAction, ActionListener refreshAction) {
        this.saveButton = saveButton;
        this.predictButton = predictButton;
        this.evaluateButton = evaluateButton;
        this.chartButton = chartButton;
        this.refreshButton = refreshButton;
        this.saveAction = saveAction;
        this.predictAction = predictAction;
        this.evaluateAction = evaluateAction;
        this.chartAction = chartAction;
        this.refreshAction = refreshAction;
    }

    public JPanel build() {
        JPanel panel = new JPanel();
        saveButton.addActionListener(saveAction);
        predictButton.addActionListener(predictAction);
        evaluateButton.addActionListener(evaluateAction);
        chartButton.addActionListener(chartAction);
        refreshButton.addActionListener(refreshAction);

        panel.add(saveButton);
        panel.add(predictButton);
        panel.add(evaluateButton);
        panel.add(chartButton);
        panel.add(refreshButton);
        return panel;
    }
}

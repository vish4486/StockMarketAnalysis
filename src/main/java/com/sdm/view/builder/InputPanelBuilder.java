package com.sdm.view.builder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class InputPanelBuilder {

    private final JTextField symbolField;
    private final JComboBox<String> timeframeDropdown;
    private final JButton fetchButton;
    private final ActionListener fetchAction;

    public InputPanelBuilder(JTextField symbolField, JComboBox<String> timeframeDropdown, JButton fetchButton, ActionListener fetchAction) {
        this.symbolField = symbolField;
        this.timeframeDropdown = timeframeDropdown;
        this.fetchButton = fetchButton;
        this.fetchAction = fetchAction;
    }

    public JPanel build() {
        JPanel inputPanel = new JPanel();
        symbolField.setColumns(10);
        fetchButton.addActionListener(fetchAction);

        inputPanel.add(new JLabel("Stock Symbol:"));
        inputPanel.add(symbolField);
        inputPanel.add(new JLabel("Timeframe:"));
        inputPanel.add(timeframeDropdown);
        inputPanel.add(fetchButton);

        return inputPanel;
    }
}

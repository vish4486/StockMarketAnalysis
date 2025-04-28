package com.sdm.view.builder;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Locale;

public class AutoCompleteHandler {

    private final JTextField textField;
    private final List<String> stockSymbols;

    public AutoCompleteHandler(JTextField textField, List<String> stockSymbols) {
        this.textField = textField;
        this.stockSymbols = stockSymbols;
    }

    public void enable() {
        JPopupMenu popupMenu = new JPopupMenu();
        JList<String> suggestionList = new JList<>();

        popupMenu.setFocusable(false);
        popupMenu.setPopupSize(300, 150);
        popupMenu.add(new JScrollPane(suggestionList));

        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateSuggestions(); }
            public void removeUpdate(DocumentEvent e) { updateSuggestions(); }
            public void changedUpdate(DocumentEvent e) { updateSuggestions(); }

            private void updateSuggestions() {
                SwingUtilities.invokeLater(() -> {
                    String input = normalizeInput(textField.getText());
                    if (input.isEmpty()) {
                        popupMenu.setVisible(false);
                        return;
                    }
                    List<String> filtered = stockSymbols.stream()
                            .filter(name -> name.toUpperCase(Locale.ROOT).contains(input))
                            .limit(10)
                            .toList();
                    if (filtered.isEmpty()) {
                        popupMenu.setVisible(false);
                        return;
                    }
                    suggestionList.setListData(filtered.toArray(new String[0]));
                    suggestionList.setVisibleRowCount(Math.min(filtered.size(), 10));
                    popupMenu.show(textField, 0, textField.getHeight());
                });
            }
        });

        textField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (popupMenu.isVisible() && e.getKeyCode() == KeyEvent.VK_DOWN) {
                    suggestionList.setSelectedIndex(0);
                    suggestionList.requestFocus();
                }
            }
        });

        suggestionList.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    applyAutoComplete(suggestionList.getSelectedValue());
                }
            }
        });

        suggestionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                applyAutoComplete(suggestionList.getSelectedValue());
            }
        });
    }

    private String normalizeInput(String input) {
        return input == null ? "" : input.toUpperCase(Locale.ROOT).trim();
    }

    
    private void applyAutoComplete(String selected) {
        if (selected != null) {
            String symbol = selected.contains(" - ") 
                ? selected.substring(0, selected.indexOf(" - ")).trim()
                : selected.trim();
            textField.setText(symbol);
            textField.requestFocus();
        }
    }
    
}

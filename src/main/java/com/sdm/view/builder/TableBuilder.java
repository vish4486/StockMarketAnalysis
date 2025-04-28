package com.sdm.view.builder;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TableBuilder {

    public JTable build(DefaultTableModel model) {
        JTable table = new JTable(model);
        return table;
    }
}

package com.trophonix.txt;

import javax.swing.*;
import java.awt.*;

/**
 * Created by lucas on 3/31/17.
 */

import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JTextArea;
import java.util.List;

public class FontChooser extends JPanel {

    JLabel lbFontFamilyLabel;
    JLabel lbTypefaceLabel;
    JTextField tfFontFamilyField;
    JList lsFontFamilyList;
    JList lsTypefaceList;
    JLabel lbFontSizeLabel;
    JList lsList3;
    JTextArea taPreviewArea;
    JLabel lbPreviewLabel;
    JButton btBut0;
    JButton btBut1;

    Font font = null;

    private TrophonixTXT main;
    private JFrame frame;

    public FontChooser(TrophonixTXT main, JFrame frame, Font font) {
        this.main = main;
        this.frame = frame;
        this.font = font;

        GridBagLayout gbPanel = new GridBagLayout();
        GridBagConstraints gbcPanel = new GridBagConstraints();
        setLayout(gbPanel);

        lbFontFamilyLabel = new JLabel("Font Family");

        gbcPanel.gridx = 1;
        gbcPanel.gridy = 1;
        gbcPanel.gridwidth = 9;
        gbcPanel.gridheight = 1;
        gbcPanel.fill = GridBagConstraints.BOTH;
        gbcPanel.weightx = 1;
        gbcPanel.weighty = 1;
        gbcPanel.anchor = GridBagConstraints.NORTH;
        gbPanel.setConstraints(lbFontFamilyLabel, gbcPanel);
        add(lbFontFamilyLabel);

        lbTypefaceLabel = new JLabel("Typeface");

        gbcPanel.gridx = 11;
        gbcPanel.gridy = 1;
        gbcPanel.gridwidth = 9;
        gbcPanel.gridheight = 1;
        gbcPanel.fill = GridBagConstraints.BOTH;
        gbcPanel.weightx = 1;
        gbcPanel.weighty = 1;
        gbcPanel.anchor = GridBagConstraints.NORTH;
        gbPanel.setConstraints(lbTypefaceLabel, gbcPanel);
        add(lbTypefaceLabel);

        tfFontFamilyField = new JTextField();
        tfFontFamilyField.setEditable(false);

        gbcPanel.gridx = 1;
        gbcPanel.gridy = 2;
        gbcPanel.gridwidth = 9;
        gbcPanel.gridheight = 1;
        gbcPanel.fill = GridBagConstraints.BOTH;
        gbcPanel.weightx = 1;
        gbcPanel.weighty = 0;
        gbcPanel.anchor = GridBagConstraints.NORTH;
        gbPanel.setConstraints(tfFontFamilyField, gbcPanel);
        add(tfFontFamilyField);

        Font[] systemFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        String[] dataFontFamilyList = new String[systemFonts.length];
        for (int i = 0; i < systemFonts.length; i++) {
            dataFontFamilyList[i] = systemFonts[i].getFamily();
        }
        lsFontFamilyList = new JList(dataFontFamilyList);
        lsFontFamilyList.addListSelectionListener(event -> {
            makeFont();
            tfFontFamilyField.setText(lsFontFamilyList.getSelectedValue().toString());
        });
        lsFontFamilyList.setSelectedValue(font.getFamily(), true);


        gbcPanel.gridx = 1;
        gbcPanel.gridy = 3;
        gbcPanel.gridwidth = 9;
        gbcPanel.gridheight = 9;
        gbcPanel.fill = GridBagConstraints.BOTH;
        gbcPanel.weightx = 1;
        gbcPanel.weighty = 1;
        gbcPanel.anchor = GridBagConstraints.NORTH;
        gbPanel.setConstraints(lsFontFamilyList, gbcPanel);
        add(new JScrollPane(lsFontFamilyList), gbcPanel);

        String[] dataTypefaceList = {"Plain", "Bold", "Italic", "Bold Italic"};
        lsTypefaceList = new JList(dataTypefaceList);
        lsTypefaceList.addListSelectionListener(event -> makeFont());
        lsTypefaceList.setSelectedValue(font.getStyle(), true);

        gbcPanel.gridx = 11;
        gbcPanel.gridy = 2;
        gbcPanel.gridwidth = 9;
        gbcPanel.gridheight = 4;
        gbcPanel.fill = GridBagConstraints.BOTH;
        gbcPanel.weightx = 1;
        gbcPanel.weighty = 1;
        gbcPanel.anchor = GridBagConstraints.NORTH;
        gbPanel.setConstraints(lsTypefaceList, gbcPanel);
        add(lsTypefaceList);

        lbFontSizeLabel = new JLabel("Size");

        gbcPanel.gridx = 11;
        gbcPanel.gridy = 7;
        gbcPanel.gridwidth = 9;
        gbcPanel.gridheight = 1;
        gbcPanel.fill = GridBagConstraints.BOTH;
        gbcPanel.weightx = 1;
        gbcPanel.weighty = 1;
        gbcPanel.anchor = GridBagConstraints.NORTH;
        gbPanel.setConstraints(lbFontSizeLabel, gbcPanel);
        add(lbFontSizeLabel);

        List<Integer> dataList3 = new ArrayList<>();
        for (int i = 6; i < 73; i += 2) dataList3.add(i);
        lsList3 = new JList(dataList3.toArray());
        lsList3.addListSelectionListener(event -> makeFont());
        lsList3.setSelectedValue(font.getSize(), true);

        gbcPanel.gridx = 11;
        gbcPanel.gridy = 8;
        gbcPanel.gridwidth = 9;
        gbcPanel.gridheight = 4;
        gbcPanel.fill = GridBagConstraints.BOTH;
        gbcPanel.weightx = 1;
        gbcPanel.weighty = 1;
        gbcPanel.anchor = GridBagConstraints.NORTH;
        gbPanel.setConstraints(lsList3, gbcPanel);
        add(new JScrollPane(lsList3), gbcPanel);

        taPreviewArea = new JTextArea(2, 10);
        taPreviewArea.setText("The quick brown fox jumps over the lazy dog");

        gbcPanel.gridx = 1;
        gbcPanel.gridy = 14;
        gbcPanel.gridwidth = 19;
        gbcPanel.gridheight = 7;
        gbcPanel.fill = GridBagConstraints.BOTH;
        gbcPanel.weightx = 1;
        gbcPanel.weighty = 1;
        gbcPanel.anchor = GridBagConstraints.NORTH;
        gbPanel.setConstraints(taPreviewArea, gbcPanel);
        add(taPreviewArea);

        lbPreviewLabel = new JLabel("Preview");

        gbcPanel.gridx = 1;
        gbcPanel.gridy = 13;
        gbcPanel.gridwidth = 19;
        gbcPanel.gridheight = 1;
        gbcPanel.fill = GridBagConstraints.BOTH;
        gbcPanel.weightx = 1;
        gbcPanel.weighty = 1;
        gbcPanel.anchor = GridBagConstraints.NORTH;
        gbPanel.setConstraints(lbPreviewLabel, gbcPanel);
        add(lbPreviewLabel);

        btBut0 = new JButton( "Save Font"  );
        gbcPanel.gridx = 1;
        gbcPanel.gridy = 22;
        gbcPanel.gridwidth = 9;
        gbcPanel.gridheight = 2;
        gbcPanel.fill = GridBagConstraints.BOTH;
        gbcPanel.weightx = 1;
        gbcPanel.weighty = 0;
        gbcPanel.anchor = GridBagConstraints.NORTH;
        gbPanel.setConstraints( btBut0, gbcPanel );
        add( btBut0);
        btBut0.addActionListener(event -> {
            makeFont();
            if (font != null) main.setFont(font);
            frame.setVisible(false);
            frame.dispose();
        });

        btBut1 = new JButton( "Cancel"  );
        gbcPanel.gridx = 11;
        gbcPanel.gridy = 22;
        gbcPanel.gridwidth = 9;
        gbcPanel.gridheight = 2;
        gbcPanel.fill = GridBagConstraints.BOTH;
        gbcPanel.weightx = 1;
        gbcPanel.weighty = 0;
        gbcPanel.anchor = GridBagConstraints.NORTH;
        gbPanel.setConstraints( btBut1, gbcPanel );
        add( btBut1 );
        btBut1.addActionListener(event -> {
            frame.setVisible(false);
            frame.dispose();
        });
    }

    private void makeFont() {
        try {
            font = new Font(lsFontFamilyList.getSelectedValue().toString(), lsTypefaceList.getSelectedIndex(), Integer.parseInt(lsList3.getSelectedValue().toString()));
            taPreviewArea.setFont(font);
        } catch (Exception ex) {

        }
    }

}
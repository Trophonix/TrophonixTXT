package com.trophonix.editor;

import com.sun.java.swing.action.ExitAction;
import javafx.stage.FileChooser;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.file.Files;

/**
 * Created by Lucas on 3/31/17.
 */
public class TEditor extends JFrame {

    private File currentDirectory = new File(".");
    private File currentFile;

    private JTextArea textArea = new JTextArea();
    private JScrollPane textPane = new JScrollPane(textArea);

    public TEditor() {
        super("Trophonix Editor");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException ex) {
            ex.printStackTrace();
        }
        Dimension size = new Dimension(640, 480);

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenuItem newItem = new JMenuItem("[N]ew", KeyEvent.VK_N);
        newItem.addActionListener(event -> textArea.setText(""));
        fileMenu.add(newItem);

        JMenuItem openItem = new JMenuItem("[O]pen", KeyEvent.VK_O);
        openItem.addActionListener(event -> openFileChooser());
        fileMenu.add(openItem);

        JMenuItem exitItem = new JMenuItem("E[x]it", KeyEvent.VK_X);
        exitItem.addActionListener(event -> dispose());
        fileMenu.add(exitItem);

        JMenuItem saveItem = new JMenuItem("[S]ave", KeyEvent.VK_S);
        saveItem.addActionListener(event -> openFileSaver());
        fileMenu.add(saveItem);

        setJMenuBar(menuBar);

        add(textPane);
        pack();
        setSize(size);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
    }

    private void openFileChooser() {
        JFrame fileFrame = new JFrame("Choose a File");
        JFileChooser chooser = new JFileChooser();
        int input = chooser.showOpenDialog(fileFrame);
        if (input == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                try {
                    textArea.setText("");
                    Files.readAllLines(file.toPath()).forEach(line -> textArea.append(line + "\n"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        fileFrame.setVisible(false);
        fileFrame.dispose();
    }

    private void openFileSaver() {}

    public static void main(String[] args) {
        new TEditor();
    }

}

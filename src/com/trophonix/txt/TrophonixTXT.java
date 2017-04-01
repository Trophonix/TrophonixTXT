package com.trophonix.txt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.nio.file.Files;

/**
 * Created by Lucas on 3/31/17.
 */
public class TrophonixTXT extends JFrame {

    private static final String TITLE = "TrophonixTXT v1.1";

    private File currentDirectory = new File(".");
    private File currentFile = null;

    private JTextArea textArea = new JTextArea();
    private JScrollPane textPane = new JScrollPane(textArea);

    private String lastSaved;

    public TrophonixTXT() {
        super(TITLE + " (New File)");
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
        newItem.addActionListener(event -> {
            if (confirmClose()) {
                textArea.setText("");
                setTitle(TITLE + " (New File)");
            }
        });
        fileMenu.add(newItem);

        JMenuItem openItem = new JMenuItem("[O]pen", KeyEvent.VK_O);
        openItem.addActionListener(event -> openFileChooser());
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem("[S]ave", KeyEvent.VK_A);
        saveItem.addActionListener(event -> {
            if (currentFile == null || !currentFile.exists()) {
                openFileSaver();
            } else {
                saveCurrentFile();
            }
        });
        fileMenu.add(saveItem);

        JMenuItem saveAsItem = new JMenuItem("Save [A]s", KeyEvent.VK_S);
        saveAsItem.addActionListener(event -> openFileSaver());
        fileMenu.add(saveAsItem);

        JMenuItem exitItem = new JMenuItem("E[x]it", KeyEvent.VK_X);
        exitItem.addActionListener(event -> {
            if (confirmClose()) dispose();
        });
        fileMenu.add(exitItem);

        setJMenuBar(menuBar);

        add(textPane);
        pack();
        setSize(size);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                if (!getTitle().endsWith("*)")) setTitle(getTitle().replace(")", "*)"));
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {

            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {

            }
        });
    }

    private void openFileChooser() {
        JFrame fileFrame = makeChooserFrame();
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(currentDirectory);
        int input = chooser.showOpenDialog(fileFrame);
        if (input == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                try {
                    textArea.setText("");
                    currentDirectory = file.getParentFile();
                    Files.readAllLines(file.toPath()).forEach(line -> textArea.append(line + "\n"));
                    currentFile = file;
                    setTitle(TITLE + " (" + file.getName() + ")");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        fileFrame.setVisible(false);
        fileFrame.dispose();
    }

    private void openFileSaver() {
        JFrame fileFrame = makeChooserFrame();
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(currentDirectory);
        chooser.setSelectedFile(currentFile);
        int input = chooser.showSaveDialog(fileFrame);
        if (input == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                currentFile = file;
                saveCurrentFile();
            }
        }
        fileFrame.setVisible(false);
        fileFrame.dispose();
    }

    private JFrame makeChooserFrame() {
        JFrame chooserFrame = new JFrame();
        chooserFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        chooserFrame.setVisible(true);
        return chooserFrame;
    }

    private void saveCurrentFile() {
        try {
            String name = currentFile.getName();
            if (!name.contains("."))
                name += ".txt";
            currentFile = new File(currentFile.getParent(), name);
            if (!currentFile.exists()) {
                currentFile.getParentFile().mkdirs();
                currentFile.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(currentFile, true);
            textArea.write(fileWriter);
            fileWriter.close();
            lastSaved = textArea.getText();
            setTitle(TITLE + " (" + name + ")");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean confirmClose() {
        if (!textArea.getText().isEmpty() && !textArea.getText().equals(lastSaved)) {
            JFrame chooser = makeChooserFrame();
            int input = JOptionPane.showOptionDialog(chooser, "Do you want to exit without saving?", "You Haven't Saved!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"Yes, exit", "No, I want to save!"}, (Object)"Yes, exit");
            return input == 0;
        }
        return true;
    }

    public static void main(String[] args) {
        new TrophonixTXT();
    }

}

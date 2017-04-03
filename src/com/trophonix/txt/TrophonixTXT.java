package com.trophonix.txt;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.*;
import java.util.Properties;

/**
 * Created by Lucas on 3/31/17.
 */
public class TrophonixTXT extends JFrame {

    private static final String TITLE = "TrophonixTXT v1.3 BETA";

    private File currentDirectory = new File(".");
    private File currentFile = null;

    private JTextArea textArea = new JTextArea();
    private JScrollPane textPane = new JScrollPane(textArea);

    private String lastSaved;

    private File configFile;
    private Properties config;

    public TrophonixTXT() {
        super(TITLE + " (New File)");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException ex) {
            ex.printStackTrace();
        }

        JMenuBar menuBar = new JMenuBar();

        /* <----- File Menu -----> */
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem newItem = new JMenuItem("New", KeyEvent.VK_N);
        newItem.addActionListener(event -> {
            if (confirmClose()) {
                textArea.setText("");
                setTitle(TITLE + " (New File)");
            }
        });
        fileMenu.add(newItem);

        JMenuItem openItem = new JMenuItem("Open", KeyEvent.VK_O);
        openItem.addActionListener(event -> openFileChooser());
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem("Save", KeyEvent.VK_A);
        saveItem.addActionListener(event -> {
            if (currentFile == null || !currentFile.exists()) {
                openFileSaver();
            } else {
                saveCurrentFile();
            }
        });
        fileMenu.add(saveItem);

        JMenuItem saveAsItem = new JMenuItem("Save As", KeyEvent.VK_S);
        saveAsItem.addActionListener(event -> openFileSaver());
        fileMenu.add(saveAsItem);

        JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitItem.addActionListener(event -> {
            if (confirmClose()) dispose();
        });

        /* <----- Edit Menu -----> */
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);

        JMenuItem fontItem = new JMenuItem("Font", KeyEvent.VK_F);
        fontItem.addActionListener(event -> openFontChooser());
        editMenu.add(fontItem);

        JCheckBoxMenuItem wordWrapItem = new JCheckBoxMenuItem( "Word Wrap", true);
        wordWrapItem.setMnemonic(KeyEvent.VK_W);
        wordWrapItem.addActionListener(event -> {
            boolean wordWrap = wordWrapItem.getState();
            textArea.setLineWrap(wordWrap);
            config.setProperty("wordWrap", Boolean.toString(wordWrap));
            saveConfig();
        });
        editMenu.add(wordWrapItem);

        editMenu.add(new JSeparator());

        JMenuItem selectAllItem = new JMenuItem("Select All", KeyEvent.VK_A);
        selectAllItem.addActionListener(event -> {
            textArea.requestFocusInWindow();
            textArea.selectAll();
        });
        editMenu.add(selectAllItem);

        JMenuItem copyItem = new JMenuItem("Copy", KeyEvent.VK_C);
        copyItem.addActionListener(event -> {
            StringSelection selection = new StringSelection(textArea.getSelectedText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        });
        copyItem.setEnabled(false);
        editMenu.add(copyItem);

        JMenuItem pasteItem = new JMenuItem("Paste", KeyEvent.VK_P);
        pasteItem.addActionListener(event -> {
            Transferable clipboard = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
            try {
                textArea.insert((String)clipboard.getTransferData(DataFlavor.stringFlavor), textArea.getCaretPosition());
            } catch (UnsupportedFlavorException | IOException ex) {
                ex.printStackTrace();
            }
        });
        pasteItem.setEnabled(false);
        editMenu.add(pasteItem);

        editMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent menuEvent) {
                String selection = textArea.getSelectedText();
                copyItem.setEnabled(selection != null && !selection.isEmpty());
                Transferable clipboard = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
                pasteItem.setEnabled(clipboard != null && clipboard.isDataFlavorSupported(DataFlavor.stringFlavor));
            }

            public void menuDeselected(MenuEvent menuEvent) {}
            public void menuCanceled(MenuEvent menuEvent) {}
        });

        /* <----- Add Menus to MenuBar -----> */
        menuBar.add(fileMenu);
        menuBar.add(editMenu);

        /* <----- Set MenuBar -----> */
        setJMenuBar(menuBar);

        add(textPane);
        pack();
        Dimension size = new Dimension(640, 480);
        setSize(size);

        InputMap map = textArea.getInputMap(JComponent.WHEN_FOCUSED);
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (currentFile == null || !currentFile.exists()) {
                    openFileSaver();
                } else {
                    saveCurrentFile();
                }
            }
        });
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (confirmClose()) dispose();
            }
        });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowListener() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirmClose()) {
                    saveConfig();
                    dispose();
                }
            }
            public void windowOpened(WindowEvent e) {}
            public void windowClosed(WindowEvent e) {}
            public void windowIconified(WindowEvent e) {}
            public void windowDeiconified(WindowEvent e) {}
            public void windowActivated(WindowEvent e) {}
            public void windowDeactivated(WindowEvent e) {}
        });
        setLocationRelativeTo(null);
        setVisible(true);

        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                EventQueue.invokeLater(() -> {
                    if (textArea.getText().equals(lastSaved) || textArea.getText().isEmpty())
                        setTitle(getTitle().replace("*)", ")"));
                    else if (!getTitle().endsWith("*)")) setTitle(getTitle().replace(")", "*)"));
                });
            }

            public void keyPressed(KeyEvent keyEvent) {}
            public void keyReleased(KeyEvent keyEvent) {}
        });

        /* <----- Get Properties -----> */
        configFile = new File(System.getProperty("user.home"), "trophonix" + File.separator + "txt" + File.separator + "config.properties");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            try {
                configFile.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        config = new Properties();
        try {
            config.load(new FileInputStream(configFile));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (config.getProperty("fontFamily") != null) {
            Font font = new Font(config.getProperty("fontFamily"), Integer.parseInt(config.getProperty("fontStyle")), Integer.parseInt(config.getProperty("fontSize")));
            textArea.setFont(font);
        }

        if (config.getProperty("wordWrap") != null) {
            boolean wordWrap = Boolean.parseBoolean(config.getProperty("wordWrap"));
            textArea.setLineWrap(wordWrap);
            wordWrapItem.setState(wordWrap);
        }
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
                    textArea.read(new FileReader(file), "Opening File for TrophonixTXT");
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
            chooser.setVisible(true);
            int input = JOptionPane.showOptionDialog(chooser, "Do you want to exit without saving?", "You Haven't Saved!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"Yes, exit", "No, I want to save!"}, (Object)"Yes, exit");
            chooser.setVisible(false);
            chooser.dispose();
            return input == 0;
        }
        return true;
    }

    private void openFontChooser() {
        JFrame chooser = makeChooserFrame();
        chooser.add(new FontChooser(this, chooser, textArea.getFont()));
        chooser.setSize(new Dimension(400, 300));
        chooser.setLocationRelativeTo(null);
        chooser.setVisible(true);
    }

    private JFrame makeChooserFrame() {
        JFrame chooserFrame = new JFrame();
        chooserFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        chooserFrame.setLocationRelativeTo(null);
        return chooserFrame;
    }

    public void font(Font font) {
        textArea.setFont(font);
        config.setProperty("fontFamily", font.getFamily());
        config.setProperty("fontStyle", font.getStyle() + "");
        config.setProperty("fontSize", font.getSize() + "");
        saveConfig();
    }

    private void saveConfig() {
        try {
            config.store(new FileOutputStream(configFile), "saving");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TrophonixTXT();
    }

}

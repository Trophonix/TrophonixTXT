package com.trophonix.txt;

import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lucas on 3/31/17.
 */
public class TrophonixTXT extends JFrame {

    private class FindEntry {

        final int start;
        final int end;

        public FindEntry(int start, int end) {
            this.start = start;
            this.end = end;
        }

    }

    private static final String TITLE = "TrophonixTXT v1.5 BETA";
    private static final Dimension SIZE = new Dimension(640, 480);

    private JPanel mainPanel = new JPanel(new BorderLayout());

    private File currentDirectory = new File(".");
    private File currentFile = null;

    private JTextPane textArea = new JTextPane();
    private JScrollPane scrollPane = new JScrollPane(textArea);
    private Document textDocument = textArea.getDocument();
    private UndoManager undoManager = new UndoManager();

    private String lastSaved;

    private Highlighter.HighlightPainter defaultHighlighter = DefaultHighlighter.DefaultPainter;
    private Highlighter.HighlightPainter selectedHighlighter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

    private boolean finding;
    private String lastFind;
    private int findScrollIndex;
    private List<FindEntry> findIndexes = new ArrayList<>();
    private static boolean findIgnoreCase = false;
    private boolean lastFindIgnoreCase = findIgnoreCase;

    private File configFile;
    private Properties config;

    public TrophonixTXT() {
        super(TITLE + " (New File)");

        mainPanel.setBorder(null);
        scrollPane.setBorder(null);
        textArea.setBorder(new EmptyBorder(5, 10, 5, 10));

        setLayout(new BorderLayout());

        JMenuBar menuBar = new CustomMenuBar();
        menuBar.setBorder(null);

        /* <----- File Menu -----> */
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem newItem = new JMenuItem("New", KeyEvent.VK_N);
        newItem.addActionListener(event -> {
            if (confirmClose()) {
                textArea.setText("");
                setTitle(TITLE + " (New File)");
                currentDirectory = null;
                currentFile = null;
                lastSaved = null;
                undoManager.discardAllEdits();
            }
        });
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        fileMenu.add(newItem);

        JMenuItem openItem = new JMenuItem("Open", KeyEvent.VK_O);
        openItem.addActionListener(event -> openFileChooser());
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem("Save", KeyEvent.VK_A);
        saveItem.addActionListener(event -> {
            if (currentFile == null || !currentFile.exists()) {
                openFileSaver();
            } else {
                saveCurrentFile();
            }
        });
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        fileMenu.add(saveItem);

        JMenuItem saveAsItem = new JMenuItem("Save As", KeyEvent.VK_S);
        saveAsItem.addActionListener(event -> openFileSaver());
        fileMenu.add(saveAsItem);

        JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitItem.addActionListener(event -> {
            if (confirmClose()) dispose();
        });
        fileMenu.add(exitItem);

        /* <----- Edit Menu -----> */
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);

        JMenuItem undoItem = new FocusedOnlyJMenuItem("Undo", textArea);
        undoItem.addActionListener(event -> undo());
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        editMenu.add(undoItem);

        JMenuItem redoItem = new FocusedOnlyJMenuItem("Redo", textArea);
        redoItem.addActionListener(action -> redo());
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_DOWN_MASK));
        editMenu.add(redoItem);

        editMenu.add(new JSeparator());

        JCheckBoxMenuItem wordWrapItem = new JCheckBoxMenuItem( "Word Wrap", true);
        wordWrapItem.setMnemonic(KeyEvent.VK_W);
        wordWrapItem.addActionListener(event -> {
            wrap(wordWrapItem.getState());
            config.setProperty("wordWrap", Boolean.toString(wordWrapItem.getState()));
            saveConfig();
        });
        editMenu.add(wordWrapItem);

        editMenu.add(new JSeparator());

        JMenuItem selectAllItem = new JMenuItem("Select All", KeyEvent.VK_A);
        selectAllItem.addActionListener(event -> {
            textArea.requestFocusInWindow();
            textArea.selectAll();
        });
        selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        editMenu.add(selectAllItem);

        JMenuItem copyItem = new JMenuItem("Copy", KeyEvent.VK_C);
        copyItem.addActionListener(event -> {
            StringSelection selection = new StringSelection(textArea.getSelectedText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        });
        KeyStroke copyKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        copyItem.setAccelerator(copyKeyStroke);
        copyItem.setEnabled(false);
        editMenu.add(copyItem);

        JMenuItem pasteItem = new JMenuItem("Paste", KeyEvent.VK_P);
        pasteItem.addActionListener(event -> textArea.paste());
        KeyStroke pasteKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        pasteItem.setAccelerator(pasteKeyStroke);
        pasteItem.setEnabled(false);
        editMenu.add(pasteItem);

        editMenu.add(new JSeparator());

        JPanel findPanel = new JPanel(new BorderLayout());
        findPanel.setBorder(null);

        JTextField findTextField = new JTextField();
        findTextField.addActionListener((event) -> find(findTextField.getText()));
        findPanel.add(findTextField, BorderLayout.CENTER);

        UndoManager findUndoManager = new UndoManager();

        findTextField.getDocument().addUndoableEditListener(event -> findUndoManager.addEdit(event.getEdit()));

        findTextField.getActionMap().put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (findUndoManager.canUndo()) {
                    findUndoManager.undo();
                }
            }
        });
        findTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "undo");

        findTextField.getActionMap().put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (findUndoManager.canRedo()) {
                    findUndoManager.redo();
                }
            }
        });
        findTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_DOWN_MASK), "redo");

        JPanel findButtonsPanel = new JPanel(new BorderLayout());

        JCheckBox findCaseSensitive = new JCheckBox("Ignore Case");
        findCaseSensitive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findIgnoreCase = !findIgnoreCase;
                findTextField.requestFocusInWindow();
                find(findTextField.getText());
            }
        });
        findButtonsPanel.add(findCaseSensitive, BorderLayout.WEST);

        JButton findButton = new JButton("Find");
        findButton.addActionListener((event) -> find(findTextField.getText()));
        findButtonsPanel.add(findButton, BorderLayout.CENTER);

        ImageIcon xIcon = null, xHoverIcon = null;
        try {
            xIcon = new ImageIcon(ImageIO.read(getClass().getResource("resources/x.png")));
            xHoverIcon = new ImageIcon(ImageIO.read(getClass().getResource("resources/x_hover.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JButton closeFindButton = new JButton();
        try {
            closeFindButton.setIcon(xIcon);
            closeFindButton.setBorder(new EmptyBorder(3, 3, 3, 7));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ImageIcon finalXHoverIcon = xHoverIcon;
        ImageIcon finalXIcon = xIcon;
        closeFindButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeFindButton.setIcon(finalXHoverIcon);
                closeFindButton.setBorder(new EmptyBorder(3, 3, 3, 7));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeFindButton.setIcon(finalXIcon);
                closeFindButton.setBorder(new EmptyBorder(3, 3, 3, 7));
            }
        });
        closeFindButton.addActionListener((event) -> toggleFind(findPanel, findTextField));
        findButtonsPanel.add(closeFindButton, BorderLayout.EAST);

        findPanel.add(findButtonsPanel, BorderLayout.EAST);

        JMenuItem findItem = new JMenuItem("Find", KeyEvent.VK_F);
        findItem.addActionListener(event -> toggleFind(findPanel, findTextField));
        findItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        editMenu.add(findItem);

        editMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent menuEvent) {
                String selection = textArea.getSelectedText();
                copyItem.setEnabled(selection != null && !selection.isEmpty());
                Transferable clipboard = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
                pasteItem.setEnabled(clipboard != null && clipboard.isDataFlavorSupported(DataFlavor.stringFlavor));

                undoItem.setEnabled(undoManager.canUndo());
                redoItem.setEnabled(undoManager.canRedo());
            }

            public void menuDeselected(MenuEvent menuEvent) {}
            public void menuCanceled(MenuEvent menuEvent) {}
        });

        /* <----- View Menu -----> */
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        JMenuItem fontItem = new JMenuItem("Font", KeyEvent.VK_F);
        fontItem.addActionListener(event -> openFontChooser());
        viewMenu.add(fontItem);

        JMenu themeItem = new JMenu("Theme");
        themeItem.setOpaque(true);
        themeItem.setMnemonic(KeyEvent.VK_T);

        ButtonGroup themeGroup = new ButtonGroup();

        JRadioButtonMenuItem lightCheckBoxItem = new JRadioButtonMenuItem("Light");
        lightCheckBoxItem.addActionListener(event -> {
            if (lightCheckBoxItem.isSelected()) {

            }
        });
        themeItem.add(lightCheckBoxItem);
        themeGroup.add(lightCheckBoxItem);

        JRadioButtonMenuItem darkCheckBoxItem = new JRadioButtonMenuItem("Dark");
        darkCheckBoxItem.addActionListener(event -> {
            if (darkCheckBoxItem.isSelected()) {
                menuBar.setBackground(Color.BLACK);
                menuBar.setForeground(Color.WHITE);
                findPanel.setBackground(new Color(33, 33, 33));
                findPanel.setForeground(Color.WHITE);
                textArea.setBackground(new Color(66, 66, 66));
                MutableAttributeSet attrs = textArea.getInputAttributes();
                StyleConstants.setForeground(attrs, Color.WHITE);
                StyledDocument doc = textArea.getStyledDocument();
                doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
                repaint();
            }
        });
        themeItem.add(darkCheckBoxItem);
        themeGroup.add(darkCheckBoxItem);

        JRadioButtonMenuItem indigoCheckBoxItem = new JRadioButtonMenuItem("Indigo");
        indigoCheckBoxItem.addActionListener(event -> {
            if (indigoCheckBoxItem.isSelected()) {
                menuBar.setBackground(new Color(48, 63, 159));
                menuBar.setForeground(Color.WHITE);
                findPanel.setBackground(new Color(68, 138, 255));
                findPanel.setForeground(Color.WHITE);
                textArea.setBackground(new Color(63, 81, 181));
                MutableAttributeSet attrs = textArea.getInputAttributes();
                StyleConstants.setForeground(attrs, Color.WHITE);
                StyledDocument doc = textArea.getStyledDocument();
                doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
                repaint();
            }
        });
        themeItem.add(indigoCheckBoxItem);
        themeGroup.add(indigoCheckBoxItem);

        viewMenu.add(themeItem);

        /* <----- Add Menus to MenuBar -----> */
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);

        /* <----- Set MenuBar -----> */
        setJMenuBar(menuBar);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        InputMap map = textArea.getInputMap(JComponent.WHEN_FOCUSED);
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (confirmClose()) {
                    setVisible(false);
                    dispose();
                }
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowListener() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirmClose()) {
                    saveConfig();
                    setVisible(false);
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
        if (System.getProperty("os.name").startsWith("Mac OS X")) {
            MacUtil.enableOSXFullscreen(this);
            MacUtil.enableOSXQuitStrategy();
        }
        addWindowStateListener(event -> {
            if (System.getProperty("os.name").startsWith("Mac OS X")) {
                boolean wasMaximized = (event.getOldState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
                boolean isMaximized = (event.getNewState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
                if (wasMaximized != isMaximized) {
                    MacUtil.toggleOSXFullscreen(this);
                }
            }
        });

        textArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                checkForChanges();
            }

            public void keyPressed(KeyEvent keyEvent) {}
            public void keyReleased(KeyEvent keyEvent) {}
        });

        add(mainPanel);
        setSize(SIZE);
        setPreferredSize(SIZE);
        pack();
        setLocationRelativeTo(null);

        /* <----- Setup Undo/Redo -----> */
        textDocument.addUndoableEditListener(event -> {
            undoManager.addEdit(event.getEdit());
            checkForChanges();
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
            wrap(wordWrap);
            wordWrapItem.setState(wordWrap);
        } else {
            wrap(false);
        }

    }

    private void checkForChanges() {
        EventQueue.invokeLater(() -> {
            if ((lastSaved == null && textArea.getText().trim().isEmpty()) || textArea.getText().equals(lastSaved))
                setTitle(getTitle().replace("*)", ")"));
            else if (!getTitle().endsWith("*)")) setTitle(getTitle().replace(")", "*)"));
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
                    textArea.read(new FileReader(file), "Opening File for TrophonixTXT");
                    lastSaved = textArea.getText();
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
                if (!currentFile.exists() || confirmOverwrite(currentFile)) {
                    saveCurrentFile();
                }
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
            FileWriter fileWriter = new FileWriter(currentFile);
            textArea.write(fileWriter);
            fileWriter.close();
            lastSaved = textArea.getText();
            setTitle(TITLE + " (" + name + ")");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean confirmClose() {
        if (lastSaved == null && textArea.getText().isEmpty()) return true;
        if (!textArea.getText().equals(lastSaved)) {
            JFrame chooser = makeChooserFrame();
            chooser.setVisible(true);
            int input = JOptionPane.showOptionDialog(chooser, "Do you want to exit without saving?", "You Haven't Saved!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"Yes, exit", "No, I want to save!"}, "Yes, exit");
            chooser.setVisible(false);
            chooser.dispose();
            return input == 0;
        }
        return true;
    }

    private boolean confirmOverwrite(File file) {
        JFrame chooser = makeChooserFrame();
        chooser.setVisible(true);
        int input = JOptionPane.showOptionDialog(chooser, "Do you want to overwrite " + file.getName() + "?", "Overwrite existing file?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[]{"Yes", "No"}, "Yes");
        chooser.setVisible(false);
        chooser.dispose();
        return input == 0;
    }

    private void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
            checkForChanges();
        }
    }

    private void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
            checkForChanges();
        }
    }

    private void wrap(boolean wordWrap) {
        if (wordWrap) {
            scrollPane.setViewportView(textArea);
        } else {
            JPanel panel = new JPanel(new BorderLayout());
            scrollPane.setViewportView(panel);
            panel.add(textArea);
        }
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

    private void toggleFind(JPanel findPanel, JTextField findTextField) {
        finding = !finding;
        if (finding) {
            mainPanel.add(findPanel, BorderLayout.NORTH);
            findTextField.requestFocusInWindow();
        } else {
            textArea.getHighlighter().removeAllHighlights();
            mainPanel.remove(findPanel);
        }
        revalidate();
    }

    private void find(String search) {
        if (!search.isEmpty()) {
            if (lastFind != null && !lastFind.isEmpty() && search.equals(lastFind) && lastFindIgnoreCase == findIgnoreCase) {
                if (findIndexes.isEmpty()) return;
                findScrollIndex ++;
                if (findScrollIndex >= findIndexes.size()) findScrollIndex = 0;
                Highlighter highlighter = textArea.getHighlighter();
                highlighter.removeAllHighlights();
                for (int i = 0; i < findIndexes.size(); i++) {
                    FindEntry entry = findIndexes.get(i);
                    try {
                        highlighter.addHighlight(entry.start, entry.end,
                                i == findScrollIndex ? selectedHighlighter : defaultHighlighter);
                        if (i == findScrollIndex) {
                            textArea.scrollRectToVisible(textArea.modelToView(entry.end));
                        }
                    } catch (BadLocationException ignored) {}
                }
            } else {
                lastFind = search;
                lastFindIgnoreCase = findIgnoreCase;
                findIndexes.clear();
                findScrollIndex = -1;
                Highlighter highlighter = textArea.getHighlighter();
                highlighter.removeAllHighlights();
                Pattern pattern = findIgnoreCase ? Pattern.compile(search, Pattern.CASE_INSENSITIVE) : Pattern.compile(search);
                Matcher matcher = pattern.matcher(textArea.getText());
                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();

                    findIndexes.add(new FindEntry(start, end));
                }
                find(search);
            }
        }
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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException ex) {
        }
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        UIManager.put("PopupMenu.border", BorderFactory.createEmptyBorder());
        new TrophonixTXT();
    }

}

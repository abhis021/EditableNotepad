import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class EditableNotepadEx implements ActionListener {
    private static final String APPLICATION_NAME = "Editable Notepad";
    private static final String UNTITLED_TITLE = "Untitled - " + APPLICATION_NAME;
    private static final int DEFAULT_WIDTH = 600;
    private static final int DEFAULT_HEIGHT = 500;

    private JFrame frm;
    private JMenuBar mb;
    private JMenu file, edit, help;
    private JMenuItem cut, copy, paste, selectAll, delete, open, newItem, save, saveAs, exit, about;
    private JTextArea ta;
    private File currentFile;
    private boolean modified;

    public EditableNotepadEx() {
        frm = new JFrame(APPLICATION_NAME);
        frm.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frm.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        frm.setLocationRelativeTo(null);
        frm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirmDiscardChanges()) {
                    frm.dispose();
                    System.exit(0);
                }
            }
        });

        newItem = createMenuItem("New", KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK);
        open = createMenuItem("Open...", KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
        save = createMenuItem("Save", KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
        saveAs = createMenuItem("Save As...", KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        exit = createMenuItem("Exit", KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK);

        cut = createMenuItem("Cut", KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK);
        copy = createMenuItem("Copy", KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
        paste = createMenuItem("Paste", KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK);
        delete = createMenuItem("Delete", KeyEvent.VK_DELETE, 0);
        selectAll = createMenuItem("Select All", KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK);

        about = new JMenuItem("About");
        about.addActionListener(this);

        mb = new JMenuBar();
        file = new JMenu("File");
        edit = new JMenu("Edit");
        help = new JMenu("Help");

        file.add(newItem);
        file.add(open);
        file.addSeparator();
        file.add(save);
        file.add(saveAs);
        file.addSeparator();
        file.add(exit);

        edit.add(cut);
        edit.add(copy);
        edit.add(paste);
        edit.add(delete);
        edit.addSeparator();
        edit.add(selectAll);

        help.add(about);

        mb.add(file);
        mb.add(edit);
        mb.add(help);

        ta = new JTextArea();
        ta.setLineWrap(false);
        ta.setWrapStyleWord(false);
        ta.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                modified = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                modified = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                modified = true;
            }
        });

        JScrollPane scrollPane = new JScrollPane(ta);

        frm.setJMenuBar(mb);
        frm.add(scrollPane, BorderLayout.CENTER);
        frm.setTitle(UNTITLED_TITLE);
        frm.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == cut) {
            ta.cut();
        } else if (source == copy) {
            ta.copy();
        } else if (source == paste) {
            ta.paste();
        } else if (source == delete) {
            ta.replaceSelection("");
        } else if (source == selectAll) {
            ta.selectAll();
        } else if (source == newItem) {
            newFile();
        } else if (source == open) {
            openFile();
        } else if (source == save) {
            saveFile(false);
        } else if (source == saveAs) {
            saveFile(true);
        } else if (source == exit) {
            if (confirmDiscardChanges()) {
                frm.dispose();
                System.exit(0);
            }
        } else if (source == about) {
            JOptionPane.showMessageDialog(frm,
                    "Editable Notepad clone\nBuilt with Java Swing.",
                    "About " + APPLICATION_NAME,
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JMenuItem createMenuItem(String text, int keyEvent, int modifiers) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(this);
        if (keyEvent != 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(keyEvent, modifiers));
        }
        return item;
    }

    private void newFile() {
        if (!confirmDiscardChanges()) {
            return;
        }
        ta.setText("");
        currentFile = null;
        modified = false;
        frm.setTitle(UNTITLED_TITLE);
    }

    private void openFile() {
        if (!confirmDiscardChanges()) {
            return;
        }

        JFileChooser fc = new JFileChooser();
        int result = fc.showOpenDialog(frm);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = fc.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                ta.read(reader, null);
                modified = false;
                frm.setTitle(currentFile.getName() + " - " + APPLICATION_NAME);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frm,
                        "Could not open file: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFile(boolean saveAsChoice) {
        if (currentFile == null || saveAsChoice) {
            JFileChooser fc = new JFileChooser();
            int result = fc.showSaveDialog(frm);
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }
            currentFile = fc.getSelectedFile();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
            ta.write(writer);
            modified = false;
            frm.setTitle(currentFile.getName() + " - " + APPLICATION_NAME);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frm,
                    "Could not save file: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean confirmDiscardChanges() {
        if (!modified) {
            return true;
        }

        int result = JOptionPane.showConfirmDialog(frm,
                "Do you want to save changes to " + (currentFile == null ? "Untitled" : currentFile.getName()) + "?",
                APPLICATION_NAME,
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            return false;
        }

        if (result == JOptionPane.YES_OPTION) {
            saveFile(false);
            return !modified;
        }

        return true;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(EditableNotepadEx::new);
    }
}

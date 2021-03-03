import java.awt.event.*;
import java.net.FileNameMap;

import javax.swing.*;

public class EditableNotepadEx implements ActionListener
{
    JFrame frm;
    JMenuBar mnubr;
    JMenu fileMenu, editmMenu, helpMenu;
    JMenuItem cutItem, copyItem, pasteItem, selectAll;
    JTextArea textArea;

    EditableNotepadEx()
    {
        frm = new JFrame();
        cutItem = new JMenuItem("cutItem");
        copyItem = new JMenuItem("copyItem");
        pasteItem = new JMenuItem("pasteItem");
        selectAll = new JMenuItem("selectAllItem");

        copyItem.addActionListener(this);
        cutItem.addActionListener(this);
        selectAll.addActionListener(this);
        pasteItem.addActionListener(this);
        mnubr = new JMenuBar();
        mnubr.setBounds(5,5,400,40);
        fileMenu = new JMenu("File");
        editmMenu = new JMenu("Edit");
        helpMenu = new JMenu("Help");
        editmMenu.add(cutItem);
        editmMenu.add(copyItem);
        editmMenu.add(selectAll);
        mnubr.add(fileMenu);
        mnubr.add(editmMenu);
        mnubr.add(helpMenu);
        textArea = new JTextArea();
        textArea.setBounds(5, 30, 460, 460);
        frm.add(mnubr);
        frm.add(textArea);
        frm.setLayout(null);
        frm.setSize(500, 500);
        frm.setVisible(true);
    }

        public void actionPerformed(ActionEvent ae) 
        {
            if (ae.getSource()==cutItem)   
            textArea.cut();
            if(ae.getSource()== pasteItem)
            textArea.paste();
            if(ae.getSource()==copyItem)
            textArea.copy();
            if(ae.getSource()== selectAll)
            textArea.selectAll();
        }

        public static void main(String[] args) {
            new EditableNotepadEx();
        }


    }
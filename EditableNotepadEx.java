import java.awt.event.*;


import javax.swing.*;

public class EditableNotepadEx implements ActionListener
{
    JFrame frm;
    JMenuBar mb;
    JMenu file, edit, help;
    JMenuItem cut, copy, paste, Select_All, open, newItem, save, save_As, exit;
    JTextArea ta;

    EditableNotepadEx()
    {
        frm = new JFrame();
        cut = new JMenuItem("cut");
        copy = new JMenuItem("copy");
        paste = new JMenuItem("paste");
        Select_All = new JMenuItem("selectAllItem");
        open= new JMenuItem("OpenItem");
        newItem=new JMenuItem("newItem");
        save=new JMenuItem("save");
        save_As=new JMenuItem("savsasItem");
        exit= new JMenuItem("exit");

        copy.addActionListener(this);
        cut.addActionListener(this);
        Select_All.addActionListener(this);
        paste.addActionListener(this);
        open.addActionListener(this);
        newItem.addActionListener(this);
        save.addActionListener(this);
        save_As.addActionListener(this);
        exit.addActionListener(this);
        open=new JMenuItem("Open File");    
        open.addActionListener(this);            
        file=new JMenu("File");    
        file.add(open);          

        mb = new JMenuBar();
        mb.setBounds(5,5,400,40);
        file = new JMenu("File");
        edit = new JMenu("Edit");
        help = new JMenu("Help");
        file.add(newItem);
        file.add(open);
        file.add(save);
        file.add(save_As);
        file.add(exit);
        edit.add(cut);
        edit.add(copy);
        edit.add(Select_All);
        mb.add(file);
        mb.add(edit);
        mb.add(help);
        ta = new JTextArea();
        ta.setBounds(5, 30, 460, 460);
        frm.add(mb);
        frm.add(ta);
        frm.setLayout(null);
        frm.setSize(500, 500);
        frm.setVisible(true);
    }

        public void actionPerformed(ActionEvent e) 
        {
            if (e.getSource()==cut)   
                ta.cut();
            if(e.getSource()== paste)
                ta.paste();
            if(e.getSource()==copy)
                ta.copy();
            if(e.getSource()== Select_All)
                ta.selectAll();
                if(e.getSource()==open){    
                    JFileChooser fc=new JFileChooser();    
                    int i=fc.showOpenDialog(this);    
                    if(i==JFileChooser.APPROVE_OPTION){    
                        File f=fc.getSelectedFile();    
                        String filepath=f.getPath();
                    }    
                           
            

        }

        public static void main(String[] args) {
            new EditableNotepadEx();
        }


    }
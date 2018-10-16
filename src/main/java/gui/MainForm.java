package gui;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class MainForm extends JFrame{
    public JPanel panel_main;
    private JComboBox cbMin;
    private JComboBox cbMax;
    private JComboBox cbNovel;
    private JButton btTestParsing;
    private JButton btCreateTxt;

    List<String> urls = new ArrayList<>();

    public MainForm() {
        init();

        pack();
        setContentPane(panel_main);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400,300);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void init() {
        urls.add( "https://www.readlightnovel.org/release-that-witch/chapter-");
        urls.add( "http://www.wuxiaworld.com/novel/against-the-gods/atg-chapter-");
        urls.add( "https://www.wuxiaworld.com/novel/wu-dong-qian-kun/wdqk-chapter-");

        for(String s : urls){
           // cbNovel.addItem(s);
        }
    }


    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}

import helpers.Fetch;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class MainForm extends JFrame {
    private Fetch parser;
    public JPanel panel_main;
    private JComboBox cbMin;
    private JComboBox cbMax;
    private JComboBox cbNovel;
    private JButton btTestParsing;
    private JButton btCreateTxt;
    private JScrollPane spMain;
    private JTextArea textArea;

    Map<String, String> novels = new TreeMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainForm("Novel Wrapper Desktop");
            }
        });
    }

    public MainForm(String title) {
        super(title);
        setContentPane(panel_main);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);

        init();
        setEventListeners();

        setVisible(true);
        
    }



    private void init() {
        novels.put("Release that Witch / REDLIGHTNOVEL", "https://www.readlightnovel.org/release-that-witch/chapter-");
        novels.put("Against the Gods / WUXIAWORLD", "http://www.wuxiaworld.com/novel/against-the-gods/atg-chapter-");
        novels.put("Wu dong qian kun / WUXIAWORLD", "https://www.wuxiaworld.com/novel/wu-dong-qian-kun/wdqk-chapter-");
        novels.put("Overgeared / WUXIAWORLD", "https://www.wuxiaworld.com/novel/overgeared/og-chapter-");

        for (String key : novels.keySet()) {
            cbNovel.addItem(key);
        }

        for (int i = 1; i < 4000; i++) {
            cbMin.addItem(i);
            cbMax.addItem(i);
        }
    }

    private void setEventListeners() {
        btTestParsing.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int min = Integer.parseInt(cbMin.getSelectedItem().toString());
                int max = Integer.parseInt(cbMax.getSelectedItem().toString());

                if (min > max) {
                    textArea.setText("Error: Min Chapter can't be larger than max!");
                } else {
                    btTestParsing.setEnabled(false);
                    btCreateTxt.setEnabled(false);
                    startTest();
                }
            }
        });

        btCreateTxt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int min = Integer.parseInt(cbMin.getSelectedItem().toString());
                int max = Integer.parseInt(cbMax.getSelectedItem().toString());

                if (min > max) {
                    textArea.setText("Error: Min Chapter can't be larger than max!");
                } else {

                    btTestParsing.setEnabled(false);
                    btCreateTxt.setEnabled(false);
                    createTxt();
                }
            }
        });
    }

    private void createTxt() {
        startTest();


    }

    private void startTest() {
        class Parser extends SwingWorker<Boolean, Integer> {
            private String url = novels.get(cbNovel.getSelectedItem());
            int min = Integer.parseInt(cbMin.getSelectedItem().toString());
            int max = Integer.parseInt(cbMax.getSelectedItem().toString());
            private StringBuilder sb;

            @Override
            protected Boolean doInBackground() throws Exception {

                for(int i=min;i<=max;i++){
                   sb=Fetch.fetchNovel(url,i);
                   if(sb==null){
                        return false;
                   }else{
                       sb.append(sb.toString());
                   }
                   publish(i);
                }

                return true;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int mostRecentValue = chunks.get(chunks.size()-1);
                textArea.setText((int)((double)mostRecentValue/(double)max*100)+"%");
            }

            @Override
            protected void done() {
                boolean status;

                btTestParsing.setEnabled(true);
                btCreateTxt.setEnabled(true);

                try {
                    // Retrieve the return value of doInBackground.
                    status = get();
                    if(!status){
                        textArea.setText("Error !");
                    }else{
                        textArea.setText(sb.toString());
                    }
                } catch (InterruptedException e) {
                    // This is thrown if the thread's interrupted.
                } catch (ExecutionException e) {
                    // This is thrown if we throw an exception
                    // from doInBackground.
                }
            }
        }

        new Parser().execute();
    }
}

import helpers.Fetch;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class MainForm extends JFrame {
    private JPanel panel_main;
    private JComboBox cbMin;
    private JComboBox cbMax;
    private JComboBox cbNovel;
    private JButton btTestParsing;
    private JButton btCreateTxt;
    private JScrollPane spMain;
    private JTextArea textArea;
    private Map<String, String> novels = new TreeMap<>();
    private boolean toFile;

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
        toFile=false;

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
                    toFile=false;
                    new Parser().execute();
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
                    toFile=true;
                    new Parser().execute();
                }
            }
        });
    }

    private void createTxt() {
        String url = novels.get(cbNovel.getSelectedItem());
        int min = Integer.parseInt(cbMin.getSelectedItem().toString());
        int max = Integer.parseInt(cbMax.getSelectedItem().toString());

        String [] tmb=url.split("/");
        ArrayUtils.reverse(tmb);
        String novel_name= tmb[1];

        File filename = new File("books\\"+novel_name+"_"+min+"-"+max+".txt");
        try{
            filename.getParentFile().mkdirs();
        }catch(Exception e){
            e.printStackTrace();
        }

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), StandardCharsets.UTF_8))) {
            writer.write(textArea.getText());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        textArea.setText(filename+" created!");
    }

    class Parser extends SwingWorker<Boolean, Integer> {
        private String url = novels.get(cbNovel.getSelectedItem());
        private int min = Integer.parseInt(cbMin.getSelectedItem().toString());
        private int max = Integer.parseInt(cbMax.getSelectedItem().toString());
        private StringBuilder fullText=new StringBuilder();
        private String text;

        @Override
        protected Boolean doInBackground(){

            for(int i=min;i<=max;i++){
                text=Fetch.fetchNovel(url,i);
                if(text == null){
                    return false;
                }else{
                    fullText.append(text);
                }
                publish(i);
            }

            return true;
        }

        @Override
        protected void process(List<Integer> chunks) {
            int i = chunks.get(chunks.size()-1);
            int diff = max-min;
            textArea.setText((int)(((double)i-min)/diff*100) +"%");
        }

        @Override
        protected void done() {
            boolean status;

            btTestParsing.setEnabled(true);
            btCreateTxt.setEnabled(true);

            try {
                status = get();
                if(status){
                    textArea.setText(fullText.toString());
                    if(toFile){
                        createTxt();
                    }
                }else{
                    textArea.setText("Error !");
                }
            } catch (InterruptedException e) {
                // This is thrown if the thread's interrupted.
            } catch (ExecutionException e) {
                // This is thrown if we throw an exception
                // from doInBackground.
            }
        }
    }
}

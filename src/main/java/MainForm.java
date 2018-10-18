import gui.AddSourceDialog;
import helpers.Fetch;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
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
    private JButton addSourceButton;
    private Map<String, String> novels = new TreeMap<>();
    private boolean toFile=false;
    private Properties properties = new Properties();

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
        setSize(400, 500);
        setLocationRelativeTo(null);

        init();
        setEventListeners();
        setVisible(true);
    }

    private void init() {
        //load novels

        try {
            properties.load(new FileInputStream("novels.properties"));

            for (String key : properties.stringPropertyNames()) {
                novels.put(key, properties.get(key).toString());
            }

            System.out.println("loading novels succesful!");
        } catch (IOException e) {
            novels.put("Release that Witch / REDLIGHTNOVEL", "https://www.readlightnovel.org/release-that-witch/chapter-");
            novels.put("Against the Gods / WUXIAWORLD", "http://www.wuxiaworld.com/novel/against-the-gods/atg-chapter-");
            novels.put("Wu dong qian kun / WUXIAWORLD", "https://www.wuxiaworld.com/novel/wu-dong-qian-kun/wdqk-chapter-");
            novels.put("Overgeared / WUXIAWORLD", "https://www.wuxiaworld.com/novel/overgeared/og-chapter-");
        }

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

        addSourceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddSourceDialog dialog = new AddSourceDialog();
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
                URI uri = null;
                String returnValue=dialog.tfUrl.getText();
                try {
                    uri = new URI(returnValue);
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }

                if(!returnValue.equals("")){
                    novels.put(uri.getHost(),returnValue);
                    cbNovel.addItem(uri.getHost());
                }

            }
        });

        addWindowListener(new WindowAdapter() {

            //saving novels
            public void windowClosing(WindowEvent e) {
                try {
                    for (Map.Entry<String,String> entry : novels.entrySet()) {
                        properties.put(entry.getKey(), entry.getValue());
                    }

                    properties.store(new FileOutputStream("novels.properties"), null);
                    System.out.println("save successful!");
                } catch (IOException ex) {
                    ex.printStackTrace();
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
            int diff = max-min+1;
            textArea.setText(
                    (int)(((double)i-min+1)/diff*100
            ) +"%");
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

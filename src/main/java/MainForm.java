import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import gui.AddSourceDialog;
import helpers.Fetch;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import java.awt.*;
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
    private boolean toFile = false;
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
            novels.put("Trash of the Counts Family / WUXIAWORLD", "https://www.wuxiaworld.com/novel/trash-of-the-counts-family/tcf-chapter-");
            novels.put("The Novels Extra / WUXIAWORLD", "https://www.wuxiaworld.com/novel/the-novels-extra/tne-chapter-");
            novels.put("A Will Eternal / WUXIAWORLD", "https://www.wuxiaworld.com/novel/a-will-eternal/awe-chapter-");
        }

        for (String key : novels.keySet()) {
            cbNovel.addItem(key);
        }

        for (int i = 0; i < 2000; i++) {
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
                    toFile = false;
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
                    cbMin.setEnabled(false);
                    cbMax.setEnabled(false);
                    toFile = true;
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
                String returnValue = dialog.tfUrl.getText();
                try {
                    uri = new URI(returnValue);
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }

                String[] tmb = returnValue.split("/");
                ArrayUtils.reverse(tmb);
                String novel_name = tmb[1];

                if (!returnValue.equals("")) {
                    novels.put(novel_name + " (" + uri.getHost() + ")", returnValue);
                    cbNovel.addItem(novel_name + " (" + uri.getHost() + ")");
                }

            }
        });

        addWindowListener(new WindowAdapter() {

            //saving novels
            public void windowClosing(WindowEvent e) {
                try {
                    for (Map.Entry<String, String> entry : novels.entrySet()) {
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

        String[] tmb = url.split("/");
        ArrayUtils.reverse(tmb);
        String novel_name = tmb[1];

        File filename = new File("books\\" + novel_name + "_" + min + "-" + max + ".txt");
        try {
            filename.getParentFile().mkdirs();
        } catch (Exception e) {
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

        textArea.setText(filename + " created!");
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel_main = new JPanel();
        panel_main.setLayout(new GridLayoutManager(8, 2, new Insets(10, 10, 10, 10), -1, -1));
        cbMin = new JComboBox();
        panel_main.add(cbMin, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cbMax = new JComboBox();
        panel_main.add(cbMax, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Chapters");
        panel_main.add(label1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btTestParsing = new JButton();
        btTestParsing.setText("Test Parsing");
        panel_main.add(btTestParsing, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btCreateTxt = new JButton();
        btCreateTxt.setText("Create Ebook (txt)");
        panel_main.add(btCreateTxt, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spMain = new JScrollPane();
        spMain.setHorizontalScrollBarPolicy(31);
        panel_main.add(spMain, new GridConstraints(7, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setText("Welcome");
        textArea.setWrapStyleWord(true);
        spMain.setViewportView(textArea);
        final JLabel label2 = new JLabel();
        label2.setText("Novel");
        panel_main.add(label2, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cbNovel = new JComboBox();
        panel_main.add(cbNovel, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addSourceButton = new JButton();
        addSourceButton.setText("Add Source");
        panel_main.add(addSourceButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel_main;
    }

    class Parser extends SwingWorker<Boolean, Integer> {
        private String url = novels.get(cbNovel.getSelectedItem());
        private int min = Integer.parseInt(cbMin.getSelectedItem().toString());
        private int max = Integer.parseInt(cbMax.getSelectedItem().toString());
        private StringBuilder fullText = new StringBuilder();
        private String text;

        @Override
        protected Boolean doInBackground() {

            for (int i = min; i <= max; i++) {
                text = Fetch.fetchNovel(url, i);
                if (text == null) {
                    return false;
                } else {
                    fullText.append(text);
                }
                publish(i);
            }

            return true;
        }

        @Override
        protected void process(List<Integer> chunks) {
            int i = chunks.get(chunks.size() - 1);
            int diff = max - min + 1;
            textArea.setText(
                    (int) (((double) i - min + 1) / diff * 100
                    ) + "%");
        }

        @Override
        protected void done() {
            boolean status;

            btTestParsing.setEnabled(true);
            btCreateTxt.setEnabled(true);
            cbMax.setEnabled(true);
            cbMin.setEnabled(true);

            try {
                status = get();
                if (status) {
                    textArea.setText(fullText.toString());
                    if (toFile) {
                        createTxt();
                    }
                } else {
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

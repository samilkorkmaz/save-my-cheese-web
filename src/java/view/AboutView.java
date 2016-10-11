package view;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * About screen.
 *
 * @author Samil Korkmaz
 * @date January 2015
 * @copyright Public Domain
 */
public class AboutView extends JPanel {

    private static final int PREF_WIDTH = 400;
    private static final int PREF_HEIGHT = 200;
    private static AboutView instance;
    private static final JFrame frame = new JFrame("About - Save My Cheese");
    private static final JTextArea jtaInfo = new JTextArea("Save My Cheese version 0.2."
            + "\nAuthor: Samil Korkmaz\nDate: March 2015"
            + "\n\nMouse image: http://free.clipartof.com/details/57-Free-Cartoon-Gray-Field-Mouse-Clipart-Illustration");
    private static final String BLOG_URL = "http://samilkorkmaz.blogspot.com";
    private static final JButton jbBack = new JButton("Back");
    private static final JButton jbBlog = new JButton(BLOG_URL);
    private static final JPanel jpButtons = new JPanel();
    private static final java.awt.Font BUTTON_FONT = new java.awt.Font("Tahoma", 1, 12);
    private URI uri;

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PREF_WIDTH, PREF_HEIGHT);
    }

    public static void createAndShowGUI() {
        if (instance == null) {
            try {
                instance = new AboutView();
                frame.setResizable(false);
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                frame.setIconImage(ImageIO.read(WelcomeView.ICON));
                frame.getContentPane().add(instance);
                frame.pack();
                frame.setLocationRelativeTo(null);
            } catch (IOException ex) {
                Logger.getLogger(AboutView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        frame.setVisible(true);
    }

    /**
     * Reference: http://stackoverflow.com/questions/527719/how-to-add-hyperlink-in-jlabel
     */
    private static void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException e) { /* TODO: error handling */ }
        } else { /* TODO: error handling */ }
    }

    private AboutView() {        
        try {
            uri = new URI(BLOG_URL);
        } catch (URISyntaxException ex) {
            Logger.getLogger(AboutView.class.getName()).log(Level.SEVERE, null, ex);
        }
        class OpenUrlAction implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent ae) {
                System.out.println("Open url " + uri);
                open(uri);
            }
        }
        jbBlog.setText("Go to Samil's blog...");
        jbBlog.setToolTipText(uri.toString());
        jbBlog.addActionListener(new OpenUrlAction());
        
        setLayout(new java.awt.GridLayout(2, 0));
        add(jtaInfo);
        add(jpButtons);
        jpButtons.setBackground(Color.WHITE);
        jpButtons.add(jbBlog);
        jpButtons.add(jbBack);

        jtaInfo.setEditable(false);
        jtaInfo.setLineWrap(true);
        jbBack.setFont(BUTTON_FONT);

        jbBack.addActionListener((java.awt.event.ActionEvent evt) -> {
            makeVisible(false);
            WelcomeView.makeVisible(true);
        });

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                WelcomeView.makeVisible(true);
            }
        });
    }

    public static void makeVisible(boolean isVisible) {
        frame.setVisible(isVisible);
    }
}

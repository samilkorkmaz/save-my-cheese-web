package view;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Welcome screen.
 *
 * @author Samil Korkmaz
 * @date January 2015
 * @license Public Domain
 */
public class WelcomeView extends JPanel {

    public static final String IMAGE_DIR = "/resources/images/"; //Note: ImageIO.read expects "/" at the beginning of path
    public static final String POLYGON_DIR = "resources/polygons/"; //Note: class.getClassLoader().getResourceAsStream does not want "/" at the beginning of path.
    public static final URL ICON = WelcomeView.class.getResource(IMAGE_DIR + "icon - save my cheese.png");
    private static final int PREF_WIDTH = 400;
    private static final int PREF_HEIGHT = 200;
    private static WelcomeView instance;
    private static final JFrame frame = new JFrame("Welcome - Save My Cheese");
    private static final JButton jbStart = new JButton("Start");
    private static final JButton jbAbout = new JButton("About");
    private static final JButton jbExit = new JButton("Exit");
    private static final java.awt.Font BUTTON_FONT = new java.awt.Font("Tahoma", 1, 24);

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PREF_WIDTH, PREF_HEIGHT);
    }

    public static void createAndShowGUI() {
        if (instance == null) {
            try {
                instance = new WelcomeView();
                frame.setResizable(false);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setIconImage(ImageIO.read(ICON));
                frame.getContentPane().add(instance);
                frame.pack();
                frame.setLocationRelativeTo(null);                
            } catch (IOException ex) {
                Logger.getLogger(WelcomeView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        frame.setVisible(true);
    }

    private WelcomeView() {
        setLayout(new java.awt.GridLayout(3, 0));
        add(jbStart);
        add(jbAbout);
        add(jbExit);
        
        jbStart.setFont(BUTTON_FONT);
        jbAbout.setFont(BUTTON_FONT);
        jbExit.setFont(BUTTON_FONT);
           
        jbStart.addActionListener((java.awt.event.ActionEvent evt) -> {
            makeVisible(false);
            GameView.createAndShowGUI();
        });
        jbAbout.addActionListener((java.awt.event.ActionEvent evt) -> {
            makeVisible(false);
            AboutView.createAndShowGUI();
        });
        jbExit.addActionListener((java.awt.event.ActionEvent evt) -> {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });
    }
   
  
    public static void makeVisible(boolean isVisible) {
        frame.setVisible(isVisible);
    }
}

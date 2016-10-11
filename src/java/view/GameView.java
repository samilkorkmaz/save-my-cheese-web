package view;

import controller.GameController;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Game window.
 *
 * @author Samil Korkmaz
 * @date January 2015
 * @copyright Public Domain
 */
public class GameView extends JPanel {

    private static final int PREF_WIDTH = 800;
    private static final int PREF_HEIGHT = 600;
    private static final int BUTTON_PANEL_WIDTH = 100;
    private static GameView instance;
    private static final JFrame frame = new JFrame("Game - Save My Cheese");
    private static final JButton jbStartPause = new JButton("Pause");
    private static final JButton jbRestart = new JButton("Restart");
    private static final JButton jbNext = new JButton("Next");
    private static final JButton jbBack = new JButton("Back");
    private static JPanel jpCanvas;
    private static final JPanel jpButtons = new JPanel();
    private static final Font BUTTON_FONT = new Font("Tahoma", 1, 12);
    private static final Font LEVEL_SUCCESS_FONT = new Font("Tahoma", 1, 24);
    private static final Font LEVEL_FAIL_FONT = new Font("Tahoma", 1, 24);
    private static final int SUCCESS_FAIL_X = 10;
    private static final int SUCCESS_FAIL_Y = 10;
    private static final int SUCCESS_FAIL_WIDTH = 400;
    private static final int SUCCESS_FAIL_HEIGHT = 50;
    private static final Color SUCCESS_FG_COLOR = Color.BLUE;
    private static final Color SUCCESS_BG_COLOR = Color.LIGHT_GRAY;
    private static final Color FAIL_FG_COLOR = Color.RED;
    private static final Color FAIL_BG_COLOR = Color.YELLOW;
    private static final String SUCCESS_TEXT = "Level %d completed successfully!";
    private static final String FAIL_TEXT = "Game over: Mouse ate the cheese!";
    private static final JLabel jlSuccessFail = new JLabel();
    private static final JLabel jlLevel = new JLabel();
    private static boolean isPause = true;

    @Override
    public final Dimension getPreferredSize() {
        return new Dimension(PREF_WIDTH, PREF_HEIGHT);
    }

    public static void createAndShowGUI() {
        if (instance == null) {
            try {
                instance = new GameView();
                frame.setResizable(false);
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                frame.setIconImage(ImageIO.read(WelcomeView.ICON));
                frame.getContentPane().add(instance);
                frame.pack();
                frame.setLocationRelativeTo(null);
                initLevel();
            } catch (IOException ex) {
                Logger.getLogger(WelcomeView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //frame.setVisible(true);
    }
        
    private GameView() {
        setLayout(null);
        jpCanvas = CanvasPanel.create(0, 0, (int) getPreferredSize().getWidth() - (BUTTON_PANEL_WIDTH + 10), (int) getPreferredSize().getHeight());
        jpCanvas.add(jlSuccessFail);    
        jlLevel.setBounds((int) getPreferredSize().getWidth() - (BUTTON_PANEL_WIDTH + 100), 10, 100, 20);
        jpCanvas.add(jlLevel);
        
        add(jpCanvas);
        add(jpButtons);

        jpButtons.add(jbStartPause);
        jpButtons.add(jbRestart);
        jpButtons.add(jbNext);
        jpButtons.add(jbBack);
        jpButtons.setLayout(new java.awt.GridLayout(jpButtons.getComponentCount(), 0));
        jpButtons.setBounds((int) getPreferredSize().getWidth() - (BUTTON_PANEL_WIDTH + 10), 10, BUTTON_PANEL_WIDTH,
                25 * jpButtons.getComponentCount());

        jbStartPause.setFont(BUTTON_FONT);
        jbRestart.setFont(BUTTON_FONT);
        jbNext.setFont(BUTTON_FONT);
        jbNext.setEnabled(false);
        jbBack.setFont(BUTTON_FONT);

        jbStartPause.addActionListener((java.awt.event.ActionEvent evt) -> {
            if (isPause) {
                GameController.pause();
            } else {
                GameController.continueGame();
            }
            isPause = !isPause;
            if (isPause) {
                jbStartPause.setText("Pause");                
            } else {
                jbStartPause.setText("Continue");
            }
        });

        jbRestart.addActionListener((ActionEvent ae) -> {
            initLevel();
        });

        jbNext.addActionListener((java.awt.event.ActionEvent evt) -> {
            System.out.println("next");
            GameController.incLevel();            
            initLevel();
        });

        jbBack.addActionListener((ActionEvent ae) -> {
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

    public static void initLevel() {
        GameController.start();
        jlLevel.setText("Level: " + GameController.getLevel() + " / " + GameController.getNbOfLevels());
        jlSuccessFail.setOpaque(false);
        jlSuccessFail.setText("");
        jpCanvas.repaint();
        jbNext.setEnabled(false);
    }

    public static void setLevelSuccess() {
        jlSuccessFail.setBounds(SUCCESS_FAIL_X, SUCCESS_FAIL_Y, SUCCESS_FAIL_WIDTH, SUCCESS_FAIL_HEIGHT);
        jlSuccessFail.setFont(LEVEL_SUCCESS_FONT);
        jlSuccessFail.setForeground(SUCCESS_FG_COLOR);
        jlSuccessFail.setBackground(SUCCESS_BG_COLOR);
        jlSuccessFail.setOpaque(true);
        jlSuccessFail.setText(String.format(SUCCESS_TEXT, GameController.getLevel()));
        if (GameController.isNotLastLevel()) {
            jbNext.setEnabled(true);
        }
    }

    public static void setLevelFail() {
        jlSuccessFail.setBounds(SUCCESS_FAIL_X, SUCCESS_FAIL_Y, SUCCESS_FAIL_WIDTH + 50, SUCCESS_FAIL_HEIGHT);
        jlSuccessFail.setFont(LEVEL_FAIL_FONT);
        jlSuccessFail.setForeground(FAIL_FG_COLOR);
        jlSuccessFail.setBackground(FAIL_BG_COLOR);
        jlSuccessFail.setOpaque(true);
        jlSuccessFail.setText(FAIL_TEXT);        
    }
}

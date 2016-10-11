package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

import controller.GameController;
import model.Map2D;
import model.MyMouse;
import model.MyRectangle;
import model.Node;

/**
 * Canvas on which map, cheese, puzzle pieces and mice are drawn. It is a component of GameView.
 *
 * @author Samil Korkmaz
 * @date January 2015
 * @license Public Domain
 */
public class CanvasPanel extends JPanel {

    private static CanvasPanel instance;
    private static final Stroke SHAPE_STROKE = new BasicStroke(3f);
    private static final Stroke SNAP_SHAPE_STROKE = new BasicStroke(1f);
    private static final Color SHAPE_LINE_COLOR = Color.BLACK;
    private static final Color SHAPE_FILL_COLOR = Color.BLUE;
    private static final Color SNAP_SHAPE_LINE_COLOR = Color.BLACK;
    private static final Color BACKGROUND_COLOR = Color.GREEN;

    private static final Color OPEN_PATH_COLOR = BACKGROUND_COLOR;
    private static final Color WALL_COLOR = new Color(0, 0, 0, 0); //alpha = 0: Transparent, alpha = 255: Opaque
    private static final Color MAP_GRID_COLOR = Color.LIGHT_GRAY;
    private static final Color CHEESE_COLOR = Color.YELLOW;
    private static final Color PATH_COLOR = Color.ORANGE;
    private static final Color CURRENT_NODE_COLOR = Color.RED;

    public static void refreshDrawing() {
        if (instance != null) {
            instance.repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        drawMap(g2);
        for (Shape snapShape : GameController.getSnapShapeList()) {
            g2.setStroke(SNAP_SHAPE_STROKE);
            g2.setColor(SNAP_SHAPE_LINE_COLOR);
            g2.draw(snapShape);
        }
        for (Shape shape : GameController.getShapeList()) {
            g2.setColor(SHAPE_FILL_COLOR);
            g2.fill(shape);
            g2.setStroke(SHAPE_STROKE);
            g2.setColor(SHAPE_LINE_COLOR);
            g2.draw(shape);
        }
        drawPaths(g2);
    }

    private void drawPaths(Graphics2D g2) {
        for (int i = 0; i < GameController.getMyMouseList().size(); i++) {
            MyMouse myMouse = GameController.getMyMouseList().get(i);
            for (Node pathNode : myMouse.getPath()) {
                MyRectangle pathCell = Map2D.getMapCellList().get(get1DIndex(pathNode.getRowIndex(), pathNode.getColIndex()));
                MyMouse.RectRowCol activePathPoint = myMouse.getActivePoint();
                if (pathNode.getRowIndex() == activePathPoint.getRowIndex() && pathNode.getColIndex() == activePathPoint.getColIndex()) {
                    g2.setColor(CURRENT_NODE_COLOR);
                } else {
                    g2.setColor(PATH_COLOR);
                }
                g2.draw(pathCell);
            }
            Point ap = myMouse.getActivePointXY();
            // Rotation information
            int mouseImageHalfWidth = MyMouse.getMouseImage().getWidth(null) / 2;
            int mouseImageHalfHeight = MyMouse.getMouseImage().getHeight(null) / 2;
            double locationX = mouseImageHalfWidth;
            double locationY = mouseImageHalfHeight;
            AffineTransform tx = AffineTransform.getRotateInstance(myMouse.getImageRotation_rad(), locationX, locationY);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
            // Drawing the rotated image at the required drawing locations
            g2.drawImage(op.filter(toBufferedImage(MyMouse.getMouseImage()), null), ap.x - mouseImageHalfWidth + Map2D.getRectWidth() / 2,
                    ap.y - mouseImageHalfHeight + Map2D.getRectHeight() / 2, null);
        }
    }

    /**
     * Converts a given Image into a BufferedImage.<br/>
     * Reference: http://stackoverflow.com/questions/13605248/java-converting-image-to-bufferedimage
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
     */
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    private int get1DIndex(int iRow, int iCol) {
        return iRow * Map2D.N_MAP_COLS + iCol;
    }

    private void drawMap(Graphics2D g2) {
        for (MyRectangle rect : Map2D.getMapCellList()) {
            if (rect.isOpen()) {
                g2.setColor(OPEN_PATH_COLOR);
            } else {
                g2.setColor(WALL_COLOR);
            }
            MyMouse.RectRowCol rc = Map2D.getRowCol(rect);
            if (rc.getRowIndex() == GameController.CHEESE_IROW && rc.getColIndex() == GameController.CHEESE_ICOL) {
                g2.setColor(CHEESE_COLOR);
            }
            g2.fill(rect);
            g2.setColor(MAP_GRID_COLOR);
            g2.draw(rect);
        }
    }

    public static CanvasPanel create(int x, int y, int width, int height) {
        if (instance == null) {
            instance = new CanvasPanel(x, y, width, height);
        }
        return instance;
    }

    public static int getPanelWidth() {
        return 800;
        //return instance.getBounds().width;
    }

    public static int getPanelHeight() {
        return 600;
        //return instance.getBounds().height;
    }

    private CanvasPanel(int x, int y, int width, int height) {
        super();        
        Map2D.createMap(width, height);
        setBounds(x, y, width, height);
        setLayout(null);
        setBackground(BACKGROUND_COLOR);
        MyMouseAdapter myMouseAdapter = new MyMouseAdapter();
        addMouseListener(myMouseAdapter);
        addMouseMotionListener(myMouseAdapter);
    }

    private class MyMouseAdapter extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent evt) {
            if (evt.getButton() == MouseEvent.BUTTON1) {
                GameController.setSelectedShape(evt.getX(), evt.getY());
                repaint();
            }
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            if (!GameController.isPaused()) { //prevent puzzle piece movement when game is paused
                if (!GameController.isGameOver() && mouseIsInCanvas(evt)) {
                    GameController.moveShape(evt.getX(), evt.getY());
                }
            }
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            GameController.deselectShape();
            repaint();
        }
    }

    private boolean mouseIsInCanvas(MouseEvent evt) {
        return evt.getX() >= this.getX() && evt.getX() <= this.getX() + this.getWidth()
                && evt.getY() >= this.getY() && evt.getY() <= this.getY() + this.getHeight();
    }

}

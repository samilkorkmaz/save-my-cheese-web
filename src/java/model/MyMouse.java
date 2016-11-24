package model;

import controller.GameController;
import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import view.WelcomeView;

/**
 * Mouse operations.
 *
 * @author Samil Korkmaz
 * @date January 2015
 * @license Public Domain
 */
public class MyMouse {

    private int iPath;
    private Node currentNode;
    private Node nextNode;
    private static final double DOUBLE_TOLERANCE = 1e-15;
    private boolean keepRunning = true;
    private List<Node> path; //starts from endNode and ends at startNode

    private int iActiveRow = 0;
    private int iActiveCol = 0;
    private final Point activePoint = new Point();
    private static Image mouseImage;

    private double imageRotation_rad;
    private double prevImageRotation_rad = 0;

    public double getImageRotation_rad() {
        return imageRotation_rad;
    }

    public void setPrevImageRotation_rad(double prevImageRotation_rad) {
        this.prevImageRotation_rad = prevImageRotation_rad;
    }

    /**
     * Image source: http://free.clipartof.com/details/57-Free-Cartoon-Gray-Field-Mouse-Clipart-Illustration
     * @return 
     */
    public static Image getMouseImage() {
        if (mouseImage == null) {
            try {
                mouseImage = makeColorTransparent(ImageIO.read(MyMouse.class.getResource(WelcomeView.IMAGE_DIR + "Mouse.png")), Color.WHITE);
            } catch (IOException ex) {
                Logger.getLogger(MyMouse.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return mouseImage;
    }

    /**
     * Make provided image transparent wherever color matches the provided color.<br/>
     *
     * Reference:
     * http://www.javaworld.com/article/2074105/core-java/making-white-image-backgrounds-transparent-with-java-2d-groovy.html
     *
     * @param im BufferedImage whose color will be made transparent.
     * @param color Color in provided image which will be made transparent.
     * @return Image with transparency applied.
     */
    private static Image makeColorTransparent(final BufferedImage im, final Color color) {
        final ImageFilter filter = new RGBImageFilter() {
            // the color we are looking for (white)... Alpha bits are set to opaque
            public int markerRGB = color.getRGB() | 0xFFFFFFFF;

            @Override
            public final int filterRGB(final int x, final int y, final int rgb) {
                if ((rgb | 0xFF000000) == markerRGB) {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                } else {
                    // nothing to do
                    return rgb;
                }
            }
        };
        final ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }

    public List<Node> getPath() {
        return path;
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }

    public boolean getKeepRunning() {
        return keepRunning;
    }

    public MyMouse(int iMouse) {
        super();
        System.out.println(iMouse + ". mouse created.");
    }

    private void setActivePointXY(int x, int y) {
        activePoint.x = x;
        activePoint.y = y;
    }

    public Point getActivePointXY() {
        return activePoint;
    }

    public void setActivePoint(int iRow, int iCol) {
        iActiveRow = iRow;
        iActiveCol = iCol;
        setActivePointXY(iCol * Map2D.getRectWidth(), iRow * Map2D.getRectHeight());
    }

    public void setActivePoint(RectRowCol rc) {
        setActivePoint(rc.rowIndex, rc.colIndex);
    }

    public RectRowCol getActivePoint() {
        return new RectRowCol(iActiveRow, iActiveCol);
    }

    public void calcPathToCheese() {
        System.out.println("iActiveRow = " + iActiveRow + ", iActiveCol = " + iActiveCol);
        Node startNode = new Node(null, iActiveRow, iActiveCol);
        Node endNode = new Node(null, GameController.CHEESE_IROW, GameController.CHEESE_ICOL);
        setActivePoint(startNode.getRowIndex(), startNode.getColIndex());
        this.path = new AStarPathFinder().calcPath(Map2D.getMapArray2D(), startNode, endNode);
        currentNode = path.get(path.size() - 1);
        nextNode = path.get(path.size() - 2);
        iPath = path.size() - 1;
    }

    public static void resetMap() {
        for (int iRow = 0; iRow < Map2D.N_MAP_ROWS; iRow++) {
            for (int iCol = 0; iCol < Map2D.N_MAP_COLS; iCol++) {
                Map2D.getMapArray2D()[iRow][iCol] = AStarPathFinder.OPEN;
                Map2D.getMapCellList().get(iRow * Map2D.N_MAP_ROWS + iCol).setPathType(AStarPathFinder.OPEN);
            }
        }
    }

    public static class RectRowCol {

        private int rowIndex;
        private int colIndex;

        public int getRowIndex() {
            return rowIndex;
        }

        public int getColIndex() {
            return colIndex;
        }

        public RectRowCol(int rowIndex, int colIndex) {
            this.rowIndex = rowIndex;
            this.colIndex = colIndex;
        }
    }

    public static boolean isEqualDoubles(double d1, double d2) {
        return Math.abs(d2 - d1) <= DOUBLE_TOLERANCE;
    }

    private boolean hasReachedCheese() {
        int currentNodeX = currentNode.getColIndex() * Map2D.getRectWidth();
        int currentNodeY = currentNode.getRowIndex() * Map2D.getRectHeight();
        int cheeseX = GameController.CHEESE_ICOL * Map2D.getRectWidth();
        int cheeseY = GameController.CHEESE_IROW * Map2D.getRectHeight();
        double distanceToCheese = Math.hypot(currentNodeX-cheeseX, currentNodeY-cheeseY);
        System.out.printf("rectWidth: %d, rectHeight: %d, distanceToCheese: %1.0f", Map2D.getRectWidth(), 
                Map2D.getRectHeight(), distanceToCheese);
        return distanceToCheese <= 2.1*Math.max(Map2D.getRectWidth(), Map2D.getRectHeight());
    }

    public void moveAlongPathToCheese() {
        iPath--;
        if (iPath <= 1) { //There is no path left. Can have two reasons: 1.Cheese is reached 2.There is no path to the cheese, i.e. all cells surrounding the mouse are walls (happens when a puzzle piece is dropped on top of a mouse).
            if (!GameController.isAllPuzzlePiecesPlaced()) {
                if (hasReachedCheese()) {
                    //mouse reached cheese, game over
                    GameController.onMouseReachedCheese();
                } else {
                    //No path to cheese. Do nothing, stay where you are
                }
            }
        } else {
            setActivePoint(currentNode.getRowIndex(), currentNode.getColIndex());
            int currentNodeX = currentNode.getColIndex() * Map2D.getRectWidth();
            int currentNodeY = currentNode.getRowIndex() * Map2D.getRectHeight();
            int nextNodeX = nextNode.getColIndex() * Map2D.getRectWidth();
            int nextNodeY = nextNode.getRowIndex() * Map2D.getRectHeight();
            double dxNode = nextNodeX - currentNodeX;
            double dyNode = nextNodeY - currentNodeY;
            int nDivisions = 10;
            int dx = (int) Math.round(dxNode / nDivisions);
            int dy = (int) Math.round(dyNode / nDivisions);
            if (dx == 0 && dy == 0) {
                System.out.print("currentNode.getRowIndex() = " + currentNode.getRowIndex() + ", currentNode.getColIndex() = " + currentNode.getColIndex());
                System.out.println(", nextNode.getRowIndex() = " + nextNode.getRowIndex() + ", nextNode.getColIndex() = " + nextNode.getColIndex());
                throw new IllegalArgumentException("dx/dy zero. Decrease nDivisions!");
            }
            double currentImageRotation_rad = Math.atan2(dyNode, dxNode) - Math.PI / 2;
            double dRotationTemp_rad = currentImageRotation_rad - prevImageRotation_rad;
            double dRotation_rad;
            //decide shortest rotation direction:
            if (dRotationTemp_rad > 0) {
                if (dRotationTemp_rad < Math.PI) {
                    dRotation_rad = dRotationTemp_rad;
                } else {
                    dRotation_rad = 2 * Math.PI - dRotationTemp_rad;
                }
            } else if (dRotationTemp_rad < -Math.PI) {
                dRotation_rad = 2 * Math.PI + dRotationTemp_rad;
            } else {
                dRotation_rad = dRotationTemp_rad;
            }
            double rotationInc_rad = dRotation_rad / nDivisions;

            for (int iDiv = 0; iDiv < nDivisions; iDiv++) {
                //linear interpolation of mouse rotation and position between two nodes
                imageRotation_rad = prevImageRotation_rad + iDiv * rotationInc_rad;
                setActivePointXY(currentNodeX + iDiv * dx, currentNodeY + iDiv * dy);
            }
            currentNode = nextNode;
            nextNode = path.get(iPath - 1);
            prevImageRotation_rad = currentImageRotation_rad;
        }
    }

}

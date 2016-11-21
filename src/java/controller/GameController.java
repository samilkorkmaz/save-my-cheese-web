package controller;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Map2D;
import model.MyMouse;
import model.MyPolygon;
import view.CanvasPanel;
import view.GameView;
import view.WelcomeView;

/**
 * Game controller.
 *
 * @author Samil Korkmaz
 * @date January 2015
 * @license Public Domain
 */
public class GameController {

    private static final int SLEEP_TIME_MS = 32 * 50;
    private static final List<MyPolygon> polygonList = new ArrayList<>();
    private static final List<MyPolygon> snapPolygonList = new ArrayList<>();
    private static MyPolygon selectedPolygon = null;
    private static MyPolygon selectedSnapPolygon = null;
    private static int prevMouseX;
    private static int prevMouseY;
    private static boolean isAllPuzzlePiecesPlaced;
    private static int level = 1;
    private static final int nbOfLevels = FileUtils.getNbOfLevelFiles();
    private static boolean isPaused;
    private static boolean isGameOver = false;
    private static final List<MyMouse> myMouseList = new ArrayList<>();
    private static final int N_MOUSE = 3;
    private static final List<MyMouse.RectRowCol> startPointList = new ArrayList<>();
    public static final int CHEESE_IROW = 24;
    public static final int CHEESE_ICOL = 30;
    private static int allPolyXTranslation;
    private static int allPolyYTranslation;

    public static List<MyMouse> getMyMouseList() {
        return myMouseList;
    }

    public static void updateMapAndPaths(Shape shape) {
        Map2D.updateMap(shape);
        GameController.getMyMouseList().stream().forEach((myMouse) -> {
            myMouse.calcPathToCheese();
        });
        //CanvasPanel.refreshDrawing();
    }

    public static void onMouseReachedCheese() {
        myMouseList.stream().forEach((myMouse) -> {
            myMouse.setKeepRunning(false);
        });
        GameController.setGameOver(true);
    }

    public static void reset() {
        startPointList.clear();
        MyMouse.resetMap();
        myMouseList.stream().forEach((myMouse) -> {
            myMouse.setKeepRunning(false);
        });
        myMouseList.clear();
        //CanvasPanel.refreshDrawing();
    }

    private static boolean isInStartPointList(MyMouse.RectRowCol rc) {
        boolean isInStartPointList = false;
        for (MyMouse.RectRowCol rcInList : startPointList) {
            if (rcInList.getRowIndex() == rc.getRowIndex() && rcInList.getColIndex() == rc.getColIndex()) {
                isInStartPointList = true;
                break;
            }
        }
        return isInStartPointList;
    }

    private static MyMouse.RectRowCol getRandomCell() {
        MyMouse.RectRowCol rc;
        int radius = Math.min(CHEESE_IROW, CHEESE_ICOL);
        int x0 = CHEESE_ICOL;
        int y0 = CHEESE_IROW;
        int counter = 0;
        while (true) {
            double angle_rad = Math.toRadians(30 * new Random().nextInt(12));
            int iRow = y0 + (int) Math.round(radius * Math.cos(angle_rad));
            int iCol = x0 + (int) Math.round(radius * Math.sin(angle_rad));
            rc = new MyMouse.RectRowCol(iRow, iCol);
            if (!isInStartPointList(rc)) {
                startPointList.add(rc);
                break;
            }
            if (counter++ > 100) { //infinite loop prevention
                throw new RuntimeException("while loop taking too many iterations!");
            }
        }
        return rc;
    }

    public static void setGameOver(boolean isGameOver) {
        GameController.isGameOver = isGameOver;
        if (isGameOver) {
            //GameView.setLevelFail();
        }
    }

    public static boolean isGameOver() {
        return isGameOver;
    }

    public static boolean isPaused() {
        return isPaused;
    }

    public static boolean isNotLastLevel() {
        return level < nbOfLevels;
    }

    public static int getNbOfLevels() {
        return nbOfLevels;
    }

    public static int getLevel() {
        return level;
    }

    public static void incLevel() {
        level++;
    }

    public static String getPolygonFileForCurrentLevel() {
        return "level" + level + ".txt";
    }

    private static Point calcScaledMinMaxX(List<PolygonData> pdList, double scaleFactor) {
        int maxX = Integer.MIN_VALUE;
        int minX = Integer.MAX_VALUE;
        for (PolygonData pd : pdList) {
            int currentMaxX = (int) Math.round(scaleFactor * pd.maxX);
            int currentMinX = (int) Math.round(scaleFactor * pd.minX);
            maxX = Math.max(maxX, currentMaxX);
            minX = Math.min(minX, currentMinX);
        }
        return new Point(minX, maxX);
    }

    private static Point calcScaledMinMaxY(List<PolygonData> pdList, double scaleFactor) {
        int maxY = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        for (PolygonData pd : pdList) {
            int currentMaxY = (int) Math.round(scaleFactor * pd.maxY);
            int currentMinY = (int) Math.round(scaleFactor * pd.minY);
            maxY = Math.max(maxY, currentMaxY);
            minY = Math.min(minY, currentMinY);
        }
        return new Point(minY, maxY);
    }

    /**
     * Calculate bounds of polygon list with scaling the coordinates in polygon list.
     */
    private static Point2D.Double calcScaledBounds(List<PolygonData> pdList, double scaleFactor) {
        Point minMaxX = calcScaledMinMaxX(pdList, scaleFactor);
        Point minMaxY = calcScaledMinMaxY(pdList, scaleFactor);
        double dx = minMaxX.y - minMaxX.x;
        double dy = minMaxY.y - minMaxY.x;
        return new Point2D.Double(dx, dy);
    }

    /**
     * Calculate bounds of polygon list without scaling the coordinates in polygon list.
     */
    private static Point2D.Double calcBounds(List<PolygonData> pdList) {
        return calcScaledBounds(pdList, 1);
    }

    /**
     * Calculate scale factor that will fit the polygons inside area on canvas reserved for polygons.
     */
    private static double calcScaleFactor(List<PolygonData> pdList) {
        Point2D.Double pdListBounds = calcBounds(pdList);
        double canvasAreaMultiplier = 0.5; //Polygon area is 50% of canvas paint area
        double sx = canvasAreaMultiplier * CanvasPanel.getPanelWidth() / pdListBounds.x;
        double sy = canvasAreaMultiplier * CanvasPanel.getPanelHeight() / pdListBounds.y;
        return Math.min(sx, sy);
    }

    /**
     * Calculate translation required to move center of snap polygons to cheese coordinates.
     */
    private static Point calcSnapCenterTranslation(List<PolygonData> pdList, double scaleFactor) {
        Point minMaxX = calcScaledMinMaxX(pdList, scaleFactor);
        Point minMaxY = calcScaledMinMaxY(pdList, scaleFactor);
        int cheeseX = CHEESE_ICOL * Map2D.getRectWidth();
        int cheeseY = CHEESE_IROW * Map2D.getRectHeight();
        int xTranslation = cheeseX - (int) Math.round((minMaxX.y + minMaxX.x) / 2.0);
        int yTranslation = cheeseY - (int) Math.round((minMaxY.y + minMaxY.x) / 2.0);
        return new Point(xTranslation, yTranslation);
    }

    public static void start() {
        setGameOver(false);
        isAllPuzzlePiecesPlaced = false;
        polygonList.clear();
        snapPolygonList.clear();
        List<PolygonData> pdList = getPolygonDataFromFile(getPolygonFileForCurrentLevel());
        double scaleFactor = calcScaleFactor(pdList);
        Point pSnapCenterTranslation = calcSnapCenterTranslation(pdList, scaleFactor);
        pdList.stream().forEach((pd) -> {
            int[] scaledXCoords = multiplyArray(pd.xArray, scaleFactor);
            int[] scaledYCoords = multiplyArray(pd.yArray, scaleFactor);
            addToList(scaledXCoords, scaledYCoords, pSnapCenterTranslation.x, pSnapCenterTranslation.y);
        });
        reset();
        for (int i = 0; i < N_MOUSE; i++) {
            MyMouse myMouse = new MyMouse(i);
            GameController.getMyMouseList().add(myMouse);
            myMouse.setActivePoint(getRandomCell());
            myMouse.calcPathToCheese(); //Create initial path to cheese
        }
    }

    public static void updateMice() {
        if (!isPaused()) {
            for (MyMouse myMouse : GameController.getMyMouseList()) {
                if (myMouse.getKeepRunning()) {
                    myMouse.moveAlongPathToCheese();
                    //Point ap = GameController.getMyMouseList().get(0).getActivePointXY();
                    //System.out.println("ap.x = " + ap.x + ", ap.y = " + ap.y);
                } else {
                    break;
                }
            }
            //CanvasPanel.refreshDrawing();
        }
    }

    public static double getTime_ms() {
        return 1e-6 * System.nanoTime();
    }

    public static void pause() {
        isPaused = true;
    }

    public static void continueGame() {
        isPaused = false;
    }

    public static void deselectShape() {
        selectedPolygon = null;
    }

    public static void setSelectedShape(int mouseX, int mouseY) {
        if (GameController.getShapeList().size() > 0) {
            if (selectedPolygon == null) {
                for (int i = GameController.getShapeList().size() - 1; i >= 0; i--) {
                    if (GameController.getShapeList().get(i).contains(new Point(mouseX, mouseY))) { //if there is a polygon at clicked point
                        selectedPolygon = GameController.getShapeList().get(i);

                        //move the selected polygon to the end of list so that it will be drawn last (i.e. on top) in paintComponent and checked first for mouse click:
                        GameController.getShapeList().remove(selectedPolygon);
                        GameController.getShapeList().add(GameController.getShapeList().size(), selectedPolygon);

                        selectedSnapPolygon = GameController.getSnapShapeList().get(i);
                        GameController.getSnapShapeList().remove(selectedSnapPolygon);
                        GameController.getSnapShapeList().add(GameController.getSnapShapeList().size(), selectedSnapPolygon);

                        prevMouseX = mouseX;
                        prevMouseY = mouseY;

                        break;
                    }
                }
            }
        }
    }

    public static boolean isAllPuzzlePiecesPlaced() {
        return isAllPuzzlePiecesPlaced;
    }

    private static void checkAllPuzzlePiecesPlaced() {
        isAllPuzzlePiecesPlaced = true;
        for (MyPolygon myPolygon : polygonList) {
            if (!myPolygon.isSnapped()) {
                isAllPuzzlePiecesPlaced = false;
                break;
            }
        }
        if (isAllPuzzlePiecesPlaced) {
            myMouseList.stream().forEach((myMouse) -> {
                myMouse.setKeepRunning(false); //to end game loop thread in start().
            });
            //GameView.setLevelSuccess();
        }
    }

    public static void moveShape(int mouseX, int mouseY) {
        if (selectedPolygon != null && !selectedPolygon.isSnapped()) {
            selectedPolygon.translate(mouseX - prevMouseX, mouseY - prevMouseY);
            prevMouseX = mouseX;
            prevMouseY = mouseY;
            boolean isSnapped = selectedPolygon.isCloseTo(selectedSnapPolygon);
            selectedPolygon.setIsSnapped(isSnapped);
            if (isSnapped) {
                checkAllPuzzlePiecesPlaced();
                updateMapAndPaths(selectedPolygon);
            }
        } else {
            selectedPolygon = null;
            selectedSnapPolygon = null;
        }
    }

    public static List<MyPolygon> getSnapShapeList() {
        return snapPolygonList;
    }

    public static List<MyPolygon> getShapeList() {
        return polygonList;
    }

    private static class PolygonData {

        int[] xArray;
        int[] yArray;
        int minX;
        int maxX;
        int minY;
        int maxY;
    }

    private static List<PolygonData> getPolygonDataFromFile(String fileName) {
        //read file as String list:
        InputStream is = GameController.class.getClassLoader().getResourceAsStream(WelcomeView.POLYGON_DIR + fileName);
        List<String> dataStrList = getInputStreamAsStringList(is);
        try {
            is.close();
        } catch (IOException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //extract polygon data from strings
        List<PolygonData> pdList = new ArrayList<>();
        allPolyXTranslation = getAllPolyTranslation(dataStrList.get(0));
        allPolyYTranslation = getAllPolyTranslation(dataStrList.get(1));
        for (int i = 2; i < dataStrList.size(); i++) {
            if ((i - 2) % 2 == 0) {
                PolygonData pd = new PolygonData();
                pd.xArray = getCoordArray(dataStrList.get(i));
                pd.yArray = getCoordArray(dataStrList.get(i + 1));
                pd.minX = MyPolygon.minOfArray(pd.xArray);
                pd.maxX = MyPolygon.maxOfArray(pd.xArray);
                pd.minY = MyPolygon.minOfArray(pd.yArray);
                pd.maxY = MyPolygon.minOfArray(pd.yArray);
                pdList.add(pd);
            }
        }
        return pdList;
    }

    private static int getAllPolyTranslation(String dataStr) {
        String[] str = dataStr.split("=");
        return Integer.parseInt(str[1].trim());
    }

    private static int[] getCoordArray(String dataStr) {
        String[] str1 = dataStr.split("=");
        String[] str2 = str1[1].split(",");
        int[] coordArray = new int[str2.length];
        for (int i = 0; i < str2.length; i++) {
            coordArray[i] = Integer.parseInt(str2[i].trim());
        }
        return coordArray;
    }

    private static List<String> getInputStreamAsStringList(final InputStream is) {
        String COMMENT_STRING_START = "//";
        ArrayList<String> fileAsStringList = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s;
        try {
            while ((s = br.readLine()) != null) {
                boolean isNotCommentLine = !s.trim().startsWith(COMMENT_STRING_START);
                boolean isNotEmptyLine = !s.trim().isEmpty();
                if (isNotCommentLine && isNotEmptyLine) {
                    fileAsStringList.add(s);
                }
            }
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fileAsStringList;
    }

    private static void addToList(int[] scaledXCoords, int[] scaledYCoords, int xSnap, int ySnap) {
        MyPolygon myPolygon = new MyPolygon(scaledXCoords, scaledYCoords);
        myPolygon.setSmallestXToZero();
        myPolygon.setSmallestYToZero();
        myPolygon.translate(10, 350);
        polygonList.add(myPolygon);

        MyPolygon snapPolygon = new MyPolygon(scaledXCoords, scaledYCoords);
        snapPolygon.translate(xSnap + allPolyXTranslation, ySnap + allPolyYTranslation);
        snapPolygonList.add(snapPolygon);
    }

    private static int[] multiplyArray(int[] array, double c) {
        int[] newArray = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = (int) Math.round(array[i] * c);
        }
        return newArray;
    }

}

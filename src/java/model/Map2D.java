package model;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

/**
 * Two dimensional map with rectangular cells.
 *
 * @author Samil Korkmaz
 * @date January 2015
 * @license Public Domain
 */
public class Map2D {
    public static final int N_MAP_ROWS = 50;
    public static final int N_MAP_COLS = 60;
    private static final List<MyRectangle> mapCellList = new ArrayList<>();
    private static int[][] mapArray2D;
    private static int rectHeight;
    private static int rectWidth;
    
    public static int[][] create(int nRows, int nCols) {
        int[][] map = new int[nRows][nCols];
        for (int iRow = 0; iRow < nRows; iRow++) {
            for (int iCol = 0; iCol < nCols; iCol++) {
                map[iRow][iCol] = AStarPathFinder.OPEN;
            }
        }
        return map;
    }
    
    public static void createMap(int width, int height) {
        mapArray2D = create(N_MAP_ROWS, N_MAP_COLS);
        rectHeight = height / N_MAP_ROWS;
        rectWidth = width / N_MAP_COLS;
        for (int iRow = 0; iRow < N_MAP_ROWS; iRow++) {
            for (int iCol = 0; iCol < N_MAP_COLS; iCol++) {
                MyRectangle cell = new MyRectangle(iCol * rectWidth, iRow * rectHeight, rectWidth, rectHeight, mapArray2D[iRow][iCol]);
                mapCellList.add(cell);
            }
        }
    }
    
    /**
     * Turn the map cells under the shape to walls.
     *
     * @param shape
     */
    public static void updateMap(Shape shape) {
        for (MyRectangle cell : mapCellList) {
            if (cell.isInShape(shape)) {
                MyMouse.RectRowCol rectRowCol = getRowCol(cell);
                mapArray2D[rectRowCol.getRowIndex()][rectRowCol.getColIndex()] = AStarPathFinder.WALL;
                cell.setPathType(AStarPathFinder.WALL);
            }
        }
    }
    
    public static MyMouse.RectRowCol getRowCol(MyRectangle rect) {
        int i1D = mapCellList.indexOf(rect);
        int iCol = i1D % N_MAP_COLS;
        int iRow = (i1D - iCol) / N_MAP_COLS;
        return new MyMouse.RectRowCol(iRow, iCol);
    }
    
    public static List<MyRectangle> getMapCellList() {
        return mapCellList;
    }
    
    public static int getRectWidth() {
        return rectWidth;
    }

    public static int getRectHeight() {
        return rectHeight;
    }

    public static int[][] getMapArray2D() {
        return mapArray2D;
    }

}

package model;

import java.awt.Polygon;

/**
 * Custom polygon class.
 *
 * @author skorkmaz
 */
public class MyPolygon extends Polygon {

    private boolean isSnapped;
    private static final int SNAP_TOLERANCE = 10;

    public MyPolygon(int[] xCoords, int[] yCoords) {
        super(xCoords, yCoords, xCoords.length);
        isSnapped = false;      
    }

    private void setSmallestCoordinateToZero(int[] array) {
        int minValue = minOfArray(array);
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i] - minValue;
        }
    }
    
    public void setSmallestXToZero() {
        setSmallestCoordinateToZero(xpoints);
    }
    
    public void setSmallestYToZero() {
        setSmallestCoordinateToZero(ypoints);
    }

    public static int maxOfArray(final int[] array) {
        int maxValue = Integer.MIN_VALUE;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
            }
        }
        return maxValue;
    }
    
    public static int minOfArray(final int[] array) {
        int minValue = Integer.MAX_VALUE;
        for (int i = 0; i < array.length; i++) {
            if (array[i] < minValue) {
                minValue = array[i];
            }
        }
        return minValue;
    }

    public boolean isSnapped() {
        return isSnapped;
    }

    public void setIsSnapped(boolean isSnapped) {
        this.isSnapped = isSnapped;
    }

    public boolean isCloseTo(MyPolygon snapPolygon) {
        boolean isCloseTo = Math.abs(getBounds().x - snapPolygon.getBounds().x) < SNAP_TOLERANCE
                && Math.abs(getBounds().y - snapPolygon.getBounds().y) < SNAP_TOLERANCE;
        if (isCloseTo) {
            //move polygon to snap polygon location
            int deltaX = snapPolygon.getBounds().x - getBounds().x;
            int deltaY = snapPolygon.getBounds().y - getBounds().y;
            translate(deltaX, deltaY);
        }
        return isCloseTo;
    }

}

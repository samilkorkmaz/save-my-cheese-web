package model;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

/**
 * Rectangle that knows if it is inside a given shape.
 *
 * @author Samil Korkmaz
 * @date February 2015
 * @license Public Domain
 */
public class MyRectangle extends Rectangle {

    private int pathType;
    
    public MyRectangle(int x, int y, int width, int height, int pathType) {
        super(x, y, width, height);
        this.pathType = pathType;
    }
    
    public int getPathType() {
        return pathType;
    }
    
    public boolean isOpen() {
        return pathType == AStarPathFinder.OPEN;
    }
    
    public void setPathType(int pathType) {
        this.pathType = pathType;
    }

    public boolean isInShape(Shape shape) {
        return shape.contains(calcCenterPoint());
    }

    private Point calcCenterPoint() {
        return new Point(this.x + this.width / 2, this.y + this.height / 2);
    }
}

package model;

/**
 * A node is a cell/point on path that lies on a 2D grid (map).
 *
 * @author Samil Korkmaz
 * @date January 2015
 * @license Public Domain
 */
public class Node {

    private Node parentNode;
    private final int iCol;
    private final int iRow;
    private int fCost;
    private int gCost;

    public Node(Node parentNode, int iRow, int iCol) {
        this.parentNode = parentNode;
        this.iCol = iCol;
        this.iRow = iRow;
    }
    
    public void setFCost(int fCost) {
        this.fCost = fCost;
    }
    
    public int getFCost() {
        return fCost;
    }
    
    public void setGCost(int gCost) {
        this.gCost = gCost;
    }
    
    public int getGCost() {
        return gCost;
    }
    
    public void setParent(Node parentNode) {
        this.parentNode = parentNode;
    }
    
    public Node getParentNode() {
        return parentNode;
    }

    public int getRowIndex() {
        return iRow;
    }

    public int getColIndex() {
        return iCol;
    }
}

package model;

import java.util.ArrayList;
import java.util.List;

/**
 * A* path finding implementation.<br>
 *
 * References :<br/>
 * 1. http://www.policyalmanac.org/games/aStarTutorial.htm<br>/
 * 2. https://www.youtube.com/watch?v=KNXfSOx4eEE<br/>
 * 2. https://www.youtube.com/watch?v=_oaqK0G_qKk<br/>
 *
 * @author Samil Korkmaz
 * @date February 2015
 * @license Public Domain
 */
public class AStarPathFinder {

    static final int WALL = 0;
    static final int OPEN = 1;
    private static final int SIMPLE_MOVEMENT_COST = 10; //cost when moving horizontal or vertical
    private static final int DIAGONAL_MOVEMENT_COST = 14;
    private final List<Node> openList = new ArrayList<>();
    private final List<Node> closedList = new ArrayList<>();

    /**
     * Calculates path nodes from end to start using A* algorithm.
     * @param map
     * @param startNode
     * @param endNode
     * @return path nodes from end to start
     */
    public List<Node> calcPath(int[][] map, Node startNode, Node endNode) {
        openList.clear();
        closedList.clear();
        List<Node> path = new ArrayList<>();
        path.add(endNode);
        openList.add(startNode);
        Node currentNode = startNode;
        boolean pathFound = false;
        if (!isEndNode(startNode, endNode)) { //start and end nodes are different points
            while (true) {
                currentNode = getLowestCostNodeFromOpenList();
                if (isEndNode(currentNode, endNode)) {
                    pathFound = true;
                    break;
                }
                closedList.add(currentNode);
                openList.remove(currentNode);
                //find neighbors of currentNode:
                for (int iRow = -1; iRow < 2; iRow++) {
                    for (int iCol = -1; iCol < 2; iCol++) {
                        //check adjacent nodes for closeness to target node (cost increases with distance to target):
                        int neighborRowIndex = currentNode.getRowIndex() + iRow;
                        int neighborColIndex = currentNode.getColIndex() + iCol;
                        if (!isInClosedList(neighborRowIndex, neighborColIndex)) { //ignore points already in closed list
                            if (isInMap(neighborRowIndex, neighborColIndex, map)) { //if candidate row and column is in map
                                if (isNotWall(map[neighborRowIndex][neighborColIndex])) { //if neighbor is not a wall
                                    //calculate cost of going from currentNode to neighborNode
                                    int gCost;
                                    int singleStepGCost;
                                    if (iRow != 0 && iCol != 0) { //diagonal movement
                                        singleStepGCost = DIAGONAL_MOVEMENT_COST;
                                    } else {
                                        singleStepGCost = SIMPLE_MOVEMENT_COST;
                                    }
                                    gCost = currentNode.getGCost() + singleStepGCost; //cost to start point                                
                                    Node neighbor;
                                    Node nodeInOpenList = getNodeFromOpenList(neighborRowIndex, neighborColIndex);
                                    int hCost = Math.abs(endNode.getRowIndex() - neighborRowIndex) * SIMPLE_MOVEMENT_COST + Math.abs(endNode.getColIndex() - neighborColIndex) * SIMPLE_MOVEMENT_COST; //cost to end point
                                    if (nodeInOpenList != null) { //neighbor was added to openList before (i.e. it has been analyzed before with a different parent)
                                        if (gCost < nodeInOpenList.getGCost()) {
                                            nodeInOpenList.setParent(currentNode); //change parent to currentNode, because this is the shorter path
                                            nodeInOpenList.setGCost(gCost);
                                            nodeInOpenList.setFCost(gCost + hCost);
                                        }
                                    } else {
                                        neighbor = new Node(currentNode, neighborRowIndex, neighborColIndex);
                                        neighbor.setGCost(gCost);
                                        neighbor.setFCost(gCost + hCost);
                                        openList.add(neighbor);
                                    }
                                }
                            }
                        }
                    }
                }

                if (openList.isEmpty()) {
                    System.out.println("No path from start to end!");
                    break;
                }
            }
        }
        if (pathFound) {
            while (currentNode.getParentNode() != null) {
                //construct path (start at endNode, ends at startNode)
                currentNode = currentNode.getParentNode();
                path.add(currentNode);
            }
        } else {
            path.add(startNode);
        }
        return path;
    }

    private static boolean isEndNode(Node node, Node endNode) {
        return node.getRowIndex() == endNode.getRowIndex() && node.getColIndex() == endNode.getColIndex();
    }

    private static boolean isNotWall(int value) {
        return value != WALL;
    }

    private static boolean isInMap(int iRow, int iCol, int[][] map) {
        return iRow >= 0 && iRow < map.length && iCol >= 0 && iCol < map[0].length;
    }

    public Node getLowestCostNodeFromOpenList() {
        int lowestCost = Integer.MAX_VALUE;
        int iLowestCost = -1;
        for (int i = 0; i < openList.size(); i++) {
            if (openList.get(i).getFCost() < lowestCost) {
                lowestCost = openList.get(i).getFCost();
                iLowestCost = i;
            }
        }
        return openList.get(iLowestCost);
    }

    public boolean isInClosedList(int iRow, int iCol) {
        boolean isInClosedList = false;
        if (!closedList.isEmpty()) {
            for (Node node : closedList) {
                if (node.getRowIndex() == iRow && node.getColIndex() == iCol) {
                    isInClosedList = true;
                }
            }
        }
        return isInClosedList;
    }

    public Node getNodeFromOpenList(int iRow, int iCol) {
        Node nodeToReturn = null;
        if (!openList.isEmpty()) {
            for (Node node : openList) {
                if (node.getRowIndex() == iRow && node.getColIndex() == iCol) {
                    nodeToReturn = node;
                }
            }
        }
        return nodeToReturn;
    }
}

package savemycheese;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import controller.GameController;
import java.util.List;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.spi.JsonProvider;
import model.Map2D;

/**
 *
 * @author sam
 */
public class Main extends HttpServlet {

    private static final String UNDEFINED_STR = "\"UNDEFINED\"";
    private final int TRIGGER_INIT = 0;
    private final int TRIGGER_MOUSE_MOVE = 10;
    private final int TRIGGER_TIME_TICK = 20;

    String polygonStr = UNDEFINED_STR;
    String miceJsonStr = UNDEFINED_STR;
    String map2DJsonStr = UNDEFINED_STR;
    String isGameOverJsonStr = UNDEFINED_STR;
    String isAllPuzzlePiecesPlacedJsonStr = UNDEFINED_STR;

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("\nDO POST");
        //Get data from JavaScript
        JsonReader jsonReader = JsonProvider.provider().createReader(request.getReader());
        JsonObject jsonFromJavaScript = jsonReader.readObject();
        //Prepare data to send to JavaScript

        int trigger = jsonFromJavaScript.getInt("trigger"); //TODO use enum
        System.out.println("trigger: " + trigger);
        String miceJsonStartStr = "\"mice\":";
        String jsonToJavaScriptStr;
        map2DJsonStr = "\"mapRectWidth\":" + Map2D.getRectWidth() + ", \"mapRectHeight\":" + Map2D.getRectHeight();
        isGameOverJsonStr = "\"isGameOver\":" + GameController.isGameOver();
        isAllPuzzlePiecesPlacedJsonStr = "\"isAllPuzzlePiecesPlaced\":" + GameController.isAllPuzzlePiecesPlaced();
        switch (trigger) {
            case TRIGGER_INIT:
                init();
                Gson gson = new Gson();
                String dragPolygonJsonStr = gson.toJson(GameController.getShapeList());
                String snapPolygonJsonStr = gson.toJson(GameController.getSnapShapeList());
                String dragPolygonStartJsonStr = "\"dragPolys\":";
                String snapPolygonStartJsonStr = "\"snapPolys\":";
                String polygonStartJsonStr = "\"updatePolygons\":true";
                polygonStr = polygonStartJsonStr + ", " + dragPolygonStartJsonStr + dragPolygonJsonStr
                        + ", " + snapPolygonStartJsonStr + snapPolygonJsonStr;
                miceJsonStr = updateMice();
                System.out.println("map2DJsonStr:" + map2DJsonStr);
                break;
            case TRIGGER_MOUSE_MOVE:
                System.out.println("JAVA: mouse move");
                miceJsonStr = getMiceState();
                //polygon stuff:
                boolean mouseDown = jsonFromJavaScript.getBoolean("mouseDown");
                int mouseX = jsonFromJavaScript.getInt("mouseX");
                int mouseY = jsonFromJavaScript.getInt("mouseY");
                polygonStr = updatePolygons(mouseDown, mouseX, mouseY);
                break;
            case TRIGGER_TIME_TICK:
                System.out.println("JAVA: time tick");
                miceJsonStr = updateMice();
                break;
            default:
                System.out.println("ERROR: Unknown trigger " + trigger);
                break;
        }

        jsonToJavaScriptStr = "{" + polygonStr + ", " + miceJsonStartStr + miceJsonStr + ", " + map2DJsonStr + 
                ", " + isGameOverJsonStr + "," + isAllPuzzlePiecesPlacedJsonStr + "}";
        //Send data to JavaScript
        //System.out.println("jsonToJavaScript: " + jsonToJavaScriptStr);
        try {
            response.setContentType("text/plain");
            response.getWriter().println(jsonToJavaScriptStr);
        } catch (IOException ex) {
            System.out.println("IOEXCEPTION!");
        }
    }

    private String updatePolygons(boolean mouseDown, int mouseX, int mouseY) {
        String dragPolygonStartJsonStr = "\"dragPolys\":";
        String snapPolygonStartJsonStr = "\"snapPolys\":";
        String polygonStartJsonStr = "\"updatePolygons\":true";
        System.out.println("mouseDown: " + mouseDown + ", x: " + mouseX + ", y: " + mouseY);
        if (mouseDown) {
            GameController.setSelectedShape(mouseX, mouseY);
            GameController.moveShape(mouseX, mouseY);
        } else {
            GameController.deselectShape();
        }

        Gson gson = new Gson();
        String dragPolygonJsonStr = gson.toJson(GameController.getShapeList());
        String snapPolygonJsonStr = gson.toJson(GameController.getSnapShapeList());
        return polygonStartJsonStr + ", " + dragPolygonStartJsonStr + dragPolygonJsonStr
                + ", " + snapPolygonStartJsonStr + snapPolygonJsonStr;
    }

    private String updateMice() {
        GameController.updateMice();
        return getMiceState();
    }

    private String getMiceState() {
        String jsonToJavaScriptStr;
        if (GameController.getMyMouseList().isEmpty()) {
            System.out.println("Mouse list empty!");
            jsonToJavaScriptStr = "{activePoint\":{\"x\":0,\"y\":0}}";
        } else {
            List myMouseList = GameController.getMyMouseList();
            Gson gson = new Gson();
            jsonToJavaScriptStr = gson.toJson(myMouseList);
        }
        return jsonToJavaScriptStr;

    }

    @Override
    public void init() throws ServletException {
        System.out.println("INIT");
        Map2D.createMap(800, 600);
        GameController.reset();
        GameController.start();
    }

}

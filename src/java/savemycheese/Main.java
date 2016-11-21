package savemycheese;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import controller.GameController;
import java.util.ArrayList;
import java.util.List;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.spi.JsonProvider;
import model.Map2D;
import model.MyPolygon;

/**
 *
 * @author sam
 */
public class Main extends HttpServlet {

    private static final String UNDEFINED_STR = "\"UNDEFINED\"";
    private final int TRIGGER_INIT = 0;
    private final int TRIGGER_MOUSE_MOVE = 10;
    private final int TRIGGER_TIME_TICK = 20;

    private static final List<MyPolygon> polygonList = new ArrayList<>();
    private static final List<MyPolygon> snapPolygonList = new ArrayList<>();
    String polygonStr = UNDEFINED_STR;
    String miceJsonStr = UNDEFINED_STR;

    private boolean isSelected = false;
    int iSelected = -1;
    private int snapX = 400;
    private int snapY = 100;
    private static int prevMouseX;
    private static int prevMouseY;

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
        switch (trigger) {
            case TRIGGER_INIT:
                init();
                Gson gson = new Gson();
                String dragPolygonJsonStr = gson.toJson(polygonList);
                String snapPolygonJsonStr = gson.toJson(snapPolygonList);
                String dragPolygonStartJsonStr = "\"dragPolys\":";
                String snapPolygonStartJsonStr = "\"snapPolys\":";
                String polygonStartJsonStr = "\"updatePolygons\":true";
                polygonStr = polygonStartJsonStr + ", " + dragPolygonStartJsonStr + dragPolygonJsonStr
                        + ", " + snapPolygonStartJsonStr + snapPolygonJsonStr;
                miceJsonStr = updateMice();
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

        jsonToJavaScriptStr = "{" + polygonStr + ", " + miceJsonStartStr + miceJsonStr + "}";
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
        //String polygonStartJsonStr = "\"updatePolygons\":false";
        String dragPolygonStartJsonStr = "\"dragPolys\":";
        String snapPolygonStartJsonStr = "\"snapPolys\":";
        //String dragPolygonJsonStr = UNDEFINED_STR;
        //String snapPolygonJsonStr = UNDEFINED_STR;
        String polygonStartJsonStr = "\"updatePolygons\":true";
        System.out.println("mouseDown: " + mouseDown + ", x: " + mouseX + ", y: " + mouseY);
        if (!mouseDown) {
            isSelected = false;
        }
        if (!isSelected) {
            for (int iPoly = 0; iPoly < polygonList.size(); iPoly++) {
                if (polygonList.get(iPoly).contains(mouseX, mouseY)) {
                    isSelected = true;
                    iSelected = iPoly;
                    prevMouseX = mouseX;
                    prevMouseY = mouseY;
                    System.out.println("polygon contains xc yc! iSelected = " + iSelected);
                    break;
                }
            }
        }
        if (isSelected && mouseDown) {
            System.out.println("translate triangle");
            polygonList.get(iSelected).translate(mouseX - prevMouseX, mouseY - prevMouseY);
            prevMouseX = mouseX;
            prevMouseY = mouseY;
            polygonList.get(iSelected).isCloseTo(snapPolygonList.get(iSelected));
        }
        Gson gson = new Gson();
        String dragPolygonJsonStr = gson.toJson(polygonList);
        String snapPolygonJsonStr = gson.toJson(snapPolygonList);
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

        isSelected = false;
        iSelected = -1;
        int[] xPoints1 = {0, 100, 0};
        int[] yPoints1 = {0, 0, 100};

        int[] xPoints2 = {0, 100, 100, 0};
        int[] yPoints2 = {0, 0, 100, 100};

        int[] xPoints3 = {200, 300, 300, 50};
        int[] yPoints3 = {0, 0, 100, 100};

        MyPolygon poly1 = new MyPolygon(xPoints1, yPoints1);
        MyPolygon poly2 = new MyPolygon(xPoints2, yPoints2);
        MyPolygon poly3 = new MyPolygon(xPoints3, yPoints3);
        MyPolygon snapPoly1 = new MyPolygon(xPoints1, yPoints1);
        MyPolygon snapPoly2 = new MyPolygon(xPoints2, yPoints2);
        MyPolygon snapPoly3 = new MyPolygon(xPoints3, yPoints3);
        prevMouseX = 0;
        prevMouseY = 0;

        snapPoly1.translate(snapX, snapY);
        snapPoly2.translate(snapX + 100, snapY);
        snapPoly3.translate(snapX - 300, snapY);

        polygonList.clear();
        polygonList.add(poly1);
        polygonList.add(poly2);
        polygonList.add(poly3);

        snapPolygonList.clear();
        snapPolygonList.add(snapPoly1);
        snapPolygonList.add(snapPoly2);
        snapPolygonList.add(snapPoly3);
    }

}

package eru.myapps.loboroutes;

/**
 * Created by elopezmo on 05.10.16.
 */
public class Extra {

    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_AUDIO = "AUDIO";
    public static final String TYPE_IMG = "IMG";

    private int routeID;
    private int locusNum;
    private String type;
    private String source;
    private int x;
    private int y;
    private int ID;


    public Extra(int routeID, int locusNum, String type, String source, int x, int y, int ID) {
        this.routeID = routeID;
        this.locusNum = locusNum;
        this.type = type;
        this.source = source;
        this.x = x;
        this.y = y;
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getRouteID() {
        return routeID;
    }

    public void setRouteID(int routeID) {
        this.routeID = routeID;
    }

    public int getLocusNum() {
        return locusNum;
    }

    public void setLocusNum(int locusNum) {
        this.locusNum = locusNum;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}

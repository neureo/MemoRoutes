package eru.myapps.loboroutes;

import java.util.LinkedList;

/**
 * Created by eru on 13.04.16.
 */
public class Route {

    private String title;
    private String description;
    private int countFrom;
    private int id;
    private String cover;

    public Route(String t, String d, int countFrom, int id){
        title = t;
        description = d;
        this.id = id;
        this.countFrom = countFrom;
        cover = MainActivity.TEXT_DEFAULT;
    }

    public Route(String t, String d, int countFrom, int id, String cover){
        title = t;
        description = d;
        this.id = id;
        this.countFrom = countFrom;
        this.cover = cover;
    }


    public int getCountFrom() {
        return countFrom;
    }

    public void setCountFrom(int countFrom) {
        this.countFrom = countFrom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }
}

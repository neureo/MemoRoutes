package eru.myapps.loboroutes;

import android.graphics.drawable.Drawable;

/**
 * Created by eru on 13.04.16.
 */
public class Locus {

    private String name;
    private String path;
    private String thumbnail;
    private int num;


    public Locus(int num, String name, String path, String thumbnail){
        this.name = name;
        this.path = path;
        this.thumbnail = thumbnail;
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}

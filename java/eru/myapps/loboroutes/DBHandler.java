package eru.myapps.loboroutes;

import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;


/**
 * Created by eru on 16.04.16.
 */
public class DBHandler extends SQLiteOpenHelper {

    private static final int DB_VERSION = 3;
    private static final String DB_NAME = "routes.db";
    private static final String TABLE_ROUTES = "routes";
    private static final String TABLE_EXTRAS = "loci";
    private static final String TABLE_LOCI = "extras";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_COUNTFROM = "countfrom";
    private static final String COLUMN_COVER = "cover";

    private static final String COLUMN_NUM = "num";
    private static final String COLUMN_ROUTE_ID = "routeID";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PATH = "path";
    private static final String COLUMN_THUMB = "thumb";


    private static final String COLUMN_LOCUS_NUM = "extraLocusNum";
    private static final String COLUMN_TYPE = "extraType";
    private static final String COLUMN_SOURCE = "extraSource";
    private static final String COLUMN_X = "extraX";
    private static final String COLUMN_Y = "extraY";
    private static final String COLUMN_EXTRA_ID = "extraID";



    private static int len = 0;

    public DBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, factory, DB_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_ROUTES + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_COUNTFROM + " INTEGER, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_COVER + " TEXT " + ");";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE_LOCI + "(" +
                COLUMN_NUM + " INTEGER, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PATH + " TEXT, " +
                COLUMN_THUMB + " TEXT, " +
                COLUMN_ROUTE_ID + " INTEGER " + ");";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE_EXTRAS + "(" +
                COLUMN_EXTRA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ROUTE_ID + " INTEGER, " +
                COLUMN_LOCUS_NUM + " INTEGER, " +
                COLUMN_TYPE + " TEXT, " +
                COLUMN_SOURCE + " TEXT, " +
                COLUMN_X + " INTEGER, " +
                COLUMN_Y + " INTEGER " + ");";
        db.execSQL(query);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXTRAS);
        onCreate(db);

    }

    // add new route
    public void addRoute(Route route){
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE,route.getTitle());
        values.put(COLUMN_DESCRIPTION,route.getDescription());
        values.put(COLUMN_COUNTFROM,route.getCountFrom());
        values.put(COLUMN_COVER,route.getCover());

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_ROUTES,null,values);
        db.close();
        len++;
    }

    public boolean addLocus(int route_ID, Locus locus){
        SQLiteDatabase db = getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME,locus.getName());
        values.put(COLUMN_PATH,locus.getPath());
        values.put(COLUMN_THUMB,locus.getThumbnail());
        values.put(COLUMN_ROUTE_ID,route_ID);
        values.put(COLUMN_NUM,locus.getNum());
        db.insert(TABLE_LOCI, null, values);
        db.close();

        return true;
    }



    // delete route
    public void deleteRoute(String title, boolean alsoContents){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_ROUTES + " WHERE " + COLUMN_TITLE + "=\"" + title + "\";");
        len--;
        int id = getRouteID(title);
        if (alsoContents){
            deleteLoci(id);
        }
        db.close();
    }

    public void deleteLoci(int routeID){
        ArrayList<Locus> loci = getLoci(routeID);
        for (Locus l : loci){
            deleteLocus(routeID,l.getNum(),true);
        }
    }

    // delete locus
    public void deleteLocus(int routeID, int num, boolean alsoContents){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_LOCI +
                " WHERE " + COLUMN_ROUTE_ID + "=" + routeID +
                " AND " + COLUMN_NUM + " = " + num + ";");
        db.close();
        if (alsoContents){
            ArrayList<Extra> extras = getExtras(routeID,num);
            for (Extra e: extras){
                deleteExtra(e);
            }
        }
        updateUpperCount(routeID,num+1,-1);
    }


    // delete extra
    public void deleteExtra(Extra extra){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_EXTRAS + " WHERE " + COLUMN_EXTRA_ID + "=" + extra.getID());
        db.close();
    }


    public ArrayList<Route> getRoutes(){
        ArrayList<Route> routes = new ArrayList<Route>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ROUTES + " ORDER BY "+ COLUMN_TITLE + " ASC;"  ;
        // ORDER BY column_name ASC|DESC, column_name ASC|DESC;
        Cursor cursor = db.rawQuery(query,null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()){
            String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
            String descrip = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
            int countFrom = cursor.getInt(cursor.getColumnIndex(COLUMN_COUNTFROM));
            int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            String cover = cursor.getString(cursor.getColumnIndex(COLUMN_COVER));
            Route r = new Route(title,descrip,countFrom,id, cover);
            routes.add(r);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();
        return routes;

    }




    public int getIndex(String title) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + COLUMN_TITLE + " FROM " + TABLE_ROUTES +" ORDER BY "+ COLUMN_TITLE + " ASC;"  ;
        Cursor cursor = db.rawQuery(query,null);
        cursor.moveToFirst();

        int index = 0;
        boolean found = false;
        while (!cursor.isAfterLast() && !found){
            found = title.equals(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
            if (!found){
                index++;
            }
            cursor.moveToNext();
        }

        cursor.close();
        db.close();
        return index;

    }


    public int getRouteID(String title){
        if (!exists(title)) return -1;

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + COLUMN_ID +" FROM " + TABLE_ROUTES +
                " WHERE " + COLUMN_TITLE + " = \"" + title + "\";",null);
        c.moveToFirst();

        int ID = c.getInt(c.getColumnIndex(COLUMN_ID));
        c.close();
        db.close();
        return ID;
    }


    public Route getRoute(int ID){

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_ROUTES +
                " WHERE " + COLUMN_ID + " = " + ID + ";",null);
        c.moveToFirst();

        int countFrom = c.getInt(c.getColumnIndex(COLUMN_COUNTFROM));
        String title = c.getString(c.getColumnIndex(COLUMN_TITLE));
        String description = c.getString(c.getColumnIndex(COLUMN_DESCRIPTION));
        String cover = c.getString(c.getColumnIndex(COLUMN_COVER));

        Route route = new Route(title,description,countFrom,ID,cover);
        c.close();
        db.close();
        return route;
    }





    public ArrayList<Locus> getLoci(int routeID){
        ArrayList<Locus> loci = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_LOCI +
                " WHERE " + COLUMN_ROUTE_ID + " = " + routeID + " ORDER BY "+ COLUMN_NUM + " ASC;"  ;

        Cursor cursor = db.rawQuery(query,null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()){
            int num = cursor.getInt(cursor.getColumnIndex(COLUMN_NUM));
            String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
            String path = cursor.getString(cursor.getColumnIndex(COLUMN_PATH));
            String thumb = cursor.getString(cursor.getColumnIndex(COLUMN_THUMB));

            Locus locus = new Locus(num,name,path,thumb);
            loci.add(locus);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();
        return loci;
    }


    // tests if route with given title already exist
    public boolean exists(String title){
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ROUTES + " WHERE " + COLUMN_TITLE + " = \"" + title + "\";";
        Cursor c = db.rawQuery(query,null);
        boolean result = c.getCount()>0;
        c.close();
        db.close();
        return result;
    }


    public int editRoute(int routeID, String newTitle, String newDescription, int countFrom, String cover) {
        Route route = getRoute(routeID);
        int oldCount = route.getCountFrom();
        String query = "UPDATE " + TABLE_ROUTES + " SET " + COLUMN_TITLE + " = \"" + newTitle +
                "\", " + COLUMN_DESCRIPTION + " = \"" + newDescription + "\", " + COLUMN_COUNTFROM + " = " + countFrom +
                 ", " + COLUMN_COVER + " = \"" + cover + "\"" +
                 " WHERE " + COLUMN_ID + " = " + routeID + ";";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);

        db.close();
        int newID = getRouteID(newTitle);

        if (oldCount != countFrom){
            updateCount(newID, oldCount, countFrom);
        }

        return newID;

    }

    public void editLocus(int route_id, Locus edLocus) {
        String query = "UPDATE " + TABLE_LOCI + " SET " + COLUMN_NAME + " = \"" + edLocus.getName() +
                "\", " + COLUMN_PATH + " = \"" + edLocus.getPath() + "\", " + COLUMN_THUMB + " = \"" + edLocus.getThumbnail() +
                "\"" +
                " WHERE " + COLUMN_ROUTE_ID + " = " + route_id + " AND " + COLUMN_NUM + " = "+ edLocus.getNum() + ";";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);

        db.close();
    }





    public String getCover(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_ROUTES +
                " WHERE " + COLUMN_ID + " = " + id + ";",null);
        c.moveToFirst();

        String cover = c.getString(c.getColumnIndex(COLUMN_COVER));

        return cover;

    }


    private void updateCount(int routeID, int oldCount, int countFrom) {
        int difCount = countFrom - oldCount;
        SQLiteDatabase db = getWritableDatabase();
        String query = "UPDATE " + TABLE_LOCI + " SET " + COLUMN_NUM + " = " + COLUMN_NUM + " + " + difCount  +
                " WHERE " + COLUMN_ROUTE_ID + " = " + routeID + ";";

        String query2 = "UPDATE " + TABLE_EXTRAS + " SET " + COLUMN_LOCUS_NUM + " = " + COLUMN_LOCUS_NUM + " + " + difCount  +
                " WHERE " + COLUMN_ROUTE_ID + " = " + routeID + " AND " + COLUMN_LOCUS_NUM + " >= " + countFrom + ";";
        db.execSQL(query2);

        db.execSQL(query);

        db.close();
    }

    public void updateUpperCount(int routeID, int from, int diff) {
        SQLiteDatabase db = getWritableDatabase();
        String query = "UPDATE " + TABLE_LOCI + " SET " + COLUMN_NUM + " = " + COLUMN_NUM + " + " + diff  +
                " WHERE " + COLUMN_ROUTE_ID + " = " + routeID + " AND " + COLUMN_NUM + " >= " + from + ";";
        db.execSQL(query);

        String query2 = "UPDATE " + TABLE_EXTRAS +
                " SET " + COLUMN_LOCUS_NUM + " = " + COLUMN_LOCUS_NUM + " + " + diff  +
                " WHERE " + COLUMN_ROUTE_ID + " = " + routeID + " AND " + COLUMN_LOCUS_NUM + " >= " + from + ";";
        db.execSQL(query2);

        db.close();
    }

    public void updateExtraNum(int routeID, int oldNum, int newNum) {
        SQLiteDatabase db = getWritableDatabase();
        String query2 = "UPDATE " + TABLE_EXTRAS + " SET " + COLUMN_LOCUS_NUM + " = " + newNum  +
                " WHERE " + COLUMN_ROUTE_ID + " = " + routeID + " AND " + COLUMN_LOCUS_NUM + " = " + oldNum + ";";
        db.execSQL(query2);
        db.close();
    }

    public ArrayList<Extra> getExtras(int routeID , int num){
        ArrayList<Extra> extras = new ArrayList<Extra>();
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_EXTRAS +
                " WHERE " + COLUMN_ROUTE_ID + " = " + routeID +
                " AND " + COLUMN_LOCUS_NUM + " = " + num +";";
        Cursor cursor = db.rawQuery(query,null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()){
            String type = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE));
            String source = cursor.getString(cursor.getColumnIndex(COLUMN_SOURCE));
            int x = cursor.getInt(cursor.getColumnIndex(COLUMN_X));
            int y = cursor.getInt(cursor.getColumnIndex(COLUMN_Y));
            int ID = cursor.getInt(cursor.getColumnIndex(COLUMN_EXTRA_ID));

            Extra extra = new Extra(routeID,num,type,source,x,y,ID);
            extras.add(extra);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();
        return extras;
    }

    public int getExtraID(Extra e){

        int ID = -1;
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_EXTRAS +
                " WHERE " + COLUMN_ROUTE_ID + " = " + e.getRouteID() +
                " AND " + COLUMN_LOCUS_NUM + " = " + e.getLocusNum() +
                " AND " + COLUMN_X + " = " + e.getX() +
                " AND " + COLUMN_Y + " = " + e.getY() + ";";

        Cursor cursor = db.rawQuery(query,null);
        cursor.moveToFirst();

        if (!cursor.isAfterLast()){
            ID = cursor.getInt(cursor.getColumnIndex(COLUMN_EXTRA_ID));
        }

        cursor.close();
        db.close();
        return ID;
    }


    public int addExtra(int route_ID, Extra extra){
        SQLiteDatabase db = getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put(COLUMN_ROUTE_ID,route_ID);
        values.put(COLUMN_LOCUS_NUM,extra.getLocusNum());
        values.put(COLUMN_TYPE,extra.getType());
        values.put(COLUMN_SOURCE,extra.getSource());
        values.put(COLUMN_X,extra.getX());
        values.put(COLUMN_Y,extra.getY());
        db.insert(TABLE_EXTRAS, null, values);
        db.close();

        return getExtraID(extra);
    }


}

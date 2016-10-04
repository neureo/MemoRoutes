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
    private static final String TABLE_LOCI = "loci";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_COUNTFROM = "countfrom";

    private static final String COLUMN_NUM = "num";
    private static final String COLUMN_ROUTE_ID = "routeID";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PATH = "path";
    private static final String COLUMN_THUMB = "thumb";
    private static final String COLUMN_TEXT = "text";
    private static final String COLUMN_AUDIO = "audio";
    //private static final String COLUMN_COVER = "cover";
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
                COLUMN_DESCRIPTION + " TEXT " + ");";
                // COLUMN_COVER + " TEXT " + ");";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE_LOCI + "(" +
                COLUMN_NUM + " INTEGER, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PATH + " TEXT, " +
                COLUMN_THUMB + " TEXT, " +
                COLUMN_TEXT + " TEXT, " +
                COLUMN_AUDIO + " TEXT, " +
                COLUMN_ROUTE_ID + " INTEGER " + ");";
        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCI);
        onCreate(db);

    }

    // add new route
    public void addRoute(Route route){
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE,route.getTitle());
        values.put(COLUMN_DESCRIPTION,route.getDescription());
        values.put(COLUMN_COUNTFROM,route.getCountFrom());

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_ROUTES,null,values);
        db.close();
        len++;
    }
/**
    // add new locus
    public boolean addLocusByName(String route, String name, int num){
        if (!exists(route)) return false;

        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " + COLUMN_ID +" FROM " + TABLE_ROUTES +
                " WHERE " + COLUMN_TITLE + " = \"" + route + "\";",null);
        c.moveToFirst();
        int route_id = c.getInt(c.getColumnIndex(COLUMN_ID));

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME,name);
        values.put(COLUMN_ROUTE_ID,route_id);
        values.put(COLUMN_PATH, "drawable://" + R.drawable.locilobo);
        values.put(COLUMN_NUM,num);
        db.insert(TABLE_LOCI, null, values);
        db.close();
        return true;
    }

**/
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

    // add new locus
    public boolean addLocusByPath(int route_ID, String name, String path, int num){


        SQLiteDatabase db = getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME,name);
        values.put(COLUMN_PATH,path);
        values.put(COLUMN_ROUTE_ID,route_ID);
        values.put(COLUMN_NUM,num);
        db.insert(TABLE_LOCI, null, values);
        db.close();
        return true;
    }

    // delete row
    public void deleteRoute(String title){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_ROUTES + " WHERE " + COLUMN_TITLE + "=\"" + title + "\";");
        len--;
        db.close();
    }

    // delete row
    public void deleteLocus(int routeID, int num){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_LOCI + " WHERE " + COLUMN_ROUTE_ID + "=" + routeID + " AND " + COLUMN_NUM + " = " + num + ";");
        len--;
        db.close();
        updateUpperCount(routeID,num+1,-1);
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
            Route r = new Route(title,descrip,countFrom,id);
            routes.add(r);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();
        return routes;

    }

    public Route getRoute(String title){
        if (!exists(title)) return null;

        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ROUTES +
                " WHERE " + COLUMN_TITLE + " = \"" + title + "\";"  ;
        Cursor cursor = db.rawQuery(query,null);
        cursor.moveToFirst();

        String descrip = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
        int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
        int countFrom = cursor.getInt(cursor.getColumnIndex(COLUMN_COUNTFROM));
        Route r = new Route(title,descrip,countFrom,id);

        cursor.close();
        db.close();
        return r;

    }

    public ArrayList<String> getTitles(){

        ArrayList<String> titles = new ArrayList<String>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + COLUMN_TITLE + " FROM " + TABLE_ROUTES +" ORDER BY "+ COLUMN_TITLE + " ASC;"  ;
        Cursor cursor = db.rawQuery(query,null);
        cursor.moveToFirst();

        int index = 0;
        while (!cursor.isAfterLast()){
            String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
            titles.add(title);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();
        return titles;

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

        Route route = new Route(title,description,countFrom,ID);
        c.close();
        db.close();
        return route;
    }




    public ArrayList<String> getLociNames(String routeTitle){

        ArrayList<String> names = new ArrayList<String>();
        int routeID = getRouteID(routeTitle);

        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + COLUMN_NAME + " FROM " + TABLE_LOCI +
                " WHERE " + COLUMN_ROUTE_ID + " = "+routeID+";"  ;
        Cursor cursor = db.rawQuery(query,null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()){
            if (cursor.getCount()>0){
                names.add(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                cursor.moveToNext();
            }
        }

        cursor.close();
        db.close();
        return names;

    }

    public ArrayList<Locus> getLoci(int routeID){
        ArrayList<Locus> loci = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_LOCI +
                " WHERE " + COLUMN_ROUTE_ID + " = " + routeID + ";";
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


    public void editRoute(int routeID, String newTitle, String newDescription, int countFrom) {
        Route route = getRoute(routeID);
        int oldCount = route.getCountFrom();
        String query = "UPDATE " + TABLE_ROUTES + " SET " + COLUMN_TITLE + " = \"" + newTitle +
                "\", " + COLUMN_DESCRIPTION + " = \"" + newDescription + "\", " + COLUMN_COUNTFROM + " = " + countFrom +
                " WHERE " + COLUMN_ID + " = " + routeID + ";";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);

        db.close();
        int newID = getRouteID(newTitle);

        if (oldCount != countFrom){
            updateCount(newID, oldCount, countFrom);
        }


    }

    private void updateCount(int routeID, int oldCount, int countFrom) {
        int difCount = countFrom - oldCount;
        SQLiteDatabase db = getWritableDatabase();
        String query = "UPDATE " + TABLE_LOCI + " SET " + COLUMN_NUM + " = " + COLUMN_NUM + " + " + difCount  +
                " WHERE " + COLUMN_ROUTE_ID + " = " + routeID + ";";
        db.execSQL(query);

        db.close();
    }

    private void updateUpperCount(int routeID, int from, int diff) {
        SQLiteDatabase db = getWritableDatabase();
        String query = "UPDATE " + TABLE_LOCI + " SET " + COLUMN_NUM + " = " + COLUMN_NUM + " + " + diff  +
                " WHERE " + COLUMN_ROUTE_ID + " = " + routeID + " AND " + COLUMN_NUM + " >= " + from + ";";
        db.execSQL(query);

        db.close();
    }

}

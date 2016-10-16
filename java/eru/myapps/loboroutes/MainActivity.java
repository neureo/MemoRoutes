package eru.myapps.loboroutes;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    Button addButton;
    ArrayList<Route> routes;

    ListView routeListView;
    ArrayAdapter<Route> routeAdapter;
    TextView initScreen;
    public static DBHandler dbHandler;
    public static final int REQUEST_READ_EXTERNAL_STORAGE_CODE = 22;
    public static final int REQUEST_RECORD_AUDIO = 23;
    public static final int PICK_FROM_CAMERA = 100;
    public static final int PICK_FROM_MEMORY = 1;
    public static final int REQUEST_NEW_ROUTE = 2;
    public static final int REQUEST_EDIT_ROUTE = 3;
    private static final int REQUEST_OPEN_ROUTE = 4;
    public static String folder_main = "MemoRoute";
    public static String folder_extra = folder_main + "/extras";
    public static String folder_loci = folder_main + "/loci";

    public static final String TEXT_DEFAULT = "default";
    public static final String PREFERENCES = "App_Preferences";
    public static final String FIRST_RUN_KEY = "App_Preferences";
    private int lastCalledRouteIndex;
    AlertDialog deleteDialog;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private boolean firstRun;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        routeListView = (ListView) findViewById(R.id.routelist);
        addButton = (Button) findViewById( R.id.addnew);
        initScreen = (TextView) findViewById(R.id.initializeScreen);


        createFolder(folder_main);
        createFolder(folder_extra);
        createFolder(folder_loci);

        preferences = getSharedPreferences(PREFERENCES,MODE_PRIVATE);
        editor = preferences.edit();
        firstRun = preferences.getBoolean(FIRST_RUN_KEY,true);
        //firstRun = true;

        dbHandler = new DBHandler(this,null,null,1);

        if (firstRun){
            askPermissions();
            //Toast.makeText(getApplicationContext(),"Creating Tutorial, please wait",Toast.LENGTH_LONG).show();
        }

        routes = dbHandler.getRoutes();

        //dbHandler.onUpgrade(dbHandler.getWritableDatabase(),2,3);

        routeAdapter = new RouteAdapter2(this,routes);

        routeListView.setAdapter(routeAdapter);

        registerForContextMenu(routeListView);

        routeListView.setOnItemClickListener(new ListView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Route route = routes.get(i);
                Intent routeIntent = new Intent(MainActivity.this,RouteActivity.class);
                Bundle routeInfo = new Bundle();
                routeInfo.putInt("id",route.getId());
                routeIntent.putExtras(routeInfo);
                lastCalledRouteIndex = i;
                startActivityForResult(routeIntent,REQUEST_OPEN_ROUTE);
            }
        });


        // retain instances
        //LinkedList<String> titles = new LinkedList<String>();

        addButton.setOnClickListener(new Button.OnClickListener(){

            public void onClick(View v){
                Intent newRouteIntent = new Intent(MainActivity.this,NewRoute.class);
                newRouteIntent.putExtra("edit",false);
                startActivityForResult(newRouteIntent,REQUEST_NEW_ROUTE);
            }

        });



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE_CODE){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                initScreen.setVisibility(View.VISIBLE);
                addButton.setVisibility(View.INVISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                createTutorialRoute();

            }
        }
    }

    private boolean createTutorialRoute() {

        int[] tutRes = {R.drawable.tut1,R.drawable.tut2,R.drawable.tut3,R.drawable.tut4,R.drawable.tut5, R.drawable.tut6};
        ArrayList<Bitmap>  bitmaps = new ArrayList<>();
        for (int res:tutRes){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),res);
            Bitmap thumb =  scaleBitmap(bitmap,300,300);

            bitmaps.add(bitmap);
        }


        String title = "Tutorial";
        String desc = "Here you will see your route's description";
        Route tutorial = new Route(title,desc,1,0);
        dbHandler.addRoute(tutorial);
        tutorial.setId(dbHandler.getRouteID(title));

        int num = 1;
        for (Bitmap bitmap: bitmaps){
            String stamp = String.valueOf(System.currentTimeMillis());

            File fullFile = new File(Environment.getExternalStorageDirectory(), MainActivity.folder_loci + "/locus_" + stamp + ".png");
            File thumbFile = new File(Environment.getExternalStorageDirectory(), MainActivity.folder_loci + "/thumb_" + stamp + ".png");

            Bitmap thumb = scaleBitmap(bitmap,300,300);
            boolean end = (num >= bitmaps.size());
            BitmapSaverTask fullTask = new BitmapSaverTask(getApplicationContext(), bitmap, fullFile.getPath(),end);
            fullTask.execute();

            BitmapSaverTask thumbTask = new BitmapSaverTask(getApplicationContext(), thumb, thumbFile.getPath(),false);
            thumbTask.execute();


            Locus locus = new Locus(num,"",fullFile.getAbsolutePath(),thumbFile.getAbsolutePath());
            dbHandler.addLocus(tutorial.getId(),locus);
            num++;
        }

        return true;
    }

    private void createFolder(String folder) {
        File f = new File(Environment.getExternalStorageDirectory(), folder);
        if (!f.exists()) {
            f.mkdirs();
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_context_menu,menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        routeAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.ask_del_route));
        builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Route remRoute = routes.remove(info.position);
                dbHandler.deleteRoute(remRoute.getTitle(),true);
                routeAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteDialog.dismiss();
            }
        });

        deleteDialog = builder.create();


        switch (item.getItemId()) {
            case (R.id.menu_delete):{
                deleteDialog.show();
                return true;
            }
            case R.id.menu_cancel :{
                return false;
            }
            case R.id.menu_edit :{
                Intent editIntent = new Intent(getApplicationContext(),NewRoute.class);
                Route editRoute = routes.get(info.position);

                editIntent.putExtra("edit",true);
                editIntent.putExtra("title",editRoute.getTitle());
                editIntent.putExtra("description",editRoute.getDescription());
                editIntent.putExtra("countFrom",editRoute.getCountFrom());
                startActivityForResult(editIntent,REQUEST_EDIT_ROUTE);
                return true;
            }

        }
        return super.onContextItemSelected(item);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){

            Bundle letter = data.getExtras();
            String newTitle = letter.getString("title");
            String newDescription = letter.getString("description");
            int countFrom = letter.getInt("count");
            Route newRoute = new Route(newTitle, newDescription, countFrom, 0);

            if (requestCode == REQUEST_NEW_ROUTE) {
                Toast.makeText(MainActivity.this, newTitle, Toast.LENGTH_LONG).show();
                dbHandler.addRoute(newRoute);
                int routeID = dbHandler.getRouteID(newRoute.getTitle());
                int index = dbHandler.getIndex(newRoute.getTitle());
                newRoute.setId(routeID);
                routes.add(index,newRoute);
            }

            if (requestCode == REQUEST_EDIT_ROUTE){
                int index = letter.getInt("index");
                Route route = routes.get(index);
                int routeID = dbHandler.getRouteID(route.getTitle());
                dbHandler.editRoute(routeID,newTitle,newDescription,countFrom,route.getCover());
                int newID = dbHandler.getRouteID(newTitle);
                newRoute.setId(newID);
                newRoute.setCover(route.getCover());
                routes.set(index,newRoute);
            }

            if (requestCode == REQUEST_OPEN_ROUTE){
                int newID = letter.getInt("newID");
                String newCover = (dbHandler.getCover(newID));
                Route route = routes.get(lastCalledRouteIndex);
                route.setCover(newCover);
                route.setId(newID);
                routes.set(lastCalledRouteIndex,route);
            }

            routeAdapter.notifyDataSetChanged();

        }
    }

    public boolean askPermissions(){
        if ( Build.VERSION.SDK_INT > 22 && ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    MainActivity.REQUEST_READ_EXTERNAL_STORAGE_CODE);

        } else if (Build.VERSION.SDK_INT <= 22 && ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            initScreen.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.INVISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            createTutorialRoute();

        }
        return true;

    }

    class BitmapSaverTask extends AsyncTask<Void, Void, Void> {
        private Bitmap image;
        private String path;
        private boolean end;
        private Context context;

        public BitmapSaverTask(Context context,Bitmap image, String path, boolean end){
            this.image = image;
            this.path = path;
            this.context = context;
            this.end = end;
        }


        @Override
        protected Void doInBackground(Void... params) {
            File file = new File(path);
            FileOutputStream out = null;
            try{
                out = new FileOutputStream(file);
                image.compress(Bitmap.CompressFormat.JPEG,100,out);
            }catch(Exception ex){
                ex.printStackTrace();
            }finally {
                if (out != null){
                    try{
                        out.close();
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (end) {
                routeAdapter.notifyDataSetChanged();
                //Toast.makeText(getApplicationContext(),"Tutorial created!",Toast.LENGTH_SHORT).show();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                initScreen.setVisibility(View.INVISIBLE);
                addButton.setVisibility(View.VISIBLE);
                routes = dbHandler.getRoutes();
                routeAdapter = new RouteAdapter2(getApplicationContext(), routes);
                routeListView.setAdapter(routeAdapter);
                routeAdapter.notifyDataSetChanged();
                editor.putBoolean(FIRST_RUN_KEY, false);
                editor.commit();
            }

        }

    }

    public static Bitmap scaleBitmap(Bitmap bm, int maxW, int maxH){
        int width, height;
        Bitmap scaled;
        float aspectRatio = bm.getWidth() / (float) bm.getHeight();
        if (aspectRatio > 1){
            width = maxW;
            height = Math.round(width/aspectRatio);
        } else {
            height = maxH;
            width = Math.round(height*aspectRatio);
        }

        scaled = Bitmap.createScaledBitmap(bm,width,height,false);
        return scaled;

    }




}

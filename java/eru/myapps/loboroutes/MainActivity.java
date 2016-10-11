package eru.myapps.loboroutes;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.util.ArrayList;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    Button addButton;
    ArrayList<Route> routes;

    ListView routeListView;
    ArrayAdapter<Route> routeAdapter;
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
    private int lastCalledRouteIndex;
    AlertDialog deleteDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createFolder(folder_main);
        createFolder(folder_extra);
        createFolder(folder_loci);



        dbHandler = new DBHandler(this,null,null,1);
        routes = dbHandler.getRoutes();

        //dbHandler.onUpgrade(dbHandler.getWritableDatabase(),2,3);

        routeAdapter = new RouteAdapter2(this,routes);

        routeListView = (ListView) findViewById(R.id.routelist);
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
        addButton = (Button) findViewById( R.id.addnew);
        //LinkedList<String> titles = new LinkedList<String>();

        addButton.setOnClickListener(new Button.OnClickListener(){

            public void onClick(View v){
                Intent newRouteIntent = new Intent(MainActivity.this,NewRoute.class);
                newRouteIntent.putExtra("edit",false);
                startActivityForResult(newRouteIntent,REQUEST_NEW_ROUTE);
            }

        });



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

    public void askPermissions(){
        if ( Build.VERSION.SDK_INT > 22 && ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},MainActivity.REQUEST_READ_EXTERNAL_STORAGE_CODE);
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_SHORT).show();
                return;
            }
        }

    }



}

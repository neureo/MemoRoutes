package eru.myapps.loboroutes;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class RouteActivity extends AppCompatActivity {

    ListView lociGrid;
    DBHandler dbHandler;
    ArrayList<Locus> loci;
    LociAdapter lociAdapter;
    TextView routeTitleView;
    Button addButton;
    String route_title;
    int countFrom;
    int route_ID;
    Route route;
    public final static int REQUEST_NEW_LOCUS = 3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        dbHandler = new DBHandler(this,null,null,1);

        Bundle routeInfo = getIntent().getExtras();
        route_title = routeInfo.getString("title");
        route_ID = dbHandler.getRouteID(route_title);
        route = dbHandler.getRoute(route_ID);


        routeTitleView = (TextView) findViewById(R.id.routeTitleView);
        routeTitleView.setText(route.getTitle());
        lociGrid = (ListView) findViewById(R.id.loci_grid);
        loci = dbHandler.getLoci(route_ID);
        countFrom = route.getCountFrom();


        addButton = (Button) findViewById(R.id.add_button);


        lociAdapter = new LociAdapter(this,loci);
        lociGrid.setAdapter(lociAdapter);
        lociGrid.setOnItemClickListener(new ListView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent pagerIntent = new Intent(getApplicationContext(),LocusActivity.class);
                pagerIntent.putExtra("routeID",route_ID);
                pagerIntent.putExtra("pos",i);
                startActivity(pagerIntent);
            }

        });


        addButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newLocusIntent = new Intent(getApplicationContext(),NewLocusActivity.class);
                startActivityForResult(newLocusIntent,REQUEST_NEW_LOCUS);
                //dialog.show();
            }
        });



    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            String mssg = "Result not ok: " + requestCode;
            Toast.makeText(getApplicationContext(),mssg,Toast.LENGTH_LONG).show();
            return;
        }

        if (requestCode == REQUEST_NEW_LOCUS){

            Bundle extras = data.getExtras();
            String name = extras.getString("name");
            String path = extras.getString("imgPath");
            String thumbPath = extras.getString("thumbPath");



            Locus locus = new Locus(loci.size() + countFrom,name,path,thumbPath);
            dbHandler.addLocus(route_ID,locus);
            loci.add(locus);
            lociAdapter.notifyDataSetChanged();

        }




    }

    public void refresh() {
        lociAdapter.notifyDataSetChanged();
    }




}

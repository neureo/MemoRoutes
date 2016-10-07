package eru.myapps.loboroutes;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class RouteActivity extends AppCompatActivity {

    ListView lociGrid;
    DBHandler dbHandler;
    ArrayList<Locus> loci;
    LociAdapter lociAdapter;
    TextView routeTitleView;
    Button addButton;
//    String route_title;
    int countFrom;
    int route_ID;
    Route route;
    public final static int REQUEST_NEW_LOCUS = 3;
    Intent callbackIntent = new Intent();
    AlertDialog deleteDialog;
    AlertDialog moveDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        dbHandler = new DBHandler(this,null,null,1);

        Bundle routeInfo = getIntent().getExtras();
        route_ID = routeInfo.getInt("id");;
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

        registerForContextMenu(lociGrid);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.loci_context_menu,menu);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int locusNum = loci.get(info.position).getNum();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete this locus?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Locus remLocus = loci.remove(info.position);
                dbHandler.deleteLocus(route_ID,remLocus.getNum(),true);
                for (int index = remLocus.getNum() - route.getCountFrom(); index < loci.size();index ++){
                    Locus newLocus = loci.get(index);
                    newLocus.setNum(newLocus.getNum()-1);
                    loci.set(index,newLocus);
                }
                lociAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteDialog.dismiss();
            }
        });

        deleteDialog = builder.create();


        builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.choose_pos));
        View builderView = getLayoutInflater().inflate(R.layout.dialog_move_locus,null);
        builder.setView(builderView);
        final NumberPicker picker = (NumberPicker) builderView.findViewById(R.id.locus_posPicker);
        picker.setMinValue(route.getCountFrom());
        picker.setMaxValue(route.getCountFrom() + loci.size()-1);
        picker.setValue(route.getCountFrom()+info.position);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int choice = picker.getValue();
                if (choice != loci.get(info.position).getNum()){
                    Locus editLocus = loci.remove(info.position);
                    for(int index = info.position; index < loci.size(); index++){
                        Locus locus = loci.get(index);
                        locus.setNum(locus.getNum()-1);
                        loci.set(index,locus);
                    }
                    dbHandler.deleteLocus(route_ID,editLocus.getNum(),false);
                    editLocus.setNum(choice);
                    int newIndex = choice - route.getCountFrom();
                    loci.add(newIndex,editLocus);
                    for(int index = newIndex+1; index < loci.size(); index++){
                        Locus locus = loci.get(index);
                        locus.setNum(locus.getNum()+1);
                        loci.set(index,locus);
                    }
                    dbHandler.updateExtraNum(route_ID,locusNum,choice);
                    dbHandler.updateUpperCount(route_ID,choice+1,+1);
                    dbHandler.addLocus(route_ID,editLocus);
                }
                lociAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteDialog.dismiss();
            }
        });

        moveDialog = builder.create();

        switch (item.getItemId()) {
            case (R.id.menu_loci_delete):{
                deleteDialog.show();
                return true;
            }
            case (R.id.menu_loci_move):{
                moveDialog.show();
                return true;
            }
            case R.id.menu_loci_cancel :{
                return false;
            }
            case R.id.menu_loci_setCover :{
                route.setCover(loci.get(info.position).getThumbnail());
                int newID = dbHandler.editRoute(route_ID,route.getTitle(),route.getDescription(),route.getCountFrom(),route.getCover());
                route.setId(newID);
                route_ID = newID;
                callbackIntent.putExtra("newID",route_ID);
                setResult(RESULT_OK,callbackIntent);
                return true;
            }

        }
        return super.onContextItemSelected(item);

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
            String path, thumbPath;
            path = extras.getString("imgPath");
            thumbPath = extras.getString("thumbPath");
            if (route.getCover().equals(MainActivity.TEXT_DEFAULT)){
                route.setCover(thumbPath);
                int newID = dbHandler.editRoute(route_ID,route.getTitle(),route.getDescription(),route.getCountFrom(),route.getCover());
                route.setId(newID);
                route_ID = newID;
                callbackIntent.putExtra("newID",newID);
                setResult(RESULT_OK,callbackIntent);
            }


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

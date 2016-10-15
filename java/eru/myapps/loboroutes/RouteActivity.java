package eru.myapps.loboroutes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
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
    Button infoButton;
//    String route_title;
    int countFrom;
    int route_ID;
    Route route;
    public final static int REQUEST_NEW_LOCUS = 3;
    private static final int REQUEST_EDIT_LOCUS = 4;
    Intent callbackIntent = new Intent();
    AlertDialog deleteDialog;
    AlertDialog moveDialog;
    AlertDialog infoDialog;



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
        infoButton = (Button) findViewById(R.id.route_Info);

        final Context context = this ;

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = getLayoutInflater();
                View builderView = inflater.inflate(R.layout.dialog_show_text,null);
                builder.setView(builderView);

                TextView extraView = ( TextView) builderView.findViewById(R.id.extra_showTextView);
                String routeInfo = route.getDescription();
                if (routeInfo.length() == 0){
                    routeInfo = "No description found! \n\n" +
                            "Go back to your routes overview, then press and hold selected route to edit.";
                }
                final String copyText = routeInfo;
                extraView.setText(routeInfo);
                extraView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        ClipData clip = ClipData.newPlainText("Copied",copyText);
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context,"Text copied",Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                infoDialog = builder.create();
                infoDialog.show();

            }
        });


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
                newLocusIntent.putExtra("edit",false);
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
        final Locus locus = loci.get(info.position);
        final int locusNum = locus.getNum();

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
                    ArrayList<Extra> extras = dbHandler.getExtras(route_ID,locusNum);
                    dbHandler.deleteLocus(route_ID,editLocus.getNum(),true);
                    editLocus.setNum(choice);
                    int newIndex = choice - route.getCountFrom();
                    loci.add(newIndex,editLocus);
                    for(int index = newIndex+1; index < loci.size(); index++){
                        Locus locus = loci.get(index);
                        locus.setNum(locus.getNum()+1);
                        loci.set(index,locus);
                    }
                    dbHandler.updateUpperCount(route_ID,choice,+1);
                    dbHandler.addLocus(route_ID,editLocus);
                    for (Extra e : extras){
                        e.setLocusNum(choice);
                        dbHandler.addExtra(route_ID,e);
                    }
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
            case (R.id.menu_loci_edit):{
                Intent editIntent = new Intent(getApplicationContext(),NewLocusActivity.class);
                editIntent.putExtra("edit",true);
                editIntent.putExtra("path",locus.getThumbnail());
                editIntent.putExtra("name",locus.getName());
                editIntent.putExtra("pos",info.position);
                startActivityForResult(editIntent,REQUEST_EDIT_LOCUS);
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
            return;
        }

        Bundle extras = data.getExtras();

        if (requestCode == REQUEST_NEW_LOCUS){

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

        if (requestCode == REQUEST_EDIT_LOCUS){
            int pos = extras.getInt("pos");
            Locus edLocus = loci.remove(pos);

            boolean imgEdited = extras.getBoolean("edited");
            String name = extras.getString("name");
            String path, thumbPath;
            path = extras.getString("imgPath");
            thumbPath = extras.getString("thumbPath");
            if (imgEdited) {
                edLocus.setPath(path);
                edLocus.setThumbnail(thumbPath);
            }
            edLocus.setName(name);
            dbHandler.editLocus(route_ID,edLocus);
            loci.add(pos,edLocus);


            lociAdapter.notifyDataSetChanged();

        }



    }

    public void refresh() {
        lociAdapter.notifyDataSetChanged();
    }




}

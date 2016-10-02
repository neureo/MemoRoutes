package eru.myapps.loboroutes;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class LocusActivity extends AppCompatActivity {

    ViewPager pager;
    LocusPageAdapter adapter;
    ArrayList<Locus> loci;
    int routeID;
    int pos;
    DBHandler dbHandler = new DBHandler(this,null,null,1);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locus);
        pager = (ViewPager) findViewById(R.id.pager);

        Bundle extras = getIntent().getExtras();
        routeID = extras.getInt("routeID");
        pos = extras.getInt("pos");
        loci = dbHandler.getLoci(routeID);
        adapter = new LocusPageAdapter(this,loci);

        pager.setAdapter(adapter);
        pager.setCurrentItem(pos);
    }
}

package eru.myapps.loboroutes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by elopezmo on 29.09.16.
 */
public class LocusPageAdapter extends PagerAdapter {

    private ArrayList<Locus> loci;
    private ArrayList<Extra> extras;
    private Context context;
    private LayoutInflater inflater;
    private RelativeLayout locusView;
    private Button lastHook = null;
    private RelativeLayout hookGroup = null;
    private boolean addingExtra = false;
    private int routeID;

    float hookX = 0;
    float hookY = 0;
    LocusPageAdapter(Context ctx, int routeID, ArrayList<Locus> loci){
        context = ctx;
        this.loci = loci;
        this.routeID = routeID;
    }

    @Override
    public int getCount() {
        return loci.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == (RelativeLayout) object );
    }


    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        final Locus locus = loci.get(position);
        extras = MainActivity.dbHandler.getExtras(routeID,locus.getNum());

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        locusView = (RelativeLayout) inflater.inflate(R.layout.full_locus,container,false);
        ImageView imageView = (ImageView) locusView.findViewById(R.id.imageView);
        TextView nameView = (TextView) locusView.findViewById(R.id.locus_full_name);
        final LinearLayout textLayout = (LinearLayout) locusView.findViewById(R.id.locus_textLayout);
        final LinearLayout addExtras = (LinearLayout) locusView.findViewById(R.id.locus_add_extras);
        final Button textButton = (Button) locusView.findViewById(R.id.locus_textButton);
        final Button microButton = (Button) locusView.findViewById(R.id.locus_microButton);
        final Button imgButton = (Button) locusView.findViewById(R.id.locus_imgButton);
        final Button cancelButton = (Button) locusView.findViewById(R.id.locus_extra_cancel);


        nameView.setText(locus.getName());


        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastHook.setBackgroundResource(R.drawable.abc_on);
                Extra newExtra = new Extra(routeID,locus.getNum(),Extra.TYPE_TEXT,"",(int)hookX,(int)hookY,-1);
                newExtra.setID(MainActivity.dbHandler.addExtra(routeID,locus.getNum(),newExtra));
                extras.add(newExtra);
                addExtras.setVisibility(View.INVISIBLE);
                addingExtra = false;
            }
        });

        microButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastHook.setBackgroundResource(R.drawable.microphone);
                Extra newExtra = new Extra(routeID,locus.getNum(),Extra.TYPE_AUDIO,"",(int)hookX,(int)hookY,-1);
                newExtra.setID(MainActivity.dbHandler.addExtra(routeID,locus.getNum(),newExtra));
                extras.add(newExtra);
                addExtras.setVisibility(View.INVISIBLE);
                addingExtra = false;
            }
        });

        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastHook.setBackgroundResource(R.drawable.img_on);
                Extra newExtra = new Extra(routeID,locus.getNum(),Extra.TYPE_IMG,"",(int)hookX,(int)hookY,-1);
                newExtra.setID(MainActivity.dbHandler.addExtra(routeID,locus.getNum(),newExtra));
                extras.add(newExtra);
                addExtras.setVisibility(View.INVISIBLE);
                addingExtra = false;
            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addExtras.setVisibility(View.INVISIBLE);
                hookGroup.removeView(lastHook);
                addingExtra = false;
            }
        });


        if(locus.getPath().equals(MainActivity.TEXT_DEFAULT)){
            imageView.setImageResource(R.drawable.locus_default);
        }else {
            imageView.setImageBitmap(BitmapFactory.decodeFile(locus.getPath()));
        }

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    hookX = event.getX();
                    hookY = event.getY();
                }
                return false;
            }
        });

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (addingExtra){
                    hookGroup.removeView(lastHook);
                }
                addingExtra = true;
                int x = Math.round(hookX);
                int y = Math.round(hookY) ;
                hookGroup = (RelativeLayout) view.getParent();
                Toast.makeText(context,"pos:("+ x+","+y+")",Toast.LENGTH_SHORT).show();
                addExtras.setVisibility(View.VISIBLE);

                Button newHook = new Button(container.getContext());
                newHook.setBackgroundResource(R.drawable.new_hook);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        50,50);
                //params.setMargins(50,50,0,0);

                params.setMargins(x -25 - 16 ,y + 25 + 16 ,0,0); // ?????



                newHook.setLayoutParams(params);
                hookGroup.addView(newHook,hookGroup.getChildCount());
                lastHook = newHook;
                return true;
            }
        });


        addHooks(locusView,extras);

        container.addView(locusView);
        return locusView;
    }

    private void addHooks(RelativeLayout locusView, ArrayList<Extra> extras) {
        for (Extra e :extras){
            int x = e.getX();
            int y = e.getY();
            String type = e.getType();
            Button newHook = new Button(locusView.getContext());
            if (type.equals(Extra.TYPE_TEXT)){
                newHook.setBackgroundResource(R.drawable.abc_on);
            }else if (type.equals(Extra.TYPE_AUDIO)){
                newHook.setBackgroundResource(R.drawable.microphone);
            }else{ // type img
                newHook.setBackgroundResource(R.drawable.img_on);
            }
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    50,50);

            params.setMargins(x -25 - 16 ,y + 25 + 16 ,0,0); // ?????

            newHook.setLayoutParams(params);
            locusView.addView(newHook,locusView.getChildCount());

        }
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }


}
/**
    public void onClick(View view) {
        int visibility = textLayout.getVisibility();
        if (visibility == View.VISIBLE){
            textLayout.setVisibility(View.INVISIBLE);
            textButton.setBackgroundResource(R.drawable.abc_off);
        }else{
            textLayout.setVisibility(View.VISIBLE);
            textButton.setBackgroundResource(R.drawable.abc_on);
        }
    }
**/
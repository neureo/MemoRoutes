package eru.myapps.loboroutes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by elopezmo on 29.09.16.
 */
public class LocusPageAdapter extends PagerAdapter {

    private ArrayList<Locus> loci;
    private Context context;
    private LayoutInflater inflater;
    LocusPageAdapter(Context ctx, ArrayList<Locus> loci){
        context = ctx;
        this.loci = loci;
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
    public Object instantiateItem(ViewGroup container, int position) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View locusView = inflater.inflate(R.layout.full_locus,container,false);
        ImageView imageView = (ImageView) locusView.findViewById(R.id.imageView);
        TextView textView = (TextView) locusView.findViewById(R.id.locus_full_name);
        Locus locus = loci.get(position);
        textView.setText(locus.getName());
        imageView.setImageBitmap(BitmapFactory.decodeFile(locus.getPath()));
        container.addView(locusView);
        return locusView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }


}

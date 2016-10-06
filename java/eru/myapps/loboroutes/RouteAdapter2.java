package eru.myapps.loboroutes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by eru on 24.04.16.
 */
class RouteAdapter2 extends ArrayAdapter<Route> {
    public RouteAdapter2(Context context, ArrayList<Route> routes) {
        super(context, R.layout.custom_row ,routes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.custom_row,parent,false);
        Route route = getItem(position);
        TextView titleView = (TextView) view.findViewById(R.id.route_title);
        ImageView coverView = (ImageView) view.findViewById(R.id.route_cover);

        titleView.setText(route.getTitle());
        String cover = route.getCover();
        if (cover.equals(MainActivity.TEXT_DEFAULT)) {
            coverView.setImageResource(R.drawable.locus_default);
        }else{
            Bitmap bitmap = BitmapFactory.decodeFile(route.getCover());
            int h = bitmap.getHeight();
            int w = bitmap.getWidth();
            int dim = Math.min(h, w);
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, dim, dim);
            coverView.setImageBitmap(bitmap);
        }

        return view;

    }
}

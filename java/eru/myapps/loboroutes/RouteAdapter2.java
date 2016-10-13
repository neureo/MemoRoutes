package eru.myapps.loboroutes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
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
            BitmapLoader loader = new BitmapLoader(coverView,route.getCover());
            loader.execute();
               /**
            Bitmap bitmap = BitmapFactory.decodeFile(route.getCover());
            int h = bitmap.getHeight();
            int w = bitmap.getWidth();
            int dim = Math.min(h, w);
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, dim, dim);
            coverView.setImageBitmap(addDoubleBorder(bitmap,2));
                **/
        }

        return view;

    }

    class BitmapLoader extends AsyncTask<Void, Void, Void> {
        private final WeakReference<ImageView> imageViewReference;
        private int data = 0;
        private Bitmap image = null;
        private String path;

        public BitmapLoader(ImageView imageView, String coverPath){
            imageViewReference = new WeakReference<ImageView>(imageView);
            path = coverPath;
        }


        @Override
        protected Void doInBackground(Void... params) {
            image = BitmapFactory.decodeFile(path);
            int w = image.getWidth();
            int h = image.getHeight();
            int dim = Math.min(w,h);
            image = ThumbnailUtils.extractThumbnail(image,dim,dim);
            image = addDoubleBorder(image,2);
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            //        imView.setImageBitmap(decodeBitmapFromPath(locus.getPath(),300,200));
            if (imageViewReference != null && image != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(image);
                    //notifyDataSetChanged();
                }
            }
            super.onPostExecute(aVoid);

        }

    }

    private Bitmap addDoubleBorder(Bitmap bmp, int bordersize){
        Bitmap singleborder = addBorder(bmp,bordersize, Color.WHITE);
        return addBorder(singleborder,bordersize,Color.BLACK);
    }


    private Bitmap addBorder(Bitmap bmp, int borderSize, int color) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(color);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }


}

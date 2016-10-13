package eru.myapps.loboroutes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.util.EventLogTags;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by eru on 25.04.16.
 */
public class LociAdapter extends ArrayAdapter<Locus> {
    Context context;
    public LociAdapter(Context context, ArrayList<Locus> ids) {
        super(context, R.layout.loci_box, ids);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());


        View locusView = inflater.inflate(R.layout.loci_box, parent, false);
        TextView numView = (TextView) locusView.findViewById(R.id.loci_num);
        ImageView imView = (ImageView) locusView.findViewById(R.id.loci_image);
        TextView nameView = (TextView) locusView.findViewById(R.id.locus_name);

        Locus locus = getItem(position);

        if (locus.getPath().equals(MainActivity.TEXT_DEFAULT)) {
            imView.setImageResource(R.drawable.locus_default);
        } else {
            BitmapLoader bitmapLoader = new BitmapLoader(imView, locus);
            bitmapLoader.execute();
        }

        numView.setText(String.valueOf(locus.getNum()));
        nameView.setText(locus.getName());


        return locusView;
    }




    class BitmapLoader extends AsyncTask<Void, Void, Void> {
        private final WeakReference<ImageView> imageViewReference;
        private int data = 0;
        private Bitmap image = null;
        private Locus locus;

        public BitmapLoader(ImageView imageView, Locus l){
            imageViewReference = new WeakReference<ImageView>(imageView);
            locus = l;
        }


        @Override
        protected Void doInBackground(Void... params) {
            image = BitmapFactory.decodeFile(locus.getThumbnail());
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

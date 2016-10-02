package eru.myapps.loboroutes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
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


        View locusView = inflater.inflate(R.layout.loci_box,parent,false);
        TextView numView = (TextView) locusView.findViewById(R.id.loci_num);
        ImageView imView = (ImageView) locusView.findViewById(R.id.loci_image);
        TextView nameView = (TextView) locusView.findViewById(R.id.locus_name);

        Locus locus = getItem(position);

        BitmapLoader bitmapLoader = new BitmapLoader(imView,locus);
        bitmapLoader.execute();

//        Bitmap thumbnail = BitmapFactory.decodeFile(locus.getThumbnail());

        numView.setText(String.valueOf(locus.getNum()));
//        imView.setImageBitmap(thumbnail);
        nameView.setText(locus.getName());


    return locusView;
    }

    public Bitmap decodeBitmapFromPath(String res, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(res, options);
    }


    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight/2;
        final int width = options.outWidth/2;
        if (height == 0){
            return 2;
        }
        if (width == 0){
            return 1;
        }
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;

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

}

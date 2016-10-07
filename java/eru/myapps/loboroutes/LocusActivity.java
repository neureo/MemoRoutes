package eru.myapps.loboroutes;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class LocusActivity extends AppCompatActivity {

    ViewPager pager;
    LocusPageAdapter adapter;
    ArrayList<Locus> loci;
    int routeID;
    int pos;
    DBHandler dbHandler = new DBHandler(this,null,null,1);
    RelativeLayout locusView;
    TextView locusTextView;
    boolean textVisible = false;
    AlertDialog dialog;
    int lastX = 0;
    int lastY = 0;
    int lastNum = 0;
    Uri imageCaptureUri;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locus);
        context = getApplicationContext();

        pager = (ViewPager) findViewById(R.id.pager);

        Bundle extras = getIntent().getExtras();
        routeID = extras.getInt("routeID");
        pos = extras.getInt("pos");
        loci = dbHandler.getLoci(routeID);
        adapter = new LocusPageAdapter(this,routeID,loci);

        pager.setAdapter(adapter);
        pager.setCurrentItem(pos);
    }

    void sendCameraIntent(final RelativeLayout locusView, int x, int y, int num){
        this.locusView = locusView;
        lastX = x;
        lastY = y;
        lastNum = num;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(getApplicationContext().getExternalFilesDir(null), "extra_" + String.valueOf(System.currentTimeMillis()) + ".png");
        imageCaptureUri = Uri.fromFile(file);

        try{
            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageCaptureUri);
            intent.putExtra("return data",true);
            startActivityForResult(intent,MainActivity.PICK_FROM_CAMERA);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void sendGalleryIntent(final RelativeLayout locusView,int x, int y, int num) {
        this.locusView = locusView;
        lastX = x;
        lastY = y;
        lastNum = num;

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,MainActivity.PICK_FROM_MEMORY);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!(resultCode == RESULT_OK)) {
            Toast.makeText(getApplicationContext(), "activity  not ok", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == MainActivity.PICK_FROM_CAMERA) {
            Toast.makeText(getApplicationContext(), "activity ok", Toast.LENGTH_SHORT).show();
            String imgPath = imageCaptureUri.getPath();
            Bitmap image = BitmapFactory.decodeFile(imgPath);
            image = NewLocusActivity.rotateImage(image, imgPath);
            image = NewLocusActivity.scaleBitmap(image, 500, 500);
            String stamp = String.valueOf(System.currentTimeMillis());
            File fullFile = new File(getExternalFilesDir(null), "extra_" + stamp + ".png");
            BitmapSaverTask saverTask =
                    new BitmapSaverTask(getApplicationContext(), image, fullFile.getAbsolutePath(), true);
            saverTask.execute();
            Extra newExtra = new Extra(routeID, lastNum, Extra.TYPE_IMG, fullFile.getAbsolutePath(), lastX, lastY, -1);
            newExtra.setID(MainActivity.dbHandler.addExtra(routeID,  newExtra));
            adapter.addHook(locusView,newExtra);
            adapter.notifyDataSetChanged();
        }

        if (requestCode == MainActivity.PICK_FROM_MEMORY) {

            imageCaptureUri = data.getData();
            InputStream inputStream;
            try{
                inputStream = getContentResolver().openInputStream(imageCaptureUri);
                Bitmap image = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                String imgPath = new File(getRealPathFromUri(imageCaptureUri)).getAbsolutePath();
                image = NewLocusActivity.rotateImage(image,imgPath);
                adapter.tempImage = NewLocusActivity.scaleBitmap(image, 50, 50);
                image = NewLocusActivity.scaleBitmap(image, 500, 500);


                String stamp = String.valueOf(System.currentTimeMillis());
                File fullFile = new File(getExternalFilesDir(null), "extra_" + stamp + ".png");
                BitmapSaverTask saverTask =
                        new BitmapSaverTask(getApplicationContext(), image, fullFile.getAbsolutePath(), false);
                saverTask.execute();

                Extra newExtra = new Extra(routeID, lastNum, Extra.TYPE_IMG, fullFile.getAbsolutePath(), lastX, lastY, -1);
                newExtra.setID(MainActivity.dbHandler.addExtra(routeID,  newExtra));
                adapter.addHook(locusView,newExtra);
                adapter.notifyDataSetChanged();


            }catch(Exception ex){
                ex.printStackTrace();
//                Toast.makeText(this,path,Toast.LENGTH_SHORT).show();
            }
        }

    }

    private String getRealPathFromUri(Uri imageUri) {
        String result;
        Cursor cursor = getContentResolver().query(imageUri, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = imageUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }



    class BitmapSaverTask extends AsyncTask<Void, Void, Void> {
        private Bitmap image;
        private String path;
        private int data = 0;
        private boolean addToGallery;
        private Context context;

        public BitmapSaverTask(Context context,Bitmap image, String path, boolean addToGallery){
            this.image = image;
            this.path = path;
            this.context = context;
            this.addToGallery = addToGallery;
        }


        @Override
        protected Void doInBackground(Void... params) {
            File file = new File(path);
            FileOutputStream out = null;
            try{
                out = new FileOutputStream(file);
                image.compress(Bitmap.CompressFormat.JPEG,100,out);
                if (addToGallery){
                    addImageToGallery(path,context);
                }
            }catch(Exception ex){
                ex.printStackTrace();
                Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT);
            }finally {
                if (out != null){
                    try{
                        out.close();
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }

    }

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

}

package eru.myapps.loboroutes;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class NewLocusActivity extends AppCompatActivity {

    EditText nameEdit;
    ImageView preview;
    String imgPath;
    Uri imageCaptureUri;
    AlertDialog dialog;
    Bitmap image;
    Bitmap thumbnail;
    boolean takenFromCamera = false;
    boolean imageSelected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_locus);
        nameEdit = (EditText) findViewById(R.id.locusNameEdit);
        preview = (ImageView) findViewById(R.id.locusPicPreview);
        image = BitmapFactory.decodeResource(getResources(),R.drawable.bg);


        nameEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (v != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }

                }
                return false;
            }
        });


        final String[] items = new String[]{"From Camera", "From File"};
        ArrayAdapter<String> itemAdapter = new ArrayAdapter<String>(this,android.R.layout.select_dialog_item,items);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image");


        builder.setAdapter(itemAdapter, new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                if (i == 0){ // "from camera" selected
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File file = new File(getExternalFilesDir(null), "locus_" + String.valueOf(System.currentTimeMillis()) + ".png");
                    imageCaptureUri = Uri.fromFile(file);

                    try{
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageCaptureUri);
                        intent.putExtra("return data",true);
                        startActivityForResult(intent,MainActivity.PICK_FROM_CAMERA);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    dialogInterface.cancel();
                } else{ // "from file" selected


                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent,MainActivity.PICK_FROM_MEMORY);
                    dialogInterface.cancel();
                }
            }
        });

        dialog = builder.create();

    }

    public void onChoosePressed(View view) {
        // test for permissions and ask for them if necessary
        askPermissions();
        dialog.show();
    }

    public void askPermissions() {
        if ( Build.VERSION.SDK_INT > 22 && ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    MainActivity.REQUEST_READ_EXTERNAL_STORAGE_CODE);
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onSave(View view) {
        Intent saveLocusIntent = new Intent();

        boolean success = false;
        String name = nameEdit.getText().toString();
        if (imageSelected) {
            String stamp = String.valueOf(System.currentTimeMillis());
            File thumbFile = new File(getExternalFilesDir(null), "locus_" + stamp + "_thumb.png");
            File fullFile = new File(getExternalFilesDir(null), "locus_" + stamp + ".png");

            BitmapSaverTask thumbTask = new BitmapSaverTask(getApplicationContext(), thumbnail, thumbFile.getPath(), false);
            thumbTask.execute();

            BitmapSaverTask fullTask = new BitmapSaverTask(getApplicationContext(), image, fullFile.getPath(), takenFromCamera);
            fullTask.execute();

            saveLocusIntent.putExtra("imgPath", fullFile.getAbsolutePath());
            saveLocusIntent.putExtra("thumbPath", thumbFile.getAbsolutePath());
        } else{
            saveLocusIntent.putExtra("imgPath", MainActivity.TEXT_DEFAULT);
            saveLocusIntent.putExtra("thumbPath", MainActivity.TEXT_DEFAULT);

        }
        saveLocusIntent.putExtra("name",name);
        setResult(RESULT_OK,saveLocusIntent);

        finish();

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            String mssg = "Result not ok: " + requestCode;
            //Toast.makeText(getApplicationContext(),mssg,Toast.LENGTH_LONG).show();
            return;
        }

        if (requestCode == MainActivity.PICK_FROM_MEMORY){
            imageCaptureUri = data.getData();
            InputStream inputStream;

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;


            try{
                inputStream = getContentResolver().openInputStream(imageCaptureUri);
                image = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                image = scaleBitmap(image,width,height);

                imgPath = new File(getRealPathFromUri(imageCaptureUri)).getAbsolutePath();
                image = rotateImage(image,imgPath);




                thumbnail = scaleBitmap(image,200,200);

                preview.setImageBitmap(thumbnail);
                takenFromCamera = false;




            }catch(Exception ex){
                ex.printStackTrace();
                Toast.makeText(this,"Image loading failed",Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == MainActivity.PICK_FROM_CAMERA){
            imgPath = imageCaptureUri.getPath();
            image = BitmapFactory.decodeFile(imgPath);
            image = rotateImage(image,imgPath);

            thumbnail = scaleBitmap(image, 200,200);


            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            image = scaleBitmap(image,width,height);

            preview.setImageBitmap(thumbnail);
            takenFromCamera = true;


        }
        imageSelected = true;


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

    public static Bitmap rotateImage(Bitmap bm, String path) {
        Matrix matrix = new Matrix();
        Bitmap rotated = null;
        try {
            File imageFile = new File(path);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
            //Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            rotated = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, false); // rotating bitmap
        }
        catch (Exception e) {

        }

        return rotated;
    }


    public static Bitmap scaleBitmap(Bitmap bm, int maxW, int maxH){
        int width, height;
        Bitmap scaled;
        float aspectRatio = bm.getWidth() / (float) bm.getHeight();
        if (aspectRatio > 1){
            width = maxW;
            height = Math.round(width/aspectRatio);
        } else {
            height = maxH;
            width = Math.round(height*aspectRatio);
        }

        scaled = Bitmap.createScaledBitmap(bm,width,height,false);
        return scaled;

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


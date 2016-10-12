package eru.myapps.loboroutes;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by elopezmo on 29.09.16.
 */
public class LocusPageAdapter extends PagerAdapter {


    private ArrayList<Locus> loci;
    ArrayList<Extra> extras;
    private Context context;
    private LayoutInflater inflater;
    Button lastHook = null;
    RelativeLayout hookGroup = null;
    private boolean addingExtra = false;
    private int routeID;
    private Locus locus;
    AlertDialog dialog;
    ViewGroup cont = null;
    private static final String LOG_TAG = "ExtraRecord";
    private ArrayList<Button> lastHookList;
    private boolean hooksVisible = false;
    private ArrayList<RelativeLayout> instantiatedViews = new ArrayList<>();

    float hookX = 0;
    float hookY = 0;
    public Bitmap tempImage;
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
        tempImage = BitmapFactory.decodeResource(container.getResources(), R.drawable.img_on);

        if (cont == null){
            cont = container;
        }
        locus = loci.get(position);
        extras = MainActivity.dbHandler.getExtras(routeID,locus.getNum());
        final ArrayList<Button> hookList = new ArrayList<Button>();
        lastHookList = hookList;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final RelativeLayout locusView = (RelativeLayout) inflater.inflate(R.layout.full_locus,container,false);

        locusView.setTag(R.string.TAG_EXTRAS,extras);
        locusView.setTag(R.string.TAG_HOOKS,hookList);

        ImageView imageView = (ImageView) locusView.findViewById(R.id.imageView);
        TextView nameView = (TextView) locusView.findViewById(R.id.locus_full_name);

        final LinearLayout addExtras = (LinearLayout) locusView.findViewById(R.id.locus_add_extras);
        final Button textButton = (Button) locusView.findViewById(R.id.locus_textButton);
        final Button microButton = (Button) locusView.findViewById(R.id.locus_microButton);
        final Button imgButton = (Button) locusView.findViewById(R.id.locus_imgButton);
        final Button cancelButton = (Button) locusView.findViewById(R.id.locus_extra_cancel);
        final Button hookShowButton = (Button) locusView.findViewById(R.id.hookShowButton);


        if(hooksVisible){
            hookShowButton.setBackgroundResource(R.drawable.hide_hook);
        }else{
            hookShowButton.setBackgroundResource(R.drawable.show_hook);
        }


        locusView.setTag(R.string.TAG_SHOWBUTTON,hookShowButton);


        nameView.setText(locus.getNum() + ". " + locus.getName());

        setupButtons(locusView, addExtras, locus, textButton,microButton,imgButton,cancelButton, hookShowButton, hookList);

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

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addingExtra){
                    addingExtra = false;
                    locusView.removeView(lastHook);
                    hookList.remove(lastHook);
                    addExtras.setVisibility(View.INVISIBLE);
                }
            }
        });

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (addingExtra){
                    hookGroup.removeView(lastHook);
                    hookList.remove(lastHook);
                }
                hooksVisible = true;
                setHookVisibility(true);
                addingExtra = true;
                int x = Math.round(hookX);
                int y = Math.round(hookY) ;
                hookGroup = (RelativeLayout) view.getParent();
                Toast.makeText(context,"pos:("+ x+","+y+")",Toast.LENGTH_SHORT).show();
                addExtras.setVisibility(View.VISIBLE);

                Button newHook = new Button(container.getContext());
                newHook.setBackgroundResource(R.drawable.new_hook);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50,50);

                params.setMargins(x -25 - 16 ,y + 25 + 16 ,0,0); // ?????



                newHook.setLayoutParams(params);
                locusView.addView(newHook,hookGroup.getChildCount());
                lastHook = newHook;
                return true;
            }
        });


        addHooks(locusView, extras, hookList);

        container.addView(locusView);
        instantiatedViews.add(locusView);
        return locusView;
    }




    private void setupButtons(final RelativeLayout locusView, final LinearLayout addExtras, final Locus l, Button textButton, Button microButton, Button imgButton, Button cancelButton, final Button hookShowButton, final ArrayList<Button> hookList) {

        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastHook.setBackgroundResource(R.drawable.abc_on);
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                View builderView = inflater.inflate(R.layout.dialog_extra_text,null);
                builder.setView(builderView);
                final EditText dialogEdit = (EditText) builderView.findViewById(R.id.extra_text);

                builder.setTitle(R.string.add_text);
                builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String text = dialogEdit.getText().toString();
                        Extra newExtra = new Extra(routeID,l.getNum(),Extra.TYPE_TEXT,text,(int)hookX,(int)hookY,-1);
                        newExtra.setID(MainActivity.dbHandler.addExtra(routeID,newExtra));
                        extras.add(newExtra);
                        dialog.dismiss();
                        locusView.removeView(lastHook);
                        hookList.remove(lastHook);
                        addHook(locusView, newExtra, hookList);
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        hookGroup.removeView(lastHook);
                        dialog.cancel();
                    }
                });
                dialog = builder.create();
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        locusView.removeView(lastHook);
                        hookList.remove(lastHook);
                    }
                });
                dialog.show();

                addExtras.setVisibility(View.INVISIBLE);
                addingExtra = false;
            }
        });

        microButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if ( Build.VERSION.SDK_INT > 22 && ContextCompat.checkSelfPermission(context,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ((Activity)context).requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MainActivity.REQUEST_RECORD_AUDIO);
                }

                lastHook.setBackgroundResource(R.drawable.microphone);
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                View builderView = inflater.inflate(R.layout.dialog_extra_audio,null);
                builder.setView(builderView);
                Button recordButton = (Button) builderView.findViewById(R.id.dialog_record_button);
                TextView recordText = (TextView) builderView.findViewById(R.id.dialog_record_text);
                final RecordListener recordListener = new RecordListener(recordButton,recordText);
                recordButton.setOnClickListener(recordListener);

                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.cancel();
                    }
                });
                builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        recordListener.stop();
                        String fileName = recordListener.getFileName();
                        if (fileName != null){
                            Extra newExtra = new Extra(routeID,l.getNum(),Extra.TYPE_AUDIO,fileName,(int)hookX,(int)hookY,-1);
                            newExtra.setID(MainActivity.dbHandler.addExtra(routeID,newExtra));
                            extras.add(newExtra);
                            locusView.removeView(lastHook);
                            hookList.remove(lastHook);
                            addHook(locusView,newExtra,hookList);
                            dialog.dismiss();
                        }else{
                            dialog.cancel();
                            Toast.makeText(context,"Empty recording",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog = builder.create();
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        recordListener.discard();
                        locusView.removeView(lastHook);
                        hookList.remove(lastHook);
                    }
                });
                dialog.show();

                addExtras.setVisibility(View.INVISIBLE);
                addingExtra = false;
            }
        });

        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastHook.setBackgroundResource(R.drawable.img_on);
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                View builderView = inflater.inflate(R.layout.dialog_extra_img,null);
                builder.setView(builderView);
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialog.cancel();
                    }
                });
                Button cameraButton = (Button) builderView.findViewById(R.id.extra_addCamera);
                Button galleryButton = (Button) builderView.findViewById(R.id.extra_addGallery);

                cameraButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lastHookList = hookList;
                        ((LocusActivity) context).sendCameraIntent(locusView,(int) hookX,(int) hookY,l.getNum());
                        dialog.dismiss();

                    }
                });

                galleryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lastHookList = hookList;
                        ((LocusActivity) context).sendGalleryIntent(locusView,(int) hookX,(int) hookY,l.getNum());
                        dialog.dismiss();

                    }
                });

                dialog = builder.create();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        locusView.removeView(lastHook);
                        hookList.remove(lastHook);
                    }
                });
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        locusView.removeView(lastHook);
                        hookList.remove(lastHook);
                    }
                });
                dialog.show();

                /**
                Extra newExtra = new Extra(routeID,locus.getNum(),Extra.TYPE_IMG,"",(int)hookX,(int)hookY,-1);
                newExtra.setID(MainActivity.dbHandler.addExtra(routeID,locus.getNum(),newExtra));
                extras.add(newExtra);**/
                addExtras.setVisibility(View.INVISIBLE);
                addingExtra = false;
            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addExtras.setVisibility(View.INVISIBLE);
                locusView.removeView(lastHook);
                hookList.remove(lastHook);
                addingExtra = false;
            }
        });

        hookShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hooksVisible = ! hooksVisible;
                setHookVisibility(hooksVisible);
            }
        });

    }


    public void addHook(final RelativeLayout locusView, final Extra e, final ArrayList<Button> hookList) {
        int x = e.getX();
        int y = e.getY();
        String type = e.getType();
        final String text = e.getSource();
        final Button newHook = new Button(locusView.getContext());
        switch (type) {
            case Extra.TYPE_TEXT:
                newHook.setBackgroundResource(R.drawable.abc_on);
                newHook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        View builderView = inflater.inflate(R.layout.dialog_show_text,null);
                        builder.setView(builderView);

                        TextView extraView = ( TextView) builderView.findViewById(R.id.extra_showTextView);
                        extraView.setText(text);
                        extraView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                ClipData clip = ClipData.newPlainText("Copied",text);
                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(context,"Text copied",Toast.LENGTH_SHORT).show();
                                return true;
                            }
                        });
                        dialog = builder.create();
                        dialog.show();
                    }
                });
                break;
            case Extra.TYPE_AUDIO:
                newHook.setBackgroundResource(R.drawable.microphone);
                newHook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        View builderView = inflater.inflate(R.layout.dialog_show_audio,null);
                        builder.setView(builderView);

                        Button playButton = (Button) builderView.findViewById(R.id.dialog_play_button);
                        TextView playText = (TextView) builderView.findViewById(R.id.dialog_play_text);
                        PlayListener listener = new PlayListener(playButton,playText,e.getSource());
                        playButton.setOnClickListener(listener);


                        dialog = builder.create();
                        dialog.show();
                    }
                });
                break;
            default:  // type img
                Bitmap bitmap = BitmapFactory.decodeFile(e.getSource());
                if (bitmap == null){ // not finished saving yet
                    bitmap = tempImage;
                }else {
                    int h = bitmap.getHeight();
                    int w = bitmap.getWidth();
                    int dim = Math.min(h, w);
                    bitmap = ThumbnailUtils.extractThumbnail(bitmap, dim, dim);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                }
                bitmap = addDoubleBorder(bitmap,2);
                if (android.os.Build.VERSION.SDK_INT < 16) {
                    newHook.setBackgroundDrawable(new BitmapDrawable(context.getResources(), bitmap));
                } else {
                    newHook.setBackground(new BitmapDrawable(context.getResources(), bitmap));
                }

                newHook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        View builderView = inflater.inflate(R.layout.dialog_show_img,null);
                        builder.setView(builderView);

                        ImageView extraView = (ImageView) builderView.findViewById(R.id.extra_showImgView);
                        extraView.setImageBitmap(BitmapFactory.decodeFile(e.getSource()));
                        dialog = builder.create();
                        dialog.show();
                    }
                });
                break;
        }


        newHook.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete selected content?");
                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((RelativeLayout)view.getParent()).removeView(newHook);
                        if (hookList != null) {
                            hookList.remove(lastHook);
                        }else{
                            lastHookList.remove(lastHook);
                        }
                        MainActivity.dbHandler.deleteExtra(e);
                        notifyDataSetChanged();

                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });

                dialog = builder.create();
                dialog.show();


                return true;
            }
        });



        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                50,50);

        params.setMargins(x -25 - 16 ,y + 25 + 16 ,0,0); // ?????

        newHook.setLayoutParams(params);
        locusView.addView(newHook,locusView.getChildCount());
        if (!hooksVisible){
            newHook.setVisibility(View.INVISIBLE);
        }
        if (hookList != null) {
            hookList.add(newHook);
        }else{
            lastHookList.add(newHook);
        }
    }

    private void setHookVisibility(boolean visible ){

        for(int i = 0; i < instantiatedViews.size();i++){
            View view;
            try{
                view = instantiatedViews.get(i);
                Button showHookButton = (Button) view.getTag(R.string.TAG_SHOWBUTTON);
                if(visible){
                    showHookButton.setBackgroundResource(R.drawable.hide_hook);
                }else{
                    showHookButton.setBackgroundResource(R.drawable.show_hook);
                }
                ArrayList<Button> hooks = (ArrayList<Button>) view.getTag(R.string.TAG_HOOKS);
                for (Button hook:hooks){
                    if (visible) {
                        hook.setVisibility(View.VISIBLE);
                    }else{
                        hook.setVisibility(View.INVISIBLE);
                    }
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void addHooks(final RelativeLayout locusView, ArrayList<Extra> extras, final ArrayList<Button> hookList) {
        for (final Extra e :extras){
            addHook(locusView,e,hookList);
        }
    }

    private Bitmap addDoubleBorder(Bitmap bmp, int bordersize){
        Bitmap singleborder = addBorder(bmp,bordersize,Color.WHITE);
        return addBorder(singleborder,bordersize,Color.BLACK);
    }


    private Bitmap addBorder(Bitmap bmp, int borderSize, int color) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(color);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }




    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
        instantiatedViews.remove(object);
    }


    private class RecordListener implements View.OnClickListener{

        private Button host;
        private TextView mssg;
        private boolean recording = false;
        private boolean recorded = false;
        private boolean playing = false;
        private String mFileName = null;
        private MediaRecorder recorder;
        private MediaPlayer player;

        public RecordListener(Button host, TextView mssg){
            super();
            this.host = host;
            this.mssg = mssg;
        }

        public String getFileName() {
            return mFileName;
        }

        @Override
        public void onClick(View view) {
            if(!recorded) {
                if (!recording) {
                    String stamp = String.valueOf(System.currentTimeMillis());
                    mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
                    mFileName += "/"+MainActivity.folder_extra + "/extra_" + stamp + ".3gp";

                    recorder = new MediaRecorder();
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorder.setOutputFile(mFileName);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    try {
                        recorder.prepare();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "prepare() failed");
                    }
                    host.setBackgroundResource(R.drawable.micro_recording);
                    mssg.setText("Recording...");
                    recorder.start();
                } else {
                    recorder.stop();
                    recorder.release();
                    recorder = null;
                    recorded = true;
                    host.setBackgroundResource(android.R.drawable.ic_media_play);
                    mssg.setText("Tap to play");
                }
                recording = !recording;
            }else{
                if (!playing) {
                    player = new MediaPlayer();
                    try {
                        player.setDataSource(mFileName);
                        player.prepare();
                        player.start();
                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                player.stop();
                                player.release();
                                player = null;
                                host.setBackgroundResource(android.R.drawable.ic_media_play);
                                mssg.setText("Tap to play");
                                playing = false;
                            }
                        });
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "prepare() failed");
                    }
                    host.setBackgroundResource(android.R.drawable.ic_media_pause);
                    mssg.setText("Playing ...");
                }else{
                    player.stop();
                    player.release();
                    player = null;
                    host.setBackgroundResource(android.R.drawable.ic_media_play);
                    mssg.setText("Tap to play");
                }
                playing = !playing;
            }

        }

        public void stop() {
            if(recording){
                recorder.stop();
                recorder.release();
                recorder = null;
                recorded = true;
            }
        }

        public void discard(){
            stop();
            if (recorded){
                File file = new File(mFileName);
                file.delete();
            }
        }
    }


    private class PlayListener implements View.OnClickListener{

        private Button host;
        private TextView mssg;
        private boolean playing = false;
        private String mFileName = null;
        private MediaPlayer player;

        public PlayListener(Button host, TextView mssg, String fileName){
            super();
            this.host = host;
            this.mssg = mssg;
            mFileName = fileName;
        }


        @Override
        public void onClick(View view) {

                if (!playing) {
                    player = new MediaPlayer();
                    try {
                        player.setDataSource(mFileName);
                        player.prepare();
                        player.start();
                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                player.stop();
                                player.release();
                                player = null;
                                host.setBackgroundResource(android.R.drawable.ic_media_play);
                                mssg.setText("Tap to play");
                                playing = false;
                            }
                        });
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "prepare() failed");
                    }
                    host.setBackgroundResource(android.R.drawable.ic_media_pause);
                    mssg.setText("Playing ...");
                }else{
                    player.stop();
                    player.release();
                    player = null;
                    host.setBackgroundResource(android.R.drawable.ic_media_play);
                    mssg.setText("Tap to play");
                }
                playing = !playing;


        }
    }


}

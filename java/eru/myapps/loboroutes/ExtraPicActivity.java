package eru.myapps.loboroutes;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class ExtraPicActivity extends Activity {

    private ImageView imageView;
    private Button goBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_pic);

        String path = getIntent().getExtras().getString("path");
        imageView = (ImageView) findViewById(R.id.extraImgFullView);
        imageView.setImageBitmap(BitmapFactory.decodeFile(path));
        goBack = (Button) findViewById(R.id.extraPick_back);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}

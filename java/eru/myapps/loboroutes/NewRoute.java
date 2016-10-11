package eru.myapps.loboroutes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toast.*;


public class NewRoute extends AppCompatActivity {


    EditText title;
    EditText descript;
    EditText countfrom;
    DBHandler dbh = new DBHandler(this, null, null, 0);
    String oldTitle;
    int index;
    boolean edit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_route);

        title = (EditText) findViewById(R.id.newtitle);
        descript = (EditText) findViewById(R.id.newdescr);
        countfrom = (EditText) findViewById(R.id.newStartCount);
        Bundle extras = getIntent().getExtras();
        edit = extras.getBoolean("edit");

        if (edit){
            oldTitle = extras.getString("title");
            title.setText(oldTitle);
            descript.setText(extras.getString("description"));
            index = extras.getInt("index");
            int oldCount = extras.getInt("countFrom");
            countfrom.setText(String.valueOf(oldCount));
        }

        title.setOnEditorActionListener(new hideKeyboardListener());
        descript.setOnEditorActionListener(new hideKeyboardListener());
        countfrom.setOnEditorActionListener(new hideKeyboardListener());


    }

    public void onCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onSave(View view) {

        String titleText = title.getText().toString();
        int countStart = Integer.parseInt(countfrom.getText().toString());
        if (titleText.length() == 0) {
            Toast.makeText(NewRoute.this, "Enter title", Toast.LENGTH_SHORT).show();
        } else if (dbh.exists(titleText) && (!edit || !titleText.equals(oldTitle))) {
            Toast.makeText(NewRoute.this, "Route \"" + titleText + "\" already exists", Toast.LENGTH_SHORT).show();
        } else {
            Intent saveIntent = new Intent();

            Bundle letter = new Bundle();
            letter.putString("title", title.getText().toString());
            letter.putString("description", descript.getText().toString());
            letter.putInt("count",countStart);

            if (edit){
                letter.putInt("index",index);
            }

            saveIntent.putExtras(letter);
            setResult(RESULT_OK, saveIntent);
            finish();
        }

    }


    private class hideKeyboardListener implements TextView.OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (textView != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                }

            }
            return true;

        }
    }



}
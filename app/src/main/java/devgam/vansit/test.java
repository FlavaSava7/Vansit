package devgam.vansit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class test extends AppCompatActivity {

    TextView textView, textView2;
    Button button1;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        final recommendedSharedPref r = new recommendedSharedPref(this);

        textView = (TextView) findViewById(R.id.textView10);
        textView2 = (TextView) findViewById(R.id.textView11);
        button1 = (Button) findViewById(R.id.button2);
        editText = (EditText) findViewById(R.id.editText2);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView2.setText(r.adddToFavType(editText.getText().toString()) + "");
                textView.setText(r.getFavType());
            }
        });
    }
}

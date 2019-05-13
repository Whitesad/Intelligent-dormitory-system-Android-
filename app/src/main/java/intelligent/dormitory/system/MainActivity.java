package intelligent.dormitory.system;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private EditText LoginUserName;
    private EditText LoginPassWord;
    private Button LoginButton;
    protected Button RegisterButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SetLoginLogo();
        GetEditText();
        SetButtonListener();
    }

    private void GetEditText(){
        this.LoginUserName=findViewById(R.id.LoginUserName);
        this.LoginPassWord=findViewById(R.id.LoginPassWord);
    }

    private void SetButtonListener(){
        OnClick onClick=new OnClick();
        this.LoginButton=findViewById(R.id.LoginButton);
        this.RegisterButton=findViewById(R.id.RegisterButton);
        LoginButton.setOnClickListener(onClick);
        RegisterButton.setOnClickListener(onClick);
    }
    class OnClick implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            Intent intent=null;
            switch (v.getId())
            {
                case R.id.LoginButton:
                    intent=new Intent(MainActivity.this,LoginActivity.class);
                    break;
                case R.id.RegisterButton:
                    intent=new Intent(MainActivity.this,RegisterActivity.class);
                    break;
            }
            startActivity(intent);
        }
    }

    private void SetLoginLogo(){
        TextView LoginLogo=findViewById(R.id.LoginLogo);
        AssetManager font=getAssets();
        Typeface tf=Typeface.DEFAULT.createFromAsset(font,"font/segmdl2.ttf");
        LoginLogo.setTypeface(tf);
    }
}

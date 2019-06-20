package intelligent.dormitory.system;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Sock.Sock;

public class RegisterActivity extends AppCompatActivity {

    private Button RegisterSubmitButton;
    private EditText UserName;
    private EditText PassWord01;
    private EditText PassWord02;

    private Sock sock=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        SetLoginLogo();
        InitialViews();
        SetOnClickListener();

        this.sock=ConstantVariable.sock;
//        this.sock=(Sock)this.getIntent().getSerializableExtra("Sock");

    }
    private void SetOnClickListener(){
        RegisterSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName=UserName.getText().toString();
                String passWord01=PassWord01.getText().toString();
                String passWord02=PassWord02.getText().toString();

                String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&（）——+|{}【】‘；：”“’。，、？]";
                Pattern p=Pattern.compile(regEx);
                Boolean isUserNameIllegal = p.matcher(userName).find();
                Boolean isPassWordIllegal = p.matcher(passWord01).find();

                if(!passWord01.equals(passWord02)){
                    Toast.makeText(getApplicationContext(),"PassWord is not exactly the same!",
                            Toast.LENGTH_SHORT).show();
                }else if(passWord01.length()>=32||passWord01.length()<=4){
                    Toast.makeText(getApplicationContext(),"PassWord must be longer than 4 letters and less than 32 letters!",
                            Toast.LENGTH_SHORT).show();
                }else if(userName.contains(" ")||isUserNameIllegal||userName.equals("")||userName==null){
                    Toast.makeText(getApplicationContext(),"UserName must be without Illegal Character and contains no space!",
                            Toast.LENGTH_SHORT).show();
                }else if(passWord01==null||passWord01.equals("")){
                    Toast.makeText(getApplicationContext(),"PassWord can't contains the illegal character!",
                            Toast.LENGTH_SHORT).show();
                }else if(isPassWordIllegal){
                    Toast.makeText(getApplicationContext(),"PassWord can't contains the illegal character!",
                            Toast.LENGTH_SHORT).show();
                }else{
                    sock.SetUserName(userName);
                    sock.SetPassWord(passWord01);
                    Sock.Status status;
                    try {
                        status=sock.Register();
                        switch (status){
                            case NONE:
                                Toast.makeText(getApplicationContext(),"Some Unexpected Errors Happend!",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case SAME_NAME:
                                Toast.makeText(getApplicationContext(),"Register Failure!The username is existed!",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case REGISTER_ERROR:
                                Toast.makeText(getApplicationContext(),"REGISTER ERROR!",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case REGISTER_AC:
                                Toast.makeText(getApplicationContext(),"Register Accepted!",
                                        Toast.LENGTH_SHORT).show();
                                break;
                                default:
                                    break;
                        }
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(),"Server Connect Error!Register Failure!",
                                Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(),"Receive Error!",
                                Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void InitialViews(){
        RegisterSubmitButton=findViewById(R.id.RegisterSubmitButton);
        UserName=findViewById(R.id.RegisterUserName);
        PassWord01=findViewById(R.id.RegisterPassWord01);
        PassWord02=findViewById(R.id.RegisterPassWord02);

    }
    private void SetLoginLogo(){
        TextView LoginLogo=findViewById(R.id.RegisterLogo);
        AssetManager font=getAssets();
        Typeface tf=Typeface.DEFAULT.createFromAsset(font,"font/segmdl2.ttf");
        LoginLogo.setTypeface(tf);
    }
}

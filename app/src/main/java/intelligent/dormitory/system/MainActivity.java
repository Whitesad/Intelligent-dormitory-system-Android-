package intelligent.dormitory.system;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import Sock.Sock;

import static java.net.InetAddress.getLocalHost;

public class MainActivity extends AppCompatActivity {
    private String  testHost="10.1.139.101";
//    private String  testHost="10.195.127.64";
    private int port=50000;

    private EditText LoginUserName;
    private EditText LoginPassWord;
    private Button LoginButton;
    protected Button RegisterButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        SetLoginLogo();
        GetEditText();
        SetButtonListener();
//        try {
//            InitialHost();
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
    }

    private void InitialHost() throws UnknownHostException, SocketException {
        try {
            String ipv4;
            ArrayList<NetworkInterface> nilist = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni: nilist)
            {
                ArrayList<InetAddress>  ialist = Collections.list(ni.getInetAddresses());
                for (InetAddress address: ialist){
                    if (!address.isLoopbackAddress() && !address.isLinkLocalAddress())
                    {
                        ipv4=address.getHostAddress();
                        this.testHost=ipv4;
                        return;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("localip", ex.toString());
        }
        return;
    }
    private void Login()  {

        String userName=LoginUserName.getText().toString();
        String passWord=LoginPassWord.getText().toString();

        Sock sock=null;
        try {
            sock = new Sock(userName,passWord,testHost,port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Sock.Status status;
        try {
            status=sock.Login();
            if(status==Sock.Status.LOGIN_AC){
                Intent intent=new Intent(MainActivity.this,CommunicateActivity.class);
                startActivity(intent);
            }else if(status== Sock.Status.WRONG_PASSWORD){

            }else if(status== Sock.Status.NO_MEMSHIP){

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                    Login();
                    break;
                case R.id.RegisterButton:
                    intent=new Intent(MainActivity.this,RegisterActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    }

    private void SetLoginLogo(){
        TextView LoginLogo=findViewById(R.id.LoginLogo);
        AssetManager font=getAssets();
        Typeface tf=Typeface.DEFAULT.createFromAsset(font,"font/segmdl2.ttf");
        LoginLogo.setTypeface(tf);
    }
}

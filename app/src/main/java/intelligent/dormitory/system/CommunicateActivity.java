package intelligent.dormitory.system;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Sock.Sock;

public class CommunicateActivity extends AppCompatActivity {

    private Button Send_Button;
    private EditText EditText_UserInput;
    private ChatAdapter chatAdapter;
    private ListView Chat_List_View;
    private List<PersonChat> personChatList=new ArrayList();
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch(what) {
                case 1:
                    CommunicateActivity.this.Chat_List_View.setSelection(CommunicateActivity.this.personChatList.size());
                default:
            }
        }
    };

    private void InitialViews(){
        this.Chat_List_View=findViewById(R.id.lv_chat_dialog);
        this.Send_Button=findViewById(R.id.btn_chat_message_send);
        this.EditText_UserInput=findViewById(R.id.et_chat_message);
        this.chatAdapter=new ChatAdapter(this,this.personChatList);
        this.Chat_List_View.setAdapter(this.chatAdapter);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(1);
        setContentView(R.layout.activity_communicate);
        Toast.makeText(getApplicationContext(),"Login AC!",
                Toast.LENGTH_SHORT).show();
        InitialViews();
        final Sock sock=(Sock)this.getIntent().getSerializableExtra("Sock");
        sock.SetOutput(handler);
        sock.Start();

        Send_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput=EditText_UserInput.getText().toString();
                sock.send(userInput);
                PersonChat personChat=new PersonChat();
                personChat.setMeSend(true);
                personChat.setChatMessage(userInput);
                CommunicateActivity.this.personChatList.add(personChat);
                EditText_UserInput.setText("");
                chatAdapter.notifyDataSetChanged();
                //handler.sendEmptyMessage(1);
            }
        });

    }
}

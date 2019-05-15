package intelligent.dormitory.system;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(1);
        setContentView(R.layout.activity_communicate);

        this.Chat_List_View=findViewById(R.id.lv_chat_dialog);
        this.Send_Button=findViewById(R.id.btn_chat_message_send);
        this.EditText_UserInput=findViewById(R.id.et_chat_message);
        this.chatAdapter=new ChatAdapter(this,this.personChatList);
        this.Chat_List_View.setAdapter(this.chatAdapter);

        Send_Button.setOnClickListener(new View.OnClickListener() {
            int i=0;
            @Override
            public void onClick(View v) {
                String userInput=EditText_UserInput.getText().toString();
                PersonChat personChat=new PersonChat();
                if(i%2==0){
                    personChat.setMeSend(true);
                    i++;
                }else{
                    personChat.setMeSend(false);
                    i++;
                }
                personChat.setChatMessage(userInput);
                CommunicateActivity.this.personChatList.add(personChat);
                EditText_UserInput.setText("");
                chatAdapter.notifyDataSetChanged();
                //handler.sendEmptyMessage(1);
            }
        });

    }
}

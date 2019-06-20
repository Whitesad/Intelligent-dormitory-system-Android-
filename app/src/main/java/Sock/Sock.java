package Sock;

//import android.app.Person;
import android.bluetooth.BluetoothAdapter;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.os.Handler;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import RSAUnit.RSAUtils;
import intelligent.dormitory.system.ChatAdapter;
import intelligent.dormitory.system.CommunicateActivity;
import intelligent.dormitory.system.PersonChat;

import static java.net.InetAddress.getLocalHost;

public class Sock implements Serializable {

    public enum Status
    {
        NONE,
        LOGIN_AC,
        NO_MEMSHIP,
        QUERY_ERROR,
        WRONG_PASSWORD,
        CONNECT_ERROR,
        REGISTER_AC,
        SAME_NAME,
        REGISTER_ERROR
    }

    //基本信息区
    private static String passWord;
    public static String userName;
    private String FTPUsername;
    private String FTPPassword;
    private String UserInput;
    private String UserInputText;


    //socket区
    private static Socket socketServer;
    private static String localIp;
    private static String localName;
    private static String  hostIp;
    private static int port;
    private static Socket socket;
    private static InputStream socket_in;
    private static OutputStream socket_out;

    //工具区
    private static DictMaker dictMaker=new DictMaker();
    private RSAUtils rsaUtils=new RSAUtils();

    //键值区
    private Boolean isSending=false;

    private void InitialIpHost() throws UnknownHostException {
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
                        Sock.localIp=ipv4;
                        Sock.localName= BluetoothAdapter.getDefaultAdapter().getName();
                        return;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("localip", ex.toString());
        }
        return;
    }
    //构造函数
    public Sock() throws UnknownHostException {
        try {
            rsaUtils.genKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
        InitialIpHost();
    }
    public Sock(String userName,String passWord) throws UnknownHostException {
        try {
            rsaUtils.genKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
        InitialIpHost();
        Sock.userName=userName;
        Sock.passWord=passWord;
    }
    public Sock(String userName,String passWord,String hostIp,int port) throws UnknownHostException {
        try {
            rsaUtils.genKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
        InitialIpHost();
        Sock.hostIp=hostIp;
        Sock.port=port;
        Sock.userName=userName;
        Sock.passWord=passWord;
    }

    //成员修改
    public void SetServer(String hostIp,int port){
        Sock.hostIp=hostIp;
        Sock.port=port;
    }
    public void SetUserName(String userName){
        Sock.userName=userName;
    }
    public void SetPassWord(String passWord){
        Sock.passWord=passWord;
    }
    //成员获得
    public boolean IsConnected()
    {
        return Sock.socketServer.isConnected();
    }
    public void Close() throws IOException {
        Sock.socketServer.shutdownOutput();
    }


    //连接方面

    //尝试一次连接
    private static void StartConnect() throws IOException {
        Sock.socketServer=new Socket();
        Sock.socketServer.connect(new InetSocketAddress(Sock.hostIp,Sock.port),100);
//        Sock.socketServer=new Socket(Sock.hostIp,Sock.port);
//        Sock.socketServer.setSoTimeout(100);
        Sock.socket_in=Sock.socketServer.getInputStream();
        Sock.socket_out=Sock.socketServer.getOutputStream();
    }
    private     void     StartConnect(String hostIp,int port) throws IOException {
        //set the hostip and port
        Sock.hostIp=hostIp;
        Sock.port=port;
        Sock.socketServer = new Socket(Sock.hostIp, Sock.port);
        Sock.socket_in=Sock.socketServer.getInputStream();
        Sock.socket_out=Sock.socketServer.getOutputStream();
    }
    //Login
    public Status Login(String userName,String passWord) throws Exception {
        Sock.userName=userName;
        Sock.passWord=passWord;
        return this.Login();
    }
    public Status Login() throws Exception {
        if(userName==null||passWord==null)
            return Status.NONE;
        StartConnect();

        Map<String,String > dict_loginRequest=Sock.dictMaker.MakeLoginRequestDict(this.rsaUtils.getPublicKey());

        Send(dict_loginRequest);
        Map<String ,String > dict_receive_mes=Receive();

        if(dict_receive_mes.containsKey("publickey")){
            dictMaker.setServerKey(dict_receive_mes.get("publickey"));
        }

        Map<String,String > dict_login=dictMaker.MakeLoginDict(Sock.userName,Sock.passWord,Sock.localIp,Sock.localName);
        Send(dict_login);
        dict_receive_mes=Receive();

        if(dict_receive_mes.containsKey("type") && (dict_receive_mes.get("type").equals("LOGIN_MES")) ){
            if(dict_receive_mes.get("status").equals("AC")){
                this.FTPUsername=dict_receive_mes.get("ftpusername");
                this.FTPPassword=dict_receive_mes.get("ftppassword");
                return Status.LOGIN_AC;
            }
            else if(dict_receive_mes.get("status").equals("NO_MEMSHIP")){
                return Status.NO_MEMSHIP;
            }else if(dict_receive_mes.get("status").equals("WRONG_PASSWORD")){
                return Status.WRONG_PASSWORD;
            }
        }
        return Status.CONNECT_ERROR;
    }
    //Register
    public Status Register() throws IOException, JSONException {
        if(userName==null||passWord==null)
            return Status.NONE;
        StartConnect();
        Map<String,String> dict_register=new HashMap<String, String>();
        dict_register=this.dictMaker.MakeRegisterDict(Sock.userName,Sock.passWord,Sock.localIp,Sock.localName);
        Send(dict_register);
        Map<String,String> dict_receive=Receive();
        if(dict_receive.containsKey("type")&&dict_receive.get("type").equals("REGISTER_MES"))
        {
            if(dict_receive.containsKey("status"))
            {
                if(dict_receive.get("status").equals("AC")){
                    return Status.REGISTER_AC;
                }else if(dict_receive.get("status").equals("SAME_NAME")){
                    return Status.SAME_NAME;
                }else if(dict_receive.get("status").equals("REGISTER_ERROR")){
                    return Status.REGISTER_ERROR;
                }else {
                    return Status.REGISTER_ERROR;
                }
            }
        }
        return Status.NONE;
    }
    public void Start(){
        ThreadOut threadOut = new ThreadOut("threadOut");
        threadOut.start();

        ThreadListen threadListen=new ThreadListen("threadListen");
        threadListen.start();
    }

    //线程,输出方面
    private Handler handler;
    private List<PersonChat> personChatList=new ArrayList();
    public void SetOutput(Handler handler,List<PersonChat> listView){
        this.handler=handler;
        this.personChatList=listView;
    }
    public void send(String userInputText){
        this.UserInputText=userInputText;
        this.isSending=true;
    }
    class ThreadListen extends Thread{
        private String name;
        private Thread thread;

        ThreadListen(){}
        ThreadListen(String name){
            this.name=name;
        }

        public void run(){
            Map<String ,String > dict_receive;
            while (true){
                try {
                    dict_receive=Receive();
                    Print(dict_receive);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        public void start(){
            if(thread==null){
                thread=new Thread(this,name);
                thread.start();
            }
        }
    }
    class ThreadOut    extends Thread{
        String name;
        Thread thread;

        ThreadOut(){}
        ThreadOut(String name){
            this.name=name;
        }

        public void run()
        {
            Scanner input= new Scanner(System.in);
            while (true){
                try {
                    if(isSending){
                        Map<String ,String> dict_send=dictMaker.MakeTextDict(userName,UserInputText,localIp,localName);
                        Send(dict_send);
                        isSending=false;
                    }
//                    UserInputText=input.nextLine();
//                    Send(dictMaker.MakeTextDict(userName,UserInputText,localIp,localName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        public void start(){
            if(thread==null){
                thread=new Thread(this,name);
                thread.start();
            }
        }
    }

    //接收、发送功能
    private static void Send(byte[] bytes_send_mes) throws IOException {
        Sock.socket_out.write(bytes_send_mes);
    }

    private static void Send(Map<String ,String > dict_mes) throws IOException {
        byte[] bytes_send_mes = Sock.dictMaker.MakeBytesDict(dict_mes);
        Sock.Send(bytes_send_mes);
    }
    private void Decrypt(Map<String ,String> dict) throws Exception {
        if(dict.containsKey("content")){
            dict.put("content",this.rsaUtils.decryptByPrivateKey(dict.get("content")));
        }
        if(dict.containsKey("ftpusername")){
            dict.put("ftpusername",this.rsaUtils.decryptByPrivateKey(dict.get("ftpusername")));
        }
        if(dict.containsKey("ftppassword")){
            dict.put("ftppassword",this.rsaUtils.decryptByPrivateKey(dict.get("ftppassword")));
        }
    }
    private Map<String ,String > Receive() throws IOException, JSONException {
        byte[] bytes_dict = new byte[2048];
        Sock.socket_in.read(bytes_dict);
        Map<String ,String > dict_receive = Sock.dictMaker.MakeDict(bytes_dict);
        try {
            Decrypt(dict_receive);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  dict_receive;
    }

    private void Print(Map<String ,String > dict_output){
        if (dict_output.containsKey("content")&&dict_output.get("content")!=null) {
//              System.out.println(dict_output.get("content"));
                PersonChat personChat=new PersonChat();
                personChat.setMeSend(false);
                personChat.setChatMessage(dict_output.get("content"));
                this.personChatList.add(personChat);

                Message message=Message.obtain();
                message.what=0;
                handler.sendMessage(message);
        }
    }


    public static void main(String[] args) throws UnsupportedEncodingException, JSONException, UnknownHostException {
        Sock sock=new Sock();
        try {
            sock=new Sock("whitesad","yang");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        sock.SetServer(Sock.localIp,50000);
        try {
            Status status = sock.Login();
            if(status== Status.LOGIN_AC){
                sock.Start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

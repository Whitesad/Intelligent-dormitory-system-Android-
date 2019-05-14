package Sock;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Scanner;

import static java.net.InetAddress.getLocalHost;

public class Sock {

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

    //建值区
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
        InitialIpHost();
        Sock.userName=userName;
        Sock.passWord=passWord;
    }
    public Sock(String userName,String passWord) throws UnknownHostException {
        InitialIpHost();
        Sock.userName=userName;
        Sock.passWord=passWord;
    }
    public Sock(String userName,String passWord,String hostIp,int port) throws UnknownHostException {
        InitialIpHost();
        Sock.hostIp=hostIp;
        Sock.port=port;
        Sock.userName=userName;
        Sock.passWord=passWord;
    }

    //成员修改
    public void SetServer(String hostIp,int port)
    {
        Sock.hostIp=hostIp;
        Sock.port=port;
    }
    //成员获得
    public boolean IsConnected()
    {
        return Sock.socketServer.isConnected();
    }
    public void Close() throws IOException {
        Sock.socketServer.close();
    }


    //连接方面

    //尝试一次连接
    public static void StartConnect() throws IOException {
        Sock.socketServer=new Socket(Sock.hostIp,Sock.port);
        Sock.socket_in=Sock.socketServer.getInputStream();
        Sock.socket_out=Sock.socketServer.getOutputStream();
    }
    public void StartConnect(String hostIp,int port) throws IOException {
        //set the hostip and port
        Sock.hostIp=hostIp;
        Sock.port=port;
        Sock.socketServer = new Socket(Sock.hostIp, Sock.port);
        Sock.socket_in=Sock.socketServer.getInputStream();
        Sock.socket_out=Sock.socketServer.getOutputStream();
    }

    public Status Login(String userName,String passWord) throws IOException, JSONException {
        Sock.userName=userName;
        Sock.passWord=passWord;
        return Login();
    }
    public static Status Login() throws IOException, JSONException {
        StartConnect();
        Map<String,String > dict_login=Sock.dictMaker.MakeLoginDict(Sock.userName,Sock.passWord,Sock.localIp,Sock.localName);

        Send(dict_login);
        Map<String ,String > dict_receive_mes=Receive();

        if(dict_receive_mes.containsKey("type") && (dict_receive_mes.get("type").equals("LOGIN_MES")) ){
            if(dict_receive_mes.get("status").equals("AC")){
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
    public void Start(){
        ThreadOut threadOut = new ThreadOut("threadOut");
        threadOut.start();

        ThreadListen threadListen=new ThreadListen("threadListen");
        threadListen.start();
    }

    //线程
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
//                    if(isSending){
//                        Send(dictMaker.MakeTextDict(userName,UserInputText,localIp,localName));
//                        isSending=false;
//                    }
                    UserInputText=input.nextLine();
                    Send(dictMaker.MakeTextDict(userName,UserInputText,localIp,localName));
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

    private static Map<String ,String > Receive() throws IOException, JSONException {
        byte[] bytes_dict = new byte[2048];
        Sock.socket_in.read(bytes_dict);
        return Sock.dictMaker.MakeDict(bytes_dict);
    }

    private void Print(Map<String ,String > dict_output){
        if (dict_output.containsKey("content")&&dict_output.get("content")!=null) {
            System.out.println(dict_output.get("content"));
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
        }
    }
}

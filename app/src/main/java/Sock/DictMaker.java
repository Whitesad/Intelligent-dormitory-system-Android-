package Sock;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import RSAUnit.RSAUtils;

public class DictMaker {
    private RSAUtils rsaUtils= new RSAUtils();

    private String Encrypt(String content) {
        try {
            String encryptContent=this.rsaUtils.encryptByPublicKey(content);
            return encryptContent;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return content;
    }

    public void setServerKey(String publickey){
        try {
            this.rsaUtils.setServerPubKey(publickey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    DictMaker(){}

    public Map<String, String> MakeDict(byte[] bytes_mes) throws JSONException {
        String str_mes = new String(bytes_mes);
        JSONObject jsonObject = new JSONObject(str_mes);
        Map<String, String> map = new HashMap<String, String>();
        for (Iterator<?> iter = jsonObject.keys(); iter.hasNext(); ) {
            String key = (String) iter.next();
            String value = jsonObject.get(key).toString();
            map.put(key, value);
        }
        return map;
    }
    public Map<String, String> MakeDict(String str_mes) throws JSONException {
        JSONObject jsonObject = new JSONObject(str_mes);
        Map<String, String> map = new HashMap<String, String>();
        for (Iterator<?> iter = jsonObject.keys(); iter.hasNext(); ) {
            String key = (String) iter.next();
            String value = jsonObject.get(key).toString();
            map.put(key, value);
        }
        return map;
    }

    public Map<String, String> MakeLoginRequestDict(String publickey){
        Map<String, String> map = new HashMap<String, String>();
        publickey=publickey.replace("\n","*");
        map.put("type","LOGIN_REQUEST");
        map.put("publickey",publickey.substring(0,publickey.length()-1));
        return map;
    }

    public Map<String, String> MakeLoginDict(String userName,String passWord,String localIp,String localName) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("username",Encrypt(userName));
        map.put("password",Encrypt(passWord));
        map.put("type","LOGIN_MES");
        map.put("status","login");
        map.put("ip",localIp);
        map.put("localname",localName);

        return map;
    }

    public byte[] MakeBytesDict(Map<String ,String > dict_mes) throws UnsupportedEncodingException {
        String str_mes="{";
        int Count=1;
        for (Map.Entry<String,String > entry : dict_mes.entrySet()) {
            str_mes+=("\""+entry.getKey()+"\": ");
            str_mes+=(" \""+entry.getValue());
            if(Count!=dict_mes.size()){
                str_mes+=("\" ,");
            }else {
                str_mes+="\" }";
            }
            Count++;
        }
        return str_mes.getBytes("utf8");
    }

    public Map<String ,String> MakeTextDict(String userName, String content, String localip, String localname){
        Map<String ,String > dict_send=new HashMap<String ,String >();
        dict_send.put("type","TEXT_MES");
        String encryptContent=Encrypt(content);
        dict_send.put("content",encryptContent);
        dict_send.put("ip",localip);
        dict_send.put("localname",localname);
        String encryptUsername=Encrypt(userName);
        dict_send.put("username",encryptUsername);
        return dict_send;
    }

    public Map<String ,String > MakeRegisterDict(String userName, String passWord, String localIp, String localName){
        Map<String, String> map = new HashMap<String, String>();
        map.put("username",userName);
        map.put("type","REGISTER_MES");
        map.put("status","register");
        map.put("ip",localIp);
        map.put("localname",localName);
        map.put("password",passWord);

        return map;
    }
}

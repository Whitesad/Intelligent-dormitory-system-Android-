package RSAUnit;

import java.io.*;
import java.security.Key;
import java.security.KeyFactory;  
import java.security.KeyPair;  
import java.security.KeyPairGenerator;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import it.sauronsoftware.base64.Base64;
import org.bouncycastle.asn1.x509.RSAPublicKeyStructure;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.bouncycastle.asn1.*;

import java.security.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/** *//**
 * <p>
 * RSA公钥/私钥/签名工具包
 * </p>
 * <p>
 */
public class RSAUtils {  
  
    /** *//** 
     * 加密算法RSA 
     */  
    public static final String KEY_ALGORITHM = "RSA";  
      
    /** *//** 
     * 签名算法 
     */  
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";  
  
    /** *//** 
     * 获取公钥的key 
     */  
    private static final String PUBLIC_KEY = "RSAPublicKey";  
      
    /** *//** 
     * 获取私钥的key 
     */  
    private static final String PRIVATE_KEY = "RSAPrivateKey";  
      
    /** *//** 
     * RSA最大加密明文大小 
     */  
    private static final int MAX_ENCRYPT_BLOCK = 117;
      
    /** *//** 
     * RSA最大解密密文大小 
     */  
    private static final int MAX_DECRYPT_BLOCK = 128;  

    /**

    * @Description:    储存公钥与私钥，PubKey为PKSC1格式，PriKey为PKCS8的BASE64编码
    * @Type:           Map<String,OBject>

    */
    private Map<String ,String  > RsaKeyPair=new HashMap<String,String >();
    private Cipher LocalCipher;

    /**

    * @Description:    服务器端的公钥及其Cipher等
    * @Type:           String

    */
    private String ServerPubKeyStr=new String();
    private Cipher ServerCipher;

    /** *//** 
     * <p> 
     * 生成密钥对(公钥和私钥) 
     * </p> 
     *  
     * @return 
     * @throws Exception
     */
    public void genKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);  
        keyPairGen.initialize(1024);  
        KeyPair keyPair = keyPairGen.generateKeyPair();

        byte[] pubKeyBytes=keyPair.getPublic().getEncoded();
        SubjectPublicKeyInfo spkInfo = SubjectPublicKeyInfo.getInstance(pubKeyBytes);
        ASN1Primitive primitive = spkInfo.parsePublicKey();
        byte[] publicKeyPKCS1 = primitive.getEncoded();

        PemObject pemObject = new PemObject("RSA PUBLIC KEY", publicKeyPKCS1);
        StringWriter pubKeysWriter = new StringWriter();
        PemWriter pemWriter = new PemWriter(pubKeysWriter);
        pemWriter.writeObject(pemObject);
        pemWriter.close();
        String pubKeyString =  pubKeysWriter.toString();
        this.RsaKeyPair.put(PUBLIC_KEY, pubKeyString);

        //初始化私钥并储存
        byte[] priKeyBytes=keyPair.getPrivate().getEncoded();
        this.RsaKeyPair.put(PRIVATE_KEY, Base64Utils.encode(priKeyBytes));
        //初始化私钥cipher
        byte[] keyBytes = priKeyBytes;
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        this.LocalCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        this.LocalCipher.init(Cipher.DECRYPT_MODE, privateK);

        return;
    }

    public void setServerPubKey(String ServerPubKeyStr) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
        this.ServerPubKeyStr=ServerPubKeyStr;
        StringReader stringReader=new StringReader(ServerPubKeyStr);
        PemReader pemReader=new PemReader(stringReader);
        PemObject pemObject = pemReader.readPemObject();
        //publicKeyPKCS1为从pem里读取出来的bytes
//        byte[] publicKeyPKCS1 = pemObject.getContent();

        SubjectPublicKeyInfo spkInfo = SubjectPublicKeyInfo.getInstance(pemObject.getContent());
        ASN1Primitive primitive = spkInfo.parsePublicKey();

        byte[] publicKeyPKCS1 = primitive.getEncoded();
        RSAPublicKeyStructure rsaPublicKeyStructure=new RSAPublicKeyStructure((ASN1Sequence)ASN1Sequence.fromByteArray(publicKeyPKCS1));
        RSAPublicKeySpec rsaPublicKeySpec=new RSAPublicKeySpec(rsaPublicKeyStructure.getModulus(),rsaPublicKeyStructure.getPublicExponent());
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key pubKey= keyFactory.generatePublic(rsaPublicKeySpec);


        this.ServerCipher=Cipher.getInstance("RSA/ECB/PKCS1Padding");
        this.ServerCipher.init(Cipher.ENCRYPT_MODE,pubKey);
    }

    public static byte[] encryptByPublicKey(byte[] data, String publicKey)  
            throws Exception {
        byte[] keyBytes = Base64Utils.decode(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        // 对数据加密  
//        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); 
        cipher.init(Cipher.ENCRYPT_MODE, publicK);  
        int inputLen = data.length;  
        ByteArrayOutputStream out = new ByteArrayOutputStream();  
        int offSet = 0;  
        byte[] cache;  
        int i = 0;  
        // 对数据分段加密  
        while (inputLen - offSet > 0) {  
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {  
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);  
            } else {  
                cache = cipher.doFinal(data, offSet, inputLen - offSet);  
            }  
            out.write(cache, 0, cache.length);  
            i++;  
            offSet = i * MAX_ENCRYPT_BLOCK;  
        }  
        byte[] encryptedData = out.toByteArray();  
        out.close();  
        return encryptedData;  
    }
    public String encryptByPublicKey(String content) throws UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        byte[] cipherText=this.ServerCipher.doFinal(content.getBytes("utf8"));
        return Base64Utils.encodeToSting(cipherText);
    }

    public static byte[] decryptByPrivateKey(byte[] encryptedData, String privateKey)
            throws Exception {
        byte[] keyBytes = Base64Utils.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
//        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        cipher.init(Cipher.DECRYPT_MODE, privateK);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }
    public String decryptByPrivateKey(String content) throws Exception {
        byte[] contentBytes = Base64Utils.decode(content);
        byte[] decryptBytes =this.LocalCipher.doFinal(contentBytes);
        return new String(decryptBytes,"utf8");
    }

    public String  getPrivateKey(Map<String, Object> keyMap)
            throws Exception {
        return this.RsaKeyPair.get(PRIVATE_KEY);
    }

    public String getPublicKey()
            throws Exception {  
        return this.RsaKeyPair.get(PUBLIC_KEY);
    }  
  
} 

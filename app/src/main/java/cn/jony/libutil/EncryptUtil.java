package cn.jony.libutil;

import android.text.TextUtils;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;

import static cn.pwrd.util.Constants.UTF_8;

@SuppressWarnings("unused")
public class EncryptUtil {
    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException var7) {
            throw new RuntimeException("Huh, MD5 should be supported?", var7);
        } catch (UnsupportedEncodingException var8) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", var8);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        byte[] var6 = hash;
        int var5 = hash.length;

        for (int var4 = 0; var4 < var5; ++var4) {
            byte b = var6[var4];
            if ((b & 255) < 16) {
                hex.append("0");
            }

            hex.append(Integer.toHexString(b & 255));
        }

        return hex.toString();
    }

    public static String getBase64(String str) {
        return new String(Base64.encode(str.getBytes(), Base64.DEFAULT));
    }

    public static String getBase64(byte[] bytes) {
        return new String(Base64.encode(bytes, Base64.DEFAULT));
    }

    public static String decodeBase64(String str) {
        return new String(Base64.decode(str.getBytes(), Base64.DEFAULT));
    }

    /**
     * AES加密
     *
     * @param content    待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的byte[]
     * @throws Exception
     */
    public static byte[] aesEncryptToBytes(String content, String encryptKey) throws Exception {
        KeyGenerator key = KeyGenerator.getInstance("AES");
        key.init(128, new SecureRandom(encryptKey.getBytes()));

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.generateKey().getEncoded(), "AES"));

        return cipher.doFinal(content.getBytes("utf-8"));
    }

    /**
     * AES加密为base 64 code
     *
     * @param content    待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的base 64 code
     * @throws Exception
     */
    public static String aesEncrypt(String content, String encryptKey) throws Exception {
        return getBase64(aesEncryptToBytes(content, encryptKey));
    }

    /**
     * 使用 rfc-1738 {@see https://www.ietf.org/rfc/rfc1738.txt},但是将'*'替换成%2A,规范对map进行编码
     * @param params
     * @param paramsEncoding
     * @return
     */
    public static String getRFC1738Params(Map<String, String> params, String paramsEncoding) {
        if (params != null && !params.isEmpty()) {
            StringBuilder encodedParams = new StringBuilder();

            try {
                Iterator ite = params.entrySet().iterator();

                if (ite.hasNext()) {
                    Map.Entry uee = (Map.Entry) ite.next();
                    if (checkNotEmpty(uee)) {
                        appendKeyValue(paramsEncoding, encodedParams, uee);
                    }
                }

                while (ite.hasNext()) {
                    Map.Entry uee = (Map.Entry) ite.next();
                    if (checkNotEmpty(uee)) {
                        encodedParams.append('&');
                        appendKeyValue(paramsEncoding, encodedParams, uee);
                    }
                }

                return encodedParams.toString();
            } catch (UnsupportedEncodingException ue) {
                throw new RuntimeException("Encoding not supported: " + paramsEncoding, ue);
            }
        } else {
            return "";
        }
    }

    private static boolean checkNotEmpty(Map.Entry uee) {
        return !TextUtils.isEmpty((CharSequence) uee.getKey());
    }

    private static void appendKeyValue(String paramsEncoding, StringBuilder encodedParams, Map.Entry uee)
            throws UnsupportedEncodingException {
        if (paramsEncoding == null) {
            paramsEncoding = UTF_8;
        }
        encodedParams.append(URLEncoder.encode((String) uee.getKey(), paramsEncoding).replaceAll("\\*", "%2A"));
        encodedParams.append('=');
        encodedParams.append(URLEncoder.encode((String) uee.getValue(), paramsEncoding).replaceAll("\\*", "%2A"));
    }
}

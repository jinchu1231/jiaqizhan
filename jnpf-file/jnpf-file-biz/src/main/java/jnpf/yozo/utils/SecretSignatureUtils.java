package jnpf.yozo.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SecretSignatureUtils {
    public static final String SHA256 = "HmacSHA256";

    public SecretSignatureUtils() {
    }

    public static String hmacSHA256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        mac.init(secret_key);
        byte[] array = mac.doFinal(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        byte[] var6 = array;
        int var7 = array.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            byte item = var6[var8];
            sb.append(Integer.toHexString(item & 255 | 256).substring(1, 3));
        }

        return sb.toString().toUpperCase();
    }
}

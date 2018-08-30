package io.github.md678685.minecraft.velocity.connect.util;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HashUtil {

    public static byte[] genSignature(String secret, byte[] data) {
        try {
            SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            mac.update(data);
            return mac.doFinal();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean verifySignature(String secret, byte[] data, byte[] sig) {
        return Arrays.equals(genSignature(secret, data), sig);
    }

}

package io.github.md678685.minecraft.velocity.connect.util;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HashUtil {

    public static boolean verifySignature(String secret, byte[] data, byte[] sig) {
        System.out.println("Started generating hash... ");
        try {
            SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            mac.update(data);
            byte[] dataSig = mac.doFinal();
            System.out.println("Finished generating hash!");
            return Arrays.equals(dataSig, sig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}

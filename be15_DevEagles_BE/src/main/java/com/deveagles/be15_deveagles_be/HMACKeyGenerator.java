package com.deveagles.be15_deveagles_be;

import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class HMACKeyGenerator {

  public static void main(String[] args) {
    try {
      KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA512");
      keyGen.init(512);

      SecretKey secretKey = keyGen.generateKey();

      String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

      System.out.println("HS512 KEY : " + encodedKey);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

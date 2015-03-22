import java.util.*;
import java.security.*;
import javax.crypto.*;


/**
*  Decrypts a string, representing a password, with the Lazy system key
*/

class dpwd {
  public static void main(String args[])
  {
   if (args.length != 1) {
     System.out.println("usage: java dpwd password\n"
     +"or\ndpwd password "
     +"\n\nDecrypts the given password with the Lazy system key defined in Layz.properties\n");
   }
   else
   {
     try
     {     	
      // generate Cipher objects for encoding and decoding
  		
      
      Cipher itsocipher1 = Cipher.getInstance("DES");
      Cipher itsocipher2 = Cipher.getInstance("DES");

        ResourceBundle rb = ResourceBundle.getBundle("Lazy");
        String  LazyKEY = rb.getString("encrypt.key");

      // generate a KeyGenerator object
      RawSecretKey key2 = new RawSecretKey("DES",Hex.fromString(LazyKEY));
      RawSecretKey mykey = (RawSecretKey) key2;
		
		
      // initialize the Cipher objects
      // System.out.println("Initializing ciphers...");
      itsocipher1.init( Cipher.ENCRYPT_MODE, mykey);
      itsocipher2.init( Cipher.DECRYPT_MODE, mykey);

		String msg = args[0];

		byte[] ciphertext = itsocipher2.doFinal(Hex.fromString(msg));
		// print out length and representation of ciphertext 
		// System.out.println("ciphertext.length = " + ciphertext.length);
		System.out.println(new String(ciphertext));
		
     }
     catch (Exception e)
     {
      System.out.println("Caught exception: " + e);
     }
   }
  }
}


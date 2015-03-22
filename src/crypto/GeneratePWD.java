
import xjava.security.*;
import cryptix.util.core.Hex;
import cryptix.provider.key.*;


class GeneratePWD {
  public static void main(String args[])
  {
   if (args.length != 2)
     System.out.println("Usage: java GeneratePWD pwd rawkey");
   else
   {
     try
     {     	
      // generate Cipher objects for encoding and decoding
  		
      
      Cipher itsocipher1 = Cipher.getInstance(
        	Cipher.getInstance("DES"),
        	(Mode)Mode.getInstance("CBC"),
        	PaddingScheme.getInstance("PKCS#5")
    	);

      Cipher itsocipher2 = Cipher.getInstance(
        	Cipher.getInstance("DES"),
        	(Mode)Mode.getInstance("CBC"),
        	PaddingScheme.getInstance("PKCS#5")
    	);

      // generate a KeyGenerator object
      RawSecretKey key2 = new RawSecretKey("DES",Hex.fromString(args[1]));
		RawKey mykey = (RawKey) key2;
		
		
      // initialize the Cipher objects
      System.out.println("Initializing ciphers...");
      itsocipher1.initEncrypt( mykey);
      itsocipher2.initDecrypt( mykey);

		String msg = args[0];

		byte[] ciphertext = itsocipher1.crypt(msg.getBytes());
		// print out length and representation of ciphertext 
		System.out.println("ciphertext.length = " + ciphertext.length);
		System.out.println("Representation = "+Hex.toString(ciphertext));
	
		// decrypt ciphertext 
		ciphertext = itsocipher2.crypt(ciphertext);
		System.out.println("plaintext.length = " + ciphertext.length);
		//////////
		System.out.println("representation = "+new String(ciphertext));
		System.out.println("representation = "+Hex.toString(ciphertext));
		
     }
     catch (Exception e)
     {
      System.out.println("Caught exception: " + e);
     }
   }
  }
}


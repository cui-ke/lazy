import java.io.*;
import java.security.*;
import xjava.security.*;
import java.math.*;
import cryptix.util.core.BI;
import cryptix.util.core.ArrayUtil;
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
		
		
    /*  KeyGenerator KG = KeyGenerator.getInstance("DES");
      System.out.println("Using algorithm " + KG.getAlgorithm());

      // generate a DES key
      Key mykey = KG.generateKey();
      System.out.println("Key = "+mykey.getFormat());
*/
      // initialize the Cipher objects
      System.out.println("Initializing ciphers...");
      itsocipher1.initEncrypt( mykey);
      itsocipher2.initDecrypt( mykey);
///////////////
		String msg = arg[0];

		byte[] ciphertext = itsocipher1.crypt(msg.getBytes());
		// print out length and representation of ciphertext 
		System.out.println("ciphertext.length = " + ciphertext.length);
		System.out.println("Representation = "+Hex.toReversedString(ciphertext));

		/*BigInteger Bciph = new BigInteger(ciphertext);
		String w = cryptix.util.core.BI.dumpString(Bciph);
		System.out.println("Ciphertext for DES encryption = " + w);*/
	
		// decrypt ciphertext 
		ciphertext = itsocipher2.crypt(ciphertext);
		System.out.println("plaintext.length = " + ciphertext.length);
		//////////
		System.out.println("representation = "+new String(ciphertext));
		System.out.println("representation = "+Hex.toReversedString(ciphertext));
		
		
		/////////
		/*Bciph = new BigInteger(ciphertext);
		w = cryptix.util.core.BI.dumpString(Bciph);
		System.out.println("Plaintext for DES encryption = " + w);*/
	
	
	
///////////////


      // creating the encrypting cipher stream
      System.out.println("Creating the encrypting cipher stream...");
      FileInputStream fis = new FileInputStream(args[0]);
      CipherInputStream cis1 = new CipherInputStream(fis, itsocipher1);

      // creating the decrypting cipher stream
      System.out.println("Creating the decrypting cipher stream...");
      CipherInputStream cis2 = new CipherInputStream(cis1, itsocipher2);

      // writing the decrypted data to output file
      System.out.println("Writing the decrypted data to output file " + args[1]);
      FileOutputStream fos = new FileOutputStream(args[1]);
      byte[] b2 = new byte[1024];
      int i2 = cis2.read(b2);
      while (i2 != -1)
      {
        fos.write(b2, 0, i2);
        i2 = cis2.read(b2);
      }
      fos.close();
      cis1.close();
      cis2.close();
     }
     catch (Exception e)
     {
      System.out.println("Caught exception: " + e);
     }
   }
  }
}


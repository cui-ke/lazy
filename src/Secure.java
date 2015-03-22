import java.security.*;
import javax.crypto.*;

class Secure {
    
    private static RawSecretKey mykey;
    static boolean nocoding;
    private static Key internalkey ;
    
    public static void init(String rawhexkey,boolean ident){
        try {
            nocoding=ident;
            RawSecretKey key2 = new RawSecretKey("DES",Hex.fromString(rawhexkey));
            mykey = (RawSecretKey) key2;
            KeyGenerator KG = KeyGenerator.getInstance("DES");
            internalkey = KG.generateKey(); // generate a internal key for encoded url
        }
        catch (Exception e) {
            System.out.println("*** ERROR in SECURE.init exception: " + e);
        }
    }
    
    
    public static String encryptpwd(String msg){
        if (nocoding) return msg;
        try {
            Cipher ENC = Cipher.getInstance("DES");
	    	ENC.init(Cipher.ENCRYPT_MODE , mykey);
            byte[] ciphertext = ENC.doFinal(msg.getBytes());
            if (ns.verboseCrypto) System.out.println(msg+" -> encrypt pwd = "+Hex.toString(ciphertext));
            return Hex.toString(ciphertext);
        }
        catch (Exception e) {
            System.out.println("*** ERROR in SECURE.encryptpwd: exception " + e);
        }
        return "";
    }
    
    public static String decryptpwd(String msg){
        if (nocoding) return msg;
        try {
            byte[] tempo=Hex.fromString(msg);
            Cipher DEC1 = Cipher.getInstance("DES");
            DEC1.init(Cipher.DECRYPT_MODE, mykey);
            byte[] ciphertext = DEC1.doFinal(tempo);
            String s=new String(ciphertext);
            if (ns.verboseCrypto) System.out.println(msg+" -> decrypt pwd = "+s);
            return s;
        }
        catch (Exception e) {
            System.out.println("*** ERROR in SECURE.decryptpwd exception: " + e);
        }
        return "";
    }
    
    public static String encrypt(String msg){
        if (nocoding) return msg;
        try {
            Cipher ENC = Cipher.getInstance("DES");            
            ENC.init(Cipher.ENCRYPT_MODE, internalkey);
            byte[] ciphertext = ENC.doFinal(msg.getBytes());
            if (ns.verboseCrypto) System.out.println(msg+" -> encrypt = "+Hex.toString(ciphertext));
            return Hex.toString(ciphertext);
        }
        catch (Exception e) {
            System.out.println("*** ERROR in SECURE.encrypt exception: " + e);
        }
        return "";
    }
    
    public static String decrypt(String msg){
        if (nocoding) return msg;
        try {
            byte[] tempo=Hex.fromString(msg);
            Cipher DEC1 = Cipher.getInstance("DES");
            DEC1.init(Cipher.DECRYPT_MODE, internalkey);
            byte[] ciphertext = DEC1.doFinal(tempo);
            String s=new String(ciphertext);
            if (ns.verboseCrypto) System.out.println(msg+" -> decrypt = "+s);
            return s;
        }
        catch (Exception e) {
            System.out.println("*** ERROR in SECURE.decrypt exception: " + e);
        }
        return "";
    }
    
    public static String[] decryptParam(String[] params){
        if (params == null) return null;
        for (int i = 0; i < params.length; i++)
            params[i]= decrypt(params[i]);
        return params;
    }
    
    public static StringBuffer transformPrivatePart(StringBuffer content){
        
        String s = content.toString();
        if (s.indexOf("<<$$")==-1) return content; // no action parameter
        
        int ix = 0, ixf = 0;
        
        while ((ix = s.indexOf("<<$$", ix)) > -1) {
            if ((ixf = s.indexOf("$$>>", ix)) < 0) {System.out.println("ERROR in ActionPrivatePart ix:"+ix+" ixf:"+ixf+" s:"+s);break;} ;
            String paramName = s.substring(ix+4, ixf);
            if (ns.verboseCrypto) System.out.println("encrypt param Name:"+paramName);
            s = s.substring(0, ix) +encrypt(paramName)+ s.substring(ixf+4, s.length());
        }
        StringBuffer newContent= new StringBuffer(s);
        return newContent;
    }
    
}

class LazyFunction {
    public static String encoded(String s){
        return Secure.encryptpwd(s);
    }
}

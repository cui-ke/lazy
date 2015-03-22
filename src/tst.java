 import java.io.*;
 
 public class tst {
 
    static boolean isIntNumber(StreamTokenizer sym) {
        if (sym.sval == null) return false;
        for (int i=0; i<sym.sval.length(); i++) { 
           char c = sym.sval.charAt(i);
           if ( c < '0' || c > '9') return false; }
        return true;
    }
 
   public static void main(String[] a) {
   
        StreamTokenizer sym = new StreamTokenizer(new StringReader(a[0]));
        

        //sym.resetSyntax();
        sym.ordinaryChars('0','9');
        sym.ordinaryChars('-', '-');
        sym.ordinaryChars('.', '.');
        sym.ordinaryChars('/', '/');// Specifies that all characters c in the
        sym.ordinaryChars(' ',' ');
        sym.wordChars('_', '_'); // Specifies that all characters c in the range
        // low <= c <= high are word constituents.
        sym.wordChars('0', '9'); sym.wordChars('A', 'Z'); sym.wordChars('a', 'z');
        // range low <= c <= high are "ordinary" in
        // this tokenizer.
        sym.slashSlashComments(true);
        sym.slashStarComments(true);
        
        
        while (sym.ttype != StreamTokenizer.TT_EOF) {
          try{
           sym.nextToken();
           System.out.println(" "+sym.ttype+" "+sym.sval+" "+isIntNumber(sym));
          } catch (IOException e) {System.out.println("error");}
        }
   }
}
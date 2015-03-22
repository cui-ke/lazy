/* Lazy hypertext view system
Copyright (C) 2002-2003,  ISI Research Group, CUI, University of Geneva

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
You can also find the GNU GPL at http://www.gnu.org/copyleft/gpl.html

You can contact the ISI research group at http://cui.unige.ch/isi

*/

import java.io.*;

	
public class lcs {

	public static void main (String [] args) throws FileNotFoundException{	
	    PrintWriter out;	    	    
	    if (args.length > 1) out = new PrintWriter(new FileOutputStream(args[1]));
	    else out = new PrintWriter(new FileOutputStream("out.sparql"));	    
      CompilerSparql.global_init();
		
      /* Compiler a = new Compiler(new StringReader("define project NODE node node_test {x} from x  \nend" ), out);
		//Compiler a = new Compiler(new StringReader("define project NODE node node_test {x} from x  \nend" ), out);
		 */
		CompilerSparql a = new CompilerSparql(new FileReader(args[0]),new FileReader(args[0]), out);
      a.startrule();
		out.close();
     
      //Compiler.compileProject("TERMINO");
	}	
}
	

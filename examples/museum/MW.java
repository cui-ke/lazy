public class MW {

	public static String firstName(Object args) {
	
		String p = ((String[])(args))[0];
		String p2 = "* "+p+" *";

		QueryResult q = DBServices.execSQLonDB("select * from nodes", "DICTLAZY", true);
		
		if (q.valid) p2+="-valid"; else p2+="-invalid";
		
		return p2;
	}
	
	public static String fName(String[] args) {
	
	
		String p2 ;
		
		System.out.println("MW-DBG-------nb. par="+args.length+"-----"+args);
		if (args.length==0) p2 = "---NO PARAMS---";
		else p2 = "--- "+args[0]+" ---";

		QueryResult q = DBServices.execSQLonDB("select * from nodes", "DICTLAZY", true);
		
		if (q.valid) p2+="-valid"; else p2+="-invalid";
		
		return p2;
		
		
	}
	
	public static String fName() { return "__ no arguments __"; }
	
	public static String fName(String a) { return "__ one argument __"+a; }
		
	public static String fName(String a, String b, String c) { 
		return "__ three argument __"+a+b+c; }
}
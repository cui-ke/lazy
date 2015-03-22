import java.sql.*;

class ConnectionDB {
    public Connection con;
    
    public String url = "";
    public String user = "";
    public String pwd = "";
    public String driver = "";
    public String msg = "";
    
    ConnectionDB(String wdriver, String wurl, String wuser, String wpwd, String wmsg) {
        driver=wdriver;url=wurl;user=wuser;pwd=wpwd;msg=wmsg;
        System.out.println(
        "ConnectionDB:"+msg+" Initializing DB connection with :\n url="
        + url
        + "\n user="
        + user
        + "\n driver="
        + driver);
        try {
            Class.forName(driver);
            con = DriverManager.getConnection(url, user, pwd);            
            System.out.println("ConnectionDB: connection "+msg+" OK\n");
        }
        catch (Exception e) {
            System.out.println("ConnectionDB: "+msg+" *** Unable to connect to database " + url);
            System.out.println(e.toString());
            return;
        }
    }
    public void finalize() {
        try {
            con.close();
        }
        catch (SQLException e) {
            System.out.println("ConnectionDB: "+msg+" SQLError: " + e.getMessage());
        }
    }
	public void reConnect() {
        if (con != null) try { con.close(); } catch (SQLException e) { }
		con = null;
		try {
		    Class.forName(driver);
            con = DriverManager.getConnection(url, user, pwd);            
            System.out.println("ConnectionDB: connection "+msg+" OK\n");
        }
        catch (Exception e) {
            System.out.println("ConnectionDB: "+msg+" *** Unable to connect to database " + url);
            System.out.println(e.toString());
        }
	}	

}

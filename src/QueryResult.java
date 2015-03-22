import java.sql.*;


class QueryResult {

    Statement stmt;
    
    public ResultSet result;
    public boolean valid = true;
    public String msg = "";
    public String sql;
    public int nbUpdated = 0;
    
    QueryResult(String wsql) {
        sql = wsql;
    }
    public void finalize() {
        try {
            stmt.close();
        }
        catch (SQLException e) {
            System.out.println("QueryResult: SQLError: " + e.getMessage());
        }
    }
}

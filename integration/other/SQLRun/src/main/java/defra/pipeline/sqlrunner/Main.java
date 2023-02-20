package defra.pipeline.sqlrunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import com.microsoft.aad.adal4j.AuthenticationException;

public class Main {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        if (args.length != 2) {
            System.out.println("Requires two arguments: 'a jdbc connection string' 'your sql query'");
        } else {
            Connection conn = DriverManager.getConnection(args[0]);
            Statement stat = conn.createStatement();
            System.out.println("Running query: " + args[1]);
            try {
                ResultSet resObj = stat.executeQuery(args[1]);
                ResultSetMetaData rsmd = resObj.getMetaData();
                int numberOfColumns = rsmd.getColumnCount();
                while (resObj.next()) {
                    for (int i =1; i <= numberOfColumns; i++) {
                        System.out.print(resObj.getString(i)+ " ");
                    }
                    System.out.println();
                }
                resObj.close();
            } catch (com.microsoft.sqlserver.jdbc.SQLServerException e) {
                if (!e.getMessage().equals("The statement did not return a result set.")) {
                    System.out.print("Error returned: ");
                }
                System.out.println(e.getMessage());
            }
            System.out.println("Query executed");
            stat.close();
            conn.close();
        }
    }
}

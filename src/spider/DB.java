/*
 * File : DB.java
 * Author : Shuai Li <lishuaihenu@gmail.com>
 * Purpose : DB util for insert caputre result
 * Created : 2013-09-19 by Shuai Li <lishuaihenu@gmail.com>
 */
package spider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author gladiator
 */
public class DB {

    public Connection conn = null;
    public Statement statement = null;
    String db = "jdbc:mysql://localhost:3306/jd";
    String updateStr = "INSERT INTO product(id, name, category, price, description)";

    public DB() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");

        conn = DriverManager.getConnection(db, "test", "test");
        statement = conn.createStatement();
    }

    public void insert(int id, String name, String category, int price,
            String description) throws SQLException {

        String valueStr = "\tVALUES\n"
                + "\t(" + id + ", \n"
                + "\t'" + name + "', \n"
                + "\t'" + category + "', \n"
                + "\t" + price + ", \n"
                + "\t'" + description + "'\n"
                + ")";

        statement.executeUpdate(updateStr + valueStr);
    }

    public void close() throws SQLException {
        statement.close();
        conn.close();
    }
}
package hooks;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class GlobalHooks {

    public static void disableWarning() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe u = (Unsafe) theUnsafe.get(null);

            Class cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field logger = cls.getDeclaredField("logger");
            u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
        } catch (Exception e) {
            // ignore
        }
    }


    public static void truncateTable() {
        Connection connection = null;
        try {

            // Load the MySQL JDBC driver

            String driverName = "com.mysql.jdbc.Driver";

            Class.forName(driverName);

            // Create a connection to the database

            String serverName = "localhost";

            String schema = "bcxtim";

            String url = "jdbc:mysql://" + serverName + "/" + schema;

            String username = "root";

            String password = "13123974";

            connection = DriverManager.getConnection(url, username, password);


            System.out.println("Successfully Connected to the database!");


        } catch (ClassNotFoundException e) {

            System.out.println("Could not find the database driver " + e.getMessage());
        } catch (SQLException e) {

            System.out.println("Could not connect to the database " + e.getMessage());
        }

        try {

            Statement statement = connection.createStatement();


            statement.executeUpdate("TRUNCATE book");


            System.out.println("Successfully truncated bcxtim");

        } catch (SQLException e) {

            System.out.println("Could not truncate bcxtim " + e.getMessage());
        }
    }

}

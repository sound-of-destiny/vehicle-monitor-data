package cn.edu.sdu.vehicleMonitorData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JT808Mysql {
    public static Connection connect_145() {
        Connection conn = null;
        do {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(
                        "jdbc:mysql://202.194.14.145:3306/vehicle-monitoring?useSSL=true",
                        "root",
                        "qlscadmin"
                );
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException sqle) {
                try {
                    sqle.printStackTrace();
                    Thread.sleep(1000);
                    System.out.println("try to connect to Database");
                } catch (InterruptedException e) {
                    System.out.println("Database Error");
                }
            }
        } while (conn == null);
        return conn;
    }
}

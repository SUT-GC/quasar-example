package concurrent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DoIo {
    public static final String DBDRIVER = "com.mysql.jdbc.Driver";
    //连接地址是由各个数据库生产商单独提供的，所以需要单独记住
    public static final String DBURL = "jdbc:mysql://localhost:3306/thread";
    //连接数据库的用户名
    public static final String DBUSER = "root";
    //连接数据库的密码
    public static final String DBPASS = "gc";

    public static boolean selectDB() {
        boolean result = Boolean.FALSE;
        Connection con = null; //表示数据库的连接对象
        ResultSet resultSet = null;

        String sql = "select * from thread_test order by id asc limit 1 offset 0";

        try {
            Class.forName(DBDRIVER); //1、使用CLASS 类加载驱动程序

            System.out.println(sql);
            con = DriverManager.getConnection(DBURL, DBUSER, DBPASS); //2、连接数据库
            Statement stmt = con.createStatement();

            resultSet = stmt.executeQuery(sql);
            int id = resultSet.getInt("id");
            System.out.println(id);
            result = Boolean.TRUE;

        } catch (Exception e) {
            e.printStackTrace();
            result = Boolean.FALSE;
        }
        return result;
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        selectDB();
        long end = System.currentTimeMillis();

        System.out.println(end - start);
    }
}

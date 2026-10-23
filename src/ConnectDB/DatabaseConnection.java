package ConnectDB;
//Người làm Phạm Thanh Huy
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

   
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyCuaHangTienLoi;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456";

  
    private DatabaseConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

   
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

  
    /**
     * Trả về connection MỚI mỗi lần gọi — các DAO đều đóng connection bằng
     * try-with-resources, nếu dùng chung 1 connection (singleton) thì lần gọi
     * sau sẽ đóng connection đang chạy transaction → lỗi "connection closed".
     */
    public Connection getConnection() throws SQLException {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            throw e;
        }
        return connection;
    }

  
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

  
    public boolean testConnection() {
        try {
            getConnection();
            return !connection.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
}

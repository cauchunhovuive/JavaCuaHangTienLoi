package connectDB;
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

    public Connection getConnection() throws SQLException {
        System.out.println("[DatabaseConnection] getConnection() called");
        long start = System.currentTimeMillis();
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DatabaseConnection] connection established in " + (System.currentTimeMillis() - start) + "ms");
        } catch (SQLException e) {
            System.out.println("[DatabaseConnection] connection failed after " + (System.currentTimeMillis() - start) + "ms: " + e.getMessage());
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

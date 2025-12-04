import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {
    private static final String PROPERTIES_FILE = "app.properties";

    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(PROPERTIES_FILE)) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("Error loading app.properties: " + e.getMessage());
            throw new SQLException("Unable to load DB config", e);
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("Could not load MySQL driver: " + e.getMessage());
        }

        return DriverManager.getConnection(url, user, password);
    }
}

package accrawler.common;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtilities {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/accrawler";

	// Database credentials
	static final String USER = "root";
	static final String PASS = "123";

	static boolean initialized = false;
	static Connection dbConnection = null;
	
	public static void initDbConnection() {
		try {
			Class.forName(JDBC_DRIVER);
			dbConnection = DriverManager.getConnection(DB_URL, USER, PASS);
			initialized = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Connection getDbConnection() {
		if (!initialized) {
			initDbConnection();
		}
		return dbConnection;
	}
	
	public static void closeDbConnection() {
		try {
			dbConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

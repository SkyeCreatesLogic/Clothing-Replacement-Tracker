
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ClothingDatabase {

    // Connect to the SQLite database.
    public static Connection connect(String databasePath) throws SQLException {

        if (databasePath == null || databasePath.isBlank()) {
            throw new SQLException("Please select a database file.");
        }

        // Load the SQLite driver.
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver could not be loaded.", e);
        }

        String url = "jdbc:sqlite:" + databasePath;
        return DriverManager.getConnection(url);
    }

    // Create the clothing table.
    public static void createTable(Connection connection) throws SQLException {

        String sql = """
                CREATE TABLE IF NOT EXISTS clothing (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    category TEXT NOT NULL,
                    size TEXT NOT NULL,
                    color TEXT NOT NULL,
                    purchase_date TEXT NOT NULL,
                    number_of_wears INTEGER NOT NULL,
                    expected_wears INTEGER NOT NULL
                )
                """;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}

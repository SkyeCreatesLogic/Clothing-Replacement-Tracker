
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseTest {

    public static void main(String[] args) {

        try (Connection connection =
                     ClothingDatabase.connect("clothing.db")) {

            ClothingDatabase.createTable(connection);

            System.out.println("SQLite database connected successfully!");
            System.out.println("Clothing table created successfully!");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}

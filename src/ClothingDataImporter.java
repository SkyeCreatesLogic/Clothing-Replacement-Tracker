
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ClothingDataImporter {

    public static void main(String[] args) {

        String databasePath = "clothing.db";
        Path textFile = Path.of("src", "clothing.txt");

        if (!Files.exists(textFile)) {
            System.out.println("Cannot find src/clothing.txt");
            return;
        }

        try (Connection connection = ClothingDatabase.connect(databasePath)) {

            ClothingDatabase.createTable(connection);

            // Prevent importing the same records twice.
            try (PreparedStatement countStatement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM clothing");
                 ResultSet result = countStatement.executeQuery()) {

                if (result.next() && result.getInt(1) > 0) {
                    System.out.println("Database already contains clothing records.");
                    System.out.println("Import skipped to prevent duplicates.");
                    return;
                }
            }

            List<String> lines = Files.readAllLines(textFile);

            String sql = """
                    INSERT INTO clothing
                    (category, size, color, purchase_date,
                     number_of_wears, expected_wears)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;

            int imported = 0;

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                for (String line : lines) {

                    if (line.isBlank()) {
                        continue;
                    }

                    String[] parts = line.split("\\|", -1);

                    if (parts.length != 6) {
                        System.out.println("Skipped invalid line: " + line);
                        continue;
                    }

                    try {
                        String category = parts[0].trim();
                        String size = parts[1].trim();
                        String color = parts[2].trim();
                        String purchaseDate = parts[3].trim();
                        int wears = Integer.parseInt(parts[4].trim());
                        int expectedWears = Integer.parseInt(parts[5].trim());

                        java.time.LocalDate.parse(purchaseDate);

                        if (category.isEmpty() || size.isEmpty()
                                || color.isEmpty() || wears < 0
                                || expectedWears <= 0) {
                            throw new IllegalArgumentException("Invalid clothing values");
                        }

                        statement.setString(1, category);
                        statement.setString(2, size);
                        statement.setString(3, color);
                        statement.setString(4, purchaseDate);
                        statement.setInt(5, wears);
                        statement.setInt(6, expectedWears);

                        statement.executeUpdate();
                        imported++;

                    } catch (IllegalArgumentException e) {
                        System.out.println("Skipped invalid line: " + line);
                    }
                }
            }

            System.out.println("Successfully imported " + imported + " clothing items!");
            System.out.println("Database: " + databasePath);

        } catch (Exception e) {
            System.out.println("Import error: " + e.getMessage());
        }
    }
}


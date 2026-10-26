
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ClothingTracker {

    private final List<ClothingItem> clothingItems = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);
    private Path filePath;

    private final List<String> validCategories = Arrays.asList(
            "socks", "shirts", "pants", "coats", "underwear", "bras"
    );

    private final List<String> validSizes = Arrays.asList(
            "XS", "S", "M", "L", "XL", "XXL", "One Size"
    );

    private final List<String> validColors = Arrays.asList(
            "black", "white", "gray", "grey", "blue", "red", "green",
            "brown", "pink", "purple", "yellow", "orange", "nude",
            "beige", "khaki", "navy"
    );

    public ClothingTracker() {
        filePath = findClothingFile();
    }

    public ClothingTracker(Path filePath) {
        this.filePath = filePath;
    }

    private Path findClothingFile() {
        Path current = Path.of("").toAbsolutePath();

        for (int i = 0; i < 6 && current != null; i++) {
            Path sourceFile = current.resolve("src").resolve("clothing.txt");
            if (Files.exists(sourceFile)) {
                return sourceFile;
            }

            Path rootFile = current.resolve("clothing.txt");
            if (Files.exists(rootFile)) {
                return rootFile;
            }

            current = current.getParent();
        }

        return Path.of("clothing.txt").toAbsolutePath();
    }

    public List<ClothingItem> getClothingItems() {
        return clothingItems;
    }

    public boolean addClothingItem(ClothingItem item) {
        if (item == null || item.getExpectedWears() <= 0
                || item.getNumberOfWears() < 0
                || item.getNumberOfWears() > item.getExpectedWears()
                || item.getPurchaseDate() == null
                || item.getPurchaseDate().isAfter(LocalDate.now())
                || !validCategories.contains(item.getCategory().toLowerCase())
                || !validSizes.contains(item.getSize())
                || !validColors.contains(item.getColor().toLowerCase())) {
            return false;
        }

        clothingItems.add(item);
        saveClothingItems();
        return true;
    }

    public boolean removeClothingItem(ClothingItem item) {
        if (item == null || !clothingItems.remove(item)) {
            return false;
        }

        saveClothingItems();
        return true;
    }

    public boolean updateClothingItem(
            ClothingItem item, ClothingItem updatedInformation) {

        if (item == null || updatedInformation == null
                || !clothingItems.contains(item)
                || updatedInformation.getExpectedWears() <= 0
                || updatedInformation.getNumberOfWears() < 0
                || updatedInformation.getNumberOfWears()
                > updatedInformation.getExpectedWears()
                || updatedInformation.getPurchaseDate() == null
                || updatedInformation.getPurchaseDate().isAfter(LocalDate.now())
                || !validCategories.contains(
                updatedInformation.getCategory().toLowerCase())
                || !validSizes.contains(updatedInformation.getSize())
                || !validColors.contains(
                updatedInformation.getColor().toLowerCase())) {
            return false;
        }

        item.setCategory(updatedInformation.getCategory());
        item.setSize(updatedInformation.getSize());
        item.setColor(updatedInformation.getColor());
        item.setPurchaseDate(updatedInformation.getPurchaseDate());
        item.setNumberOfWears(updatedInformation.getNumberOfWears());
        item.setExpectedWears(updatedInformation.getExpectedWears());

        saveClothingItems();
        return true;
    }

    public double calculateWearPercentage(ClothingItem item) {
        if (item == null || item.getExpectedWears() <= 0
                || item.getNumberOfWears() < 0) {
            throw new IllegalArgumentException("Invalid clothing wear information.");
        }

        return (double) item.getNumberOfWears()
                / item.getExpectedWears() * 100;
    }

    public boolean loadClothingItems(Path path) {
        if (path == null || path.toString().isBlank()
                || !Files.exists(path) || !Files.isRegularFile(path)
                || !Files.isReadable(path)) {
            return false;
        }

        List<ClothingItem> loadedItems = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(path);

            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }

                String[] fields = line.split("\\|", -1);

                if (fields.length != 6) {
                    continue;
                }

                try {
                    String category = fields[0].trim().toLowerCase();
                    String size = fields[1].trim();
                    String color = fields[2].trim().toLowerCase();
                    LocalDate purchaseDate =
                            LocalDate.parse(fields[3].trim());
                    int numberOfWears =
                            Integer.parseInt(fields[4].trim());
                    int expectedWears =
                            Integer.parseInt(fields[5].trim());

                    ClothingItem item = new ClothingItem(
                            category, size, color, purchaseDate,
                            numberOfWears, expectedWears
                    );

                    if (isValidItem(item)) {
                        loadedItems.add(item);
                    }
                } catch (IllegalArgumentException
                         | DateTimeParseException exception) {
                    // Skip rows containing invalid values.
                }
            }

            clothingItems.clear();
            clothingItems.addAll(loadedItems);
            filePath = path;
            return true;

        } catch (IOException exception) {
            return false;
        }
    }

    private boolean isValidItem(ClothingItem item) {
        return item.getExpectedWears() > 0
                && item.getNumberOfWears() >= 0
                && item.getNumberOfWears() <= item.getExpectedWears()
                && item.getPurchaseDate() != null
                && !item.getPurchaseDate().isAfter(LocalDate.now())
                && validCategories.contains(item.getCategory())
                && validSizes.contains(item.getSize())
                && validColors.contains(item.getColor());
    }

    public boolean saveClothingItems() {
        if (filePath == null) {
            return false;
        }

        try {
            Path parent = filePath.toAbsolutePath().getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            List<String> lines = new ArrayList<>();

            for (ClothingItem item : clothingItems) {
                lines.add(
                        item.getCategory() + "|"
                                + item.getSize() + "|"
                                + item.getColor() + "|"
                                + item.getPurchaseDate() + "|"
                                + item.getNumberOfWears() + "|"
                                + item.getExpectedWears()
                );
            }

            Files.write(filePath, lines);
            return true;

        } catch (IOException exception) {
            System.out.println("Error saving clothing data: "
                    + exception.getMessage());
            return false;
        }
    }

    public void start() {
        if (filePath != null && Files.exists(filePath)) {
            if (!loadClothingItems(filePath)) {
                System.out.println("Could not load the default clothing file.");
            }
        } else {
            System.out.println("Default clothing file was not found.");
            System.out.println("Choose option 6 to enter a file location.");
        }

        boolean running = true;

        while (running) {
            System.out.println("\n=== Clothing Replacement Tracker ===");
            System.out.println("1. View Clothing Items");
            System.out.println("2. Add Clothing Item");
            System.out.println("3. Update Clothing Item");
            System.out.println("4. Remove Clothing Item");
            System.out.println("5. Check Wear Percentage");
            System.out.println("6. Load Clothing File");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> displayClothingItems();
                case "2" -> addClothingItemFromInput();
                case "3" -> updateClothingItemFromInput();
                case "4" -> removeClothingItemFromInput();
                case "5" -> checkWearPercentage();
                case "6" -> loadFileFromInput();
                case "7" -> {
                    running = false;
                    System.out.println("Goodbye!");
                }
                default -> System.out.println(
                        "Invalid choice. Please enter a number from 1 to 7."
                );
            }
        }
    }

    private void loadFileFromInput() {
        System.out.print("Enter the full path to the clothing text file: ");
        String enteredPath = scanner.nextLine().trim();

        if (enteredPath.isBlank()) {
            System.out.println("File location cannot be blank. Try again.");
            return;
        }

        try {
            Path requestedPath = Path.of(enteredPath);

            if (loadClothingItems(requestedPath)) {
                System.out.println("File loaded successfully.");
                displayClothingItems();
            } else {
                System.out.println(
                        "Could not load that file. Check the path and try again."
                );
            }
        } catch (RuntimeException exception) {
            System.out.println("Invalid file location. Please try again.");
        }
    }

    private void displayClothingItems() {
        if (clothingItems.isEmpty()) {
            System.out.println("No clothing items are currently loaded.");
            return;
        }

        System.out.println("\n--- Clothing Items ---");

        for (int i = 0; i < clothingItems.size(); i++) {
            System.out.println((i + 1) + ". " + clothingItems.get(i));
        }

        System.out.println("Total clothing items: " + clothingItems.size());
    }

    private void addClothingItemFromInput() {
        System.out.println("\n--- Add Clothing Item ---");

        String category = getValidCategory();
        String size = getValidSize();
        String color = getValidColor();
        LocalDate purchaseDate = getValidPurchaseDate();
        int numberOfWears = getValidInteger(
                "Enter number of wears: ", 0, Integer.MAX_VALUE);
        int expectedWears = getValidInteger(
                "Enter expected number of wears: ",
                Math.max(1, numberOfWears), Integer.MAX_VALUE);

        ClothingItem item = new ClothingItem(
                category, size, color, purchaseDate,
                numberOfWears, expectedWears
        );

        if (addClothingItem(item)) {
            System.out.println("Clothing item added successfully:");
            System.out.println(item);
        } else {
            System.out.println("Item could not be added. Check the entered data.");
        }
    }

    private void updateClothingItemFromInput() {
        if (clothingItems.isEmpty()) {
            System.out.println("There are no clothing items to update.");
            return;
        }

        displayClothingItems();
        int index = getValidInteger(
                "Enter the item number to update: ",
                1, clothingItems.size()) - 1;

        ClothingItem item = clothingItems.get(index);

        boolean updating = true;

        while (updating) {
            System.out.println("\n--- Update Clothing Item ---");
            System.out.println("1. Category");
            System.out.println("2. Size");
            System.out.println("3. Color");
            System.out.println("4. Purchase Date");
            System.out.println("5. Number of Wears");
            System.out.println("6. Expected Wears");
            System.out.println("7. Return to Main Menu");
            System.out.print("Choose a field to update: ");

            String choice = scanner.nextLine().trim();

            String category = item.getCategory();
            String size = item.getSize();
            String color = item.getColor();
            LocalDate date = item.getPurchaseDate();
            int wears = item.getNumberOfWears();
            int expected = item.getExpectedWears();

            switch (choice) {
                case "1" -> category = getValidCategory();
                case "2" -> size = getValidSize();
                case "3" -> color = getValidColor();
                case "4" -> date = getValidPurchaseDate();
                case "5" -> wears = getValidInteger(
                        "Enter number of wears (0 to " + expected + "): ",
                        0, expected);
                case "6" -> expected = getValidInteger(
                        "Enter expected wears (at least " + wears + "): ",
                        Math.max(1, wears), Integer.MAX_VALUE);
                case "7" -> updating = false;
                default -> System.out.println(
                        "Invalid choice. Please enter 1 through 7."
                );
            }

            if (updating && choice.matches("[1-6]")) {
                ClothingItem updatedInformation = new ClothingItem(
                        category, size, color, date, wears, expected
                );

                if (updateClothingItem(item, updatedInformation)) {
                    System.out.println("Item updated successfully:");
                    System.out.println(item);
                } else {
                    System.out.println(
                            "Update failed. Please check the information."
                    );
                }
            }
        }
    }

    private void removeClothingItemFromInput() {
        if (clothingItems.isEmpty()) {
            System.out.println("There are no clothing items to remove.");
            return;
        }

        displayClothingItems();
        int index = getValidInteger(
                "Enter the item number to remove: ",
                1, clothingItems.size()) - 1;

        ClothingItem item = clothingItems.get(index);

        System.out.println("Selected item: " + item);
        System.out.print("Are you sure you want to remove it? (yes/no): ");
        String confirmation = scanner.nextLine().trim();

        if (confirmation.equalsIgnoreCase("yes")) {
            if (removeClothingItem(item)) {
                System.out.println("Clothing item removed successfully.");
            } else {
                System.out.println("Could not remove the clothing item.");
            }
        } else {
            System.out.println("Removal cancelled.");
        }
    }

    private void checkWearPercentage() {
        if (clothingItems.isEmpty()) {
            System.out.println("There are no clothing items to check.");
            return;
        }

        displayClothingItems();
        int index = getValidInteger(
                "Enter the item number to check: ",
                1, clothingItems.size()) - 1;

        ClothingItem item = clothingItems.get(index);

        try {
            System.out.printf(
                    "Wear percentage: %.2f%%%n",
                    calculateWearPercentage(item)
            );
        } catch (IllegalArgumentException exception) {
            System.out.println("Cannot calculate wear percentage for this item.");
        }
    }

    private String getValidCategory() {
        while (true) {
            System.out.println("Available categories: " + validCategories);
            System.out.print("Enter category: ");
            String value = scanner.nextLine().trim().toLowerCase();

            if (validCategories.contains(value)) {
                return value;
            }

            System.out.println("Invalid category. Please choose from the list.");
        }
    }

    private String getValidSize() {
        while (true) {
            System.out.println("Available sizes: " + validSizes);
            System.out.print("Enter size: ");
            String value = scanner.nextLine().trim();

            for (String validSize : validSizes) {
                if (validSize.equalsIgnoreCase(value)) {
                    return validSize;
                }
            }

            System.out.println("Invalid size. Please choose from the list.");
        }
    }

    private String getValidColor() {
        while (true) {
            System.out.println("Available colors: " + validColors);
            System.out.print("Enter color: ");
            String value = scanner.nextLine().trim().toLowerCase();

            if (validColors.contains(value)) {
                return value;
            }

            System.out.println("That color is not available.");
            System.out.println("Choose the closest available color from the list.");
        }
    }

    private LocalDate getValidPurchaseDate() {
        while (true) {
            System.out.print("Enter purchase date (YYYY-MM-DD): ");
            String value = scanner.nextLine().trim();

            if (value.isBlank()) {
                System.out.println("Purchase date cannot be blank.");
                continue;
            }

            try {
                LocalDate date = LocalDate.parse(value);

                if (date.isAfter(LocalDate.now())) {
                    System.out.println("Purchase date cannot be in the future.");
                    continue;
                }

                return date;
            } catch (DateTimeParseException exception) {
                System.out.println(
                        "Invalid date. Use the format YYYY-MM-DD."
                );
            }
        }
    }

    private int getValidInteger(String prompt, int minimum, int maximum) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            try {
                int number = Integer.parseInt(value);

                if (number >= minimum && number <= maximum) {
                    return number;
                }

                System.out.println(
                        "Enter a number between " + minimum + " and " + maximum + "."
                );
            } catch (NumberFormatException exception) {
                System.out.println("Invalid number. Please enter a whole number.");
            }
        }
    }
}

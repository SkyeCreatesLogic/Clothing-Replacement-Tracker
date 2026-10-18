
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClothingTracker {

    // This list holds all clothing items while the program is running.
    private final List<ClothingItem> clothingItems = new ArrayList<>();

    // This lets the user enter information in the console.
    private final Scanner scanner = new Scanner(System.in);

    // This is where the clothing information is saved between runs.
    private final Path filePath = Path.of("src", "clothing.txt");

    // These are the clothing categories the user can choose from.
    private final List<String> validCategories = List.of(
            "socks", "shirts", "pants", "coats", "underwear", "bras"
    );

    // These are the colors the program accepts.
    private final List<String> validColors = List.of(
            "black", "white", "gray", "grey", "blue", "red",
            "green", "brown", "pink", "purple", "yellow",
            "orange", "nude", "beige", "khaki", "navy"
    );

    // This starts the program and keeps showing the menu until the user exits.
    public void start() {
        loadClothingItems();

        boolean running = true;

        while (running) {
            System.out.println("\n=== Clothing Replacement Tracker ===");
            System.out.println("1. View Clothing Items");
            System.out.println("2. Add Clothing Item");
            System.out.println("3. Update Clothing Item");
            System.out.println("4. Remove Clothing Item");
            System.out.println("5. Check Wear Percentage");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            // This runs the action that matches the user's menu choice.
            switch (choice) {
                case "1":
                    displayClothingItems();
                    break;
                case "2":
                    addClothingItem();
                    break;
                case "3":
                    updateClothingItem();
                    break;
                case "4":
                    removeClothingItem();
                    break;
                case "5":
                    displayWearPercentage();
                    break;
                case "6":
                    saveClothingItems();
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    // An invalid choice brings the user back to the menu.
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // This displays every clothing item currently in the list.
    private void displayClothingItems() {
        if (clothingItems.isEmpty()) {
            System.out.println("No clothing items found.");
            return;
        }

        // The item numbers make it easier to select an item later.
        for (int i = 0; i < clothingItems.size(); i++) {
            System.out.println((i + 1) + ". " + clothingItems.get(i));
        }
    }

    // This keeps asking until the user enters an allowed clothing category.
    private String readCategory() {
        while (true) {
            System.out.println(
                    "Enter category (socks, shirts, pants, coats, underwear, bras):"
            );

            String category = scanner.nextLine().trim().toLowerCase();

            if (validCategories.contains(category)) {
                return category;
            }

            System.out.println("Invalid category. Please try again.");
        }
    }

    // This checks the size and asks again if the size is not allowed.
    private String readSize(String prompt) {
        while (true) {
            System.out.print(prompt);
            String size = scanner.nextLine().trim().toUpperCase();

            if (List.of("XS", "S", "M", "L", "XL", "XXL").contains(size)) {
                return size;
            }

            System.out.println(
                    "Invalid size. Enter XS, S, M, L, XL, or XXL."
            );
        }
    }

    // This checks the color and asks again if the color is not recognized.
    private String readColor(String prompt) {
        while (true) {
            System.out.print(prompt);
            String color = scanner.nextLine().trim();

            if (validColors.contains(color.toLowerCase())) {
                return color;
            }

            System.out.println(
                    "Invalid color. Please enter a recognized color."
            );
        }
    }

    // This checks the date format and prevents future purchase dates.
    private LocalDate readPurchaseDate() {
        while (true) {
            System.out.print("Enter purchase date (YYYY-MM-DD): ");

            try {
                LocalDate date = LocalDate.parse(
                        scanner.nextLine().trim()
                );

                if (date.isAfter(LocalDate.now())) {
                    System.out.println(
                            "Purchase date cannot be in the future."
                    );
                    continue;
                }

                return date;

            } catch (DateTimeParseException e) {
                System.out.println(
                        "Invalid date. Please use YYYY-MM-DD."
                );
            }
        }
    }

    // This collects the information needed to add a clothing item.
    private void addClothingItem() {
        String category = readCategory();
        String size = readSize("Enter size: ");
        String color = readColor("Enter color: ");
        LocalDate purchaseDate = readPurchaseDate();

        // The number of wears cannot be negative.
        int numberOfWears = readWholeNumber(
                "Enter number of wears: ", 0
        );

        // Expected wears must be at least the current number of wears.
        int expectedWears;

        while (true) {
            expectedWears = readWholeNumber(
                    "Enter expected number of wears: ", 1
            );

            if (expectedWears >= numberOfWears) {
                break;
            }

            System.out.println(
                    "Expected wears must be at least the current number of wears."
            );
        }

        // This creates the new clothing item with the information entered.
        ClothingItem item = new ClothingItem(
                category, size, color, purchaseDate,
                numberOfWears, expectedWears
        );

        // The new item is added to the list and saved to the file.
        clothingItems.add(item);
        saveClothingItems();

        System.out.println("Clothing item added successfully.");
    }

    // This lets the user change the size, color, and number of wears.
    private void updateClothingItem() {
        displayClothingItems();

        if (clothingItems.isEmpty()) {
            return;
        }

        int index = readItemNumber();

        // A value of -1 means the user chose to cancel.
        if (index == -1) {
            return;
        }

        ClothingItem item = clothingItems.get(index);

        String size = readSize("Enter new size: ");
        String color = readColor("Enter new color: ");

        // The updated number of wears cannot exceed expected wears.
        int wears;

        while (true) {
            wears = readWholeNumber(
                    "Enter updated number of wears: ", 0
            );

            if (wears <= item.getExpectedWears()) {
                break;
            }

            System.out.println(
                    "Number of wears cannot exceed expected wears."
            );
        }

        // These changes update the selected clothing item.
        item.setSize(size);
        item.setColor(color);
        item.setNumberOfWears(wears);

        // Save the changes so they are still there next time.
        saveClothingItems();

        System.out.println("Clothing item updated successfully.");
    }

    // This removes the clothing item selected by the user.
    private void removeClothingItem() {
        displayClothingItems();

        if (clothingItems.isEmpty()) {
            return;
        }

        int index = readItemNumber();

        if (index == -1) {
            return;
        }

        clothingItems.remove(index);
        saveClothingItems();

        System.out.println("Clothing item removed successfully.");
    }

    // This calculates and displays how much of an item's expected wear life has been used.
    private void displayWearPercentage() {
        displayClothingItems();

        if (clothingItems.isEmpty()) {
            return;
        }

        int index = readItemNumber();

        if (index == -1) {
            return;
        }

        ClothingItem item = clothingItems.get(index);

        // The calculation is handled by the ClothingItem class.
        System.out.printf(
                "Wear percentage: %.2f%%%n",
                item.calculateWearPercentage()
        );
    }

    // This accepts whole numbers and keeps asking if the entry is invalid.
    private int readWholeNumber(String prompt, int minimum) {
        while (true) {
            System.out.print(prompt);

            try {
                int number = Integer.parseInt(
                        scanner.nextLine().trim()
                );

                if (number >= minimum) {
                    return number;
                }

                System.out.println(
                        "Enter a whole number of at least " + minimum + "."
                );

            } catch (NumberFormatException e) {
                System.out.println("Enter a valid whole number.");
            }
        }
    }

    // This checks that the selected item number exists in the list.
    private int readItemNumber() {
        while (true) {
            System.out.print(
                    "Enter item number (or 0 to cancel): "
            );

            try {
                int number = Integer.parseInt(
                        scanner.nextLine().trim()
                );

                // Entering 0 lets the user cancel the action.
                if (number == 0) {
                    return -1;
                }

                // The displayed numbers start at 1, but list positions start at 0.
                if (number >= 1 && number <= clothingItems.size()) {
                    return number - 1;
                }

                System.out.println(
                        "Invalid item number. Please try again."
                );

            } catch (NumberFormatException e) {
                System.out.println("Enter a valid whole number.");
            }
        }
    }

    // This loads saved clothing records from the text file when the program starts.
    private void loadClothingItems() {
        try {
            // Create the file if it does not exist yet.
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                return;
            }

            List<String> lines = Files.readAllLines(filePath);

            // Each line should contain six pieces of clothing information.
            for (String line : lines) {
                String[] fields = line.split("\\|", -1);

                if (fields.length != 6) {
                    System.out.println(
                            "Skipping an invalid clothing record."
                    );
                    continue;
                }

                try {
                    String category = fields[0].trim().toLowerCase();
                    String size = fields[1].trim().toUpperCase();
                    String color = fields[2].trim();
                    LocalDate purchaseDate =
                            LocalDate.parse(fields[3].trim());
                    int numberOfWears =
                            Integer.parseInt(fields[4].trim());
                    int expectedWears =
                            Integer.parseInt(fields[5].trim());

                    // Check that the saved information meets the program rules.
                    boolean validRecord =
                            validCategories.contains(category)
                                    && List.of(
                                    "XS", "S", "M", "L", "XL", "XXL"
                            ).contains(size)
                                    && validColors.contains(color.toLowerCase())
                                    && !purchaseDate.isAfter(LocalDate.now())
                                    && numberOfWears >= 0
                                    && expectedWears > 0
                                    && numberOfWears <= expectedWears;

                    if (!validRecord) {
                        System.out.println(
                                "Skipping an invalid clothing record."
                        );
                        continue;
                    }

                    // Turn the saved information back into a clothing item.
                    clothingItems.add(new ClothingItem(
                            category, size, color, purchaseDate,
                            numberOfWears, expectedWears
                    ));

                } catch (DateTimeParseException
                         | NumberFormatException e) {
                    // Skip records that have dates or numbers in the wrong format.
                    System.out.println(
                            "Skipping an invalid clothing record."
                    );
                }
            }

            System.out.println(
                    clothingItems.size() + " clothing item(s) loaded."
            );

        } catch (IOException e) {
            System.out.println(
                    "Could not load clothing items: " + e.getMessage()
            );
        }
    }

    // This saves all current clothing items back to the text file.
    private void saveClothingItems() {
        List<String> lines = new ArrayList<>();

        // Store each item's information on one line, separated by | symbols.
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

        try {
            Files.write(filePath, lines);

        } catch (IOException e) {
            System.out.println(
                    "Could not save clothing items: " + e.getMessage()
            );
        }
    }
}

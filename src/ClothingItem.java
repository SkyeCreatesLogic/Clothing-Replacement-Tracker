import java.time.LocalDate;

public class ClothingItem {
    private String category;
    private String size;
    private String color;
    private LocalDate purchaseDate;
    private int numberOfWears;
    private int expectedWears;

    public ClothingItem(String category, String size, String color,
                        LocalDate purchaseDate, int numberOfWears,
                        int expectedWears) {
        this.category = category;
        this.size = size;
        this.color = color;
        this.purchaseDate = purchaseDate;
        this.numberOfWears = numberOfWears;
        this.expectedWears = expectedWears;
    }

    public String getCategory() {
        return category;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public int getNumberOfWears() {
        return numberOfWears;
    }

    public int getExpectedWears() {
        return expectedWears;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public void setNumberOfWears(int numberOfWears) {
        this.numberOfWears = numberOfWears;
    }

    public void setExpectedWears(int expectedWears) {
        this.expectedWears = expectedWears;
    }

    public double calculateWearPercentage() {
        return (numberOfWears / (double) expectedWears) * 100;
    }

    @Override
    public String toString() {
        return category + " | " + size + " | " + color + " | "
                + purchaseDate + " | " + numberOfWears + " | "
                + expectedWears + " | "
                + String.format("%.2f%%", calculateWearPercentage());
    }
}
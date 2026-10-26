
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ClothingTrackerTest {

    @TempDir
    Path tempDir;

    private ClothingItem createItem(
            String size, String color, int wears, int expectedWears) {

        return new ClothingItem(
                "shirts",
                size,
                color,
                LocalDate.of(2026, 1, 1),
                wears,
                expectedWears
        );
    }

    @Test
    void addClothingItemSuccessfully() {
        ClothingTracker tracker =
                new ClothingTracker(tempDir.resolve("add.txt"));

        ClothingItem item = createItem("M", "blue", 5, 50);

        assertTrue(tracker.addClothingItem(item));
        assertEquals(1, tracker.getClothingItems().size());
        assertEquals("shirts",
                tracker.getClothingItems().get(0).getCategory());
    }

    @Test
    void rejectClothingItemWithInvalidExpectedWears() {
        ClothingTracker tracker =
                new ClothingTracker(tempDir.resolve("invalid.txt"));

        ClothingItem item = createItem("M", "blue", 5, 0);

        assertFalse(tracker.addClothingItem(item));
        assertEquals(0, tracker.getClothingItems().size());
    }

    @Test
    void calculateWearPercentageCorrectly() {
        ClothingTracker tracker =
                new ClothingTracker(tempDir.resolve("wear.txt"));

        ClothingItem item = createItem("M", "blue", 5, 50);

        assertEquals(10.0, tracker.calculateWearPercentage(item), 0.01);
    }

    @Test
    void rejectWearPercentageWithInvalidExpectedWears() {
        ClothingTracker tracker =
                new ClothingTracker(tempDir.resolve("invalid-wear.txt"));

        ClothingItem item = createItem("M", "blue", 5, 0);

        assertThrows(
                IllegalArgumentException.class,
                () -> tracker.calculateWearPercentage(item)
        );
    }

    @Test
    void removeClothingItemSuccessfully() {
        ClothingTracker tracker =
                new ClothingTracker(tempDir.resolve("remove.txt"));

        ClothingItem item = createItem("M", "blue", 5, 50);

        assertTrue(tracker.addClothingItem(item));
        assertTrue(tracker.removeClothingItem(item));
        assertEquals(0, tracker.getClothingItems().size());
    }

    @Test
    void removingItemThatDoesNotExistReturnsFalse() {
        ClothingTracker tracker =
                new ClothingTracker(tempDir.resolve("missing.txt"));

        ClothingItem item = createItem("M", "blue", 5, 50);

        assertFalse(tracker.removeClothingItem(item));
        assertEquals(0, tracker.getClothingItems().size());
    }

    @Test
    void updateClothingItemSuccessfully() {
        ClothingTracker tracker =
                new ClothingTracker(tempDir.resolve("update.txt"));

        ClothingItem original = createItem("M", "blue", 5, 50);
        ClothingItem updated = createItem("L", "black", 5, 100);

        assertTrue(tracker.addClothingItem(original));
        assertTrue(tracker.updateClothingItem(original, updated));

        assertEquals("L", original.getSize());
        assertEquals("black", original.getColor());
        assertEquals(100, original.getExpectedWears());
    }

    @Test
    void loadClothingItemsFromFileSuccessfully() throws Exception {
        Path inputFile = tempDir.resolve("sample.txt");

        Files.writeString(
                inputFile,
                "shirts|M|blue|2026-01-01|5|50\n"
        );

        ClothingTracker tracker =
                new ClothingTracker(tempDir.resolve("tracker.txt"));

        assertTrue(tracker.loadClothingItems(inputFile));
        assertEquals(1, tracker.getClothingItems().size());

        ClothingItem loaded = tracker.getClothingItems().get(0);

        assertEquals("shirts", loaded.getCategory());
        assertEquals("M", loaded.getSize());
        assertEquals("blue", loaded.getColor());
        assertEquals(5, loaded.getNumberOfWears());
        assertEquals(50, loaded.getExpectedWears());
    }
}

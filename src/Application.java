import javax.swing.SwingUtilities;

public class Application {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            ClothingReplacementTracker app =
                    new ClothingReplacementTracker();

            app.setVisible(true);
        });
    }
}
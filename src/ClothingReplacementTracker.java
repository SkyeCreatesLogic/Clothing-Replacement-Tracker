
/*
 * Student: Skye Tharpe
 * Course: CEN 3024C - Software Development I
 * Date: October 31, 2026
 * Class: ClothingReplacementTracker
 *
 * Purpose: This class makes the screen where users can see their
 * clothing, add new items, change or remove items, load a text file,
 * and check how much their clothing has been worn.
 */

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ClothingReplacementTracker extends JFrame {

    private final ClothingTracker tracker = new ClothingTracker();

    private final Color navy = new Color(24, 37, 56);
    private final Color blue = new Color(47, 99, 172);
    private final Color background = new Color(244, 247, 251);

    private final String[] colors = {
            "black", "white", "gray", "blue", "red",
            "green", "brown", "pink", "purple", "yellow",
            "orange", "nude", "beige", "khaki", "navy"
    };

    private final String[] categories = {
            "socks", "shirts", "pants", "coats",
            "underwear", "bras"
    };

    private final String[] sizes = {
            "XS", "S", "M", "L", "XL", "XXL", "One Size"
    };

    private final DefaultTableModel model =
            new DefaultTableModel(new String[]{
                    "Item #", "Category", "Size", "Color",
                    "Purchase Date", "Number of Wears",
                    "Expected Wears", "Wear %"
            }, 0) {

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int column) {
                    if (column == 0 || column == 5 || column == 6) {
                        return Integer.class;
                    }
                    if (column == 4) {
                        return LocalDate.class;
                    }
                    return String.class;
                }
            };

    private final JTable table = new JTable(model);

    private final TableRowSorter<DefaultTableModel> sorter =
            new TableRowSorter<>(model);

    private final List<ClothingItem> displayedItems =
            new ArrayList<>();

    private final JTextField fileField = new JTextField(36);
    private final JTextField searchField = new JTextField(18);

    private final JLabel itemsValue = new JLabel("0");
    private final JLabel categoriesValue = new JLabel("0");
    private final JLabel visibleValue = new JLabel("0");

    private final JLabel status =
            new JLabel("Ready to manage your clothing inventory");

    /*
     * Method: ClothingReplacementTracker
     * Purpose: Sets up the main window and loads the clothing file.
     * Takes in: Nothing.
     * Returns: Nothing.
     */
    public ClothingReplacementTracker() {

        super("Clothing Replacement Tracker");

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1280, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        getContentPane().setBackground(background);
        setLayout(new BorderLayout());

        add(header(), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);
        add(footer(), BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exitApplication();
            }
        });

        Path defaultFile = Path.of("src", "clothing.txt");

        fileField.setText(defaultFile.toAbsolutePath().toString());

        boolean loaded = tracker.loadClothingItems(defaultFile);
        refreshTable();

        if (!loaded) {
            status.setText(
                    "Choose a clothing text file and click Load Records"
            );
        }
    }

    /*
     * Method: header
     * Purpose: Shows the project name and inventory totals.
     * Takes in: Nothing.
     * Returns: The top panel.
     */
    private JPanel header() {

        JPanel outer = new JPanel(new BorderLayout(16, 14));
        outer.setBackground(navy);
        outer.setBorder(new EmptyBorder(24, 30, 24, 30));

        JLabel title = new JLabel(
                "CLOTHING REPLACEMENT TRACKER"
        );
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Your clothing inventory, organized in one place"
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(196, 211, 229));

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 4));
        heading.setOpaque(false);
        heading.add(title);
        heading.add(subtitle);

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);

        cards.add(metric("TOTAL ITEMS", itemsValue));
        cards.add(metric("CATEGORIES", categoriesValue));
        cards.add(metric("VISIBLE ROWS", visibleValue));

        outer.add(heading, BorderLayout.WEST);
        outer.add(cards, BorderLayout.EAST);

        return outer;
    }

    /*
     * Method: metric
     * Purpose: Makes a box that shows an inventory number.
     * Takes in: The box title and number.
     * Returns: The box panel.
     */
    private JPanel metric(String caption, JLabel value) {

        JPanel card = new JPanel(new BorderLayout(4, 5));
        card.setBackground(new Color(43, 60, 82));
        card.setBorder(new EmptyBorder(13, 20, 13, 20));

        JLabel name = new JLabel(caption);
        name.setFont(new Font("SansSerif", Font.BOLD, 10));
        name.setForeground(new Color(191, 208, 228));

        value.setFont(new Font("SansSerif", Font.BOLD, 27));
        value.setForeground(Color.WHITE);

        card.add(name, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);

        return card;
    }

    /*
     * Method: content
     * Purpose: Puts the file box, table, search, and buttons on screen.
     * Takes in: Nothing.
     * Returns: The main panel.
     */
    private JPanel content() {

        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(background);
        main.setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel importCard = new JPanel(new BorderLayout(0, 12));
        importCard.setBackground(Color.WHITE);
        importCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(224, 231, 239)
                ),
                new EmptyBorder(16, 20, 17, 20)
        ));

        JPanel importControls =
                new JPanel(new BorderLayout(10, 0));
        importControls.setOpaque(false);

        fileField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        fileField.setToolTipText(
                "Enter the complete path to a clothing text file"
        );

        JPanel importButtons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        importButtons.setOpaque(false);

        JButton browse = button(
                "Browse", new Color(92, 108, 129)
        );
        JButton load = button("Load Records", blue);

        browse.addActionListener(e -> browseFile());
        load.addActionListener(e -> loadFile());

        importButtons.add(browse);
        importButtons.add(load);

        importControls.add(fileField, BorderLayout.CENTER);
        importControls.add(importButtons, BorderLayout.EAST);

        importCard.add(
                heading("Import Clothing Records"),
                BorderLayout.NORTH
        );
        importCard.add(importControls, BorderLayout.CENTER);

        JPanel inventory = new JPanel(new BorderLayout(0, 12));
        inventory.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        top.add(heading("Clothing Inventory"), BorderLayout.WEST);

        JPanel search = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        search.setOpaque(false);
        search.add(new JLabel("Search records:"));
        search.add(searchField);

        top.add(search, BorderLayout.EAST);

        configureTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(
                BorderFactory.createLineBorder(
                        new Color(224, 231, 239)
                )
        );

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 4)
        );
        actions.setOpaque(false);

        JButton add = button(
                "+ Add Clothing", new Color(36, 128, 94)
        );
        JButton update = button("Update Selected", blue);
        JButton remove = button(
                "Remove Selected", new Color(181, 71, 75)
        );
        JButton wear = button(
                "Calculate Wear", new Color(114, 80, 163)
        );
        JButton exit = button(
                "Exit", new Color(93, 104, 118)
        );

        add.addActionListener(e -> addClothing());
        update.addActionListener(e -> updateClothing());
        remove.addActionListener(e -> removeClothing());
        wear.addActionListener(e -> calculateWear());
        exit.addActionListener(e -> exitApplication());

        for (JButton b : new JButton[]{
                add, update, remove, wear, exit
        }) {
            actions.add(b);
        }

        inventory.add(top, BorderLayout.NORTH);
        inventory.add(scroll, BorderLayout.CENTER);
        inventory.add(actions, BorderLayout.SOUTH);

        main.add(importCard, BorderLayout.NORTH);
        main.add(inventory, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {

                    @Override
                    public void insertUpdate(
                            javax.swing.event.DocumentEvent e) {
                        filterTable();
                    }

                    @Override
                    public void removeUpdate(
                            javax.swing.event.DocumentEvent e) {
                        filterTable();
                    }

                    @Override
                    public void changedUpdate(
                            javax.swing.event.DocumentEvent e) {
                        filterTable();
                    }
                }
        );

        return main;
    }

    /*
     * Method: configureTable
     * Purpose: Sets up the table colors, spacing, and date sorting.
     * Takes in: Nothing.
     * Returns: Nothing.
     */
    private void configureTable() {

        table.setRowHeight(38);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setForeground(new Color(35, 48, 65));
        table.setBackground(Color.WHITE);

        table.setGridColor(new Color(225, 233, 242));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        table.setSelectionBackground(
                new Color(198, 222, 250)
        );
        table.setSelectionForeground(navy);

        table.setAutoResizeMode(
                JTable.AUTO_RESIZE_ALL_COLUMNS
        );

        int[] widths = {
                65, 140, 85, 120, 155, 150, 145, 110
        };

        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        table.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );
        table.getTableHeader().setBackground(
                new Color(43, 75, 112)
        );
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(
                new Dimension(0, 44)
        );
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column) {

                        super.getTableCellRendererComponent(
                                table, value, isSelected,
                                hasFocus, row, column
                        );

                        setBorder(new EmptyBorder(0, 10, 0, 10));

                        if (column == 0 || column >= 5) {
                            setHorizontalAlignment(SwingConstants.CENTER);
                        } else {
                            setHorizontalAlignment(SwingConstants.LEFT);
                        }

                        if (isSelected) {
                            setBackground(new Color(198, 222, 250));
                            setForeground(navy);
                            return this;
                        }

                        setBackground(
                                row % 2 == 0
                                        ? Color.WHITE
                                        : new Color(240, 246, 253)
                        );
                        setForeground(new Color(35, 48, 65));

                        // Each clothing category gets a different color.
                        if (column == 1 && value != null) {
                            switch (value.toString().toLowerCase()) {
                                case "shirts":
                                    setBackground(new Color(220, 235, 255));
                                    break;
                                case "pants":
                                    setBackground(new Color(224, 243, 231));
                                    break;
                                case "socks":
                                    setBackground(new Color(255, 241, 216));
                                    break;
                                case "coats":
                                    setBackground(new Color(237, 225, 250));
                                    break;
                                case "underwear":
                                    setBackground(new Color(255, 229, 237));
                                    break;
                                case "bras":
                                    setBackground(new Color(226, 244, 244));
                                    break;
                            }
                        }

                        // Green means low wear, orange is medium,
                        // and red means the item is close to replacement.
                        if (column == 7 && value != null) {
                            try {
                                double percentage = Double.parseDouble(
                                        value.toString().replace("%", "")
                                );

                                if (percentage >= 80) {
                                    setBackground(new Color(255, 219, 219));
                                    setForeground(new Color(160, 35, 35));
                                } else if (percentage >= 50) {
                                    setBackground(new Color(255, 237, 203));
                                    setForeground(new Color(145, 85, 20));
                                } else {
                                    setBackground(new Color(218, 243, 227));
                                    setForeground(new Color(30, 115, 70));
                                }
                            } catch (NumberFormatException ignored) {
                                // Keep the normal color.
                            }
                        }

                        return this;
                    }
                };

        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(Integer.class, renderer);
        table.setDefaultRenderer(LocalDate.class, renderer);
        table.setDefaultRenderer(String.class, renderer);

        table.setRowSorter(sorter);

        sorter.setSortKeys(java.util.Collections.singletonList(
                new RowSorter.SortKey(
                        4, SortOrder.DESCENDING
                )
        ));
    }

    /*
     * Method: heading
     * Purpose: Makes the titles for each part of the screen.
     * Takes in: The title text.
     * Returns: The title label.
     */
    private JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(navy);
        return label;
    }

    /*
     * Method: button
     * Purpose: Gives the buttons the same style.
     * Takes in: The button name and color.
     * Returns: The button.
     */
    private JButton button(String text, Color color) {

        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(color);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(12, 16, 12, 16));
        b.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        return b;
    }

    /*
     * Method: footer
     * Purpose: Shows messages at the bottom of the program.
     * Takes in: Nothing.
     * Returns: The bottom panel.
     */
    private JPanel footer() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(navy);
        panel.setBorder(new EmptyBorder(11, 28, 11, 28));

        status.setForeground(Color.WHITE);
        panel.add(status, BorderLayout.WEST);

        return panel;
    }

    /*
     * Method: refreshTable
     * Purpose: Updates the list and puts the newest purchases first.
     * Takes in: Nothing.
     * Returns: Nothing.
     */
    private void refreshTable() {

        model.setRowCount(0);
        displayedItems.clear();
        displayedItems.addAll(tracker.getClothingItems());

        displayedItems.sort(
                (first, second) -> second.getPurchaseDate()
                        .compareTo(first.getPurchaseDate())
        );

        int number = 1;
        Set<String> usedCategories = new HashSet<>();

        for (ClothingItem item : displayedItems) {

            usedCategories.add(
                    item.getCategory().toLowerCase()
            );

            model.addRow(new Object[]{
                    number++,
                    item.getCategory(),
                    item.getSize(),
                    item.getColor(),
                    item.getPurchaseDate(),
                    item.getNumberOfWears(),
                    item.getExpectedWears(),
                    String.format(
                            "%.2f%%",
                            tracker.calculateWearPercentage(item)
                    )
            });
        }

        itemsValue.setText(
                String.valueOf(tracker.getClothingItems().size())
        );
        categoriesValue.setText(
                String.valueOf(usedCategories.size())
        );
        visibleValue.setText(
                String.valueOf(table.getRowCount())
        );

        status.setText(
                "Inventory ready | " +
                        tracker.getClothingItems().size() + " total items"
        );
    }

    /*
     * Method: filterTable
     * Purpose: Shows items that match what the user searches for.
     * Takes in: Nothing.
     * Returns: Nothing.
     */
    private void filterTable() {

        String query = searchField.getText().trim();

        sorter.setRowFilter(
                query.isEmpty()
                        ? null
                        : RowFilter.regexFilter(
                        "(?i)" + Pattern.quote(query)
                )
        );

        visibleValue.setText(
                String.valueOf(table.getRowCount())
        );
    }

    /*
     * Method: browseFile
     * Purpose: Lets the user choose a clothing file.
     * Takes in: Nothing.
     * Returns: Nothing.
     */
    private void browseFile() {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a Clothing Text File");

        if (chooser.showOpenDialog(this)
                == JFileChooser.APPROVE_OPTION) {
            fileField.setText(
                    chooser.getSelectedFile().getAbsolutePath()
            );
        }
    }

    /*
     * Method: loadFile
     * Purpose: Loads the clothing records from the chosen file.
     * Takes in: The file location from the text box.
     * Returns: Nothing.
     */
    private void loadFile() {

        String input = fileField.getText().trim();

        if (input.isEmpty()) {
            error("Enter a file location or click Browse.");
            return;
        }

        try {
            if (tracker.loadClothingItems(Path.of(input))) {

                searchField.setText("");
                refreshTable();

                status.setText("Loaded file: " + input);

                JOptionPane.showMessageDialog(
                        this,
                        "Loaded " + tracker.getClothingItems().size() +
                                " clothing records."
                );

            } else {
                error(
                        "The file could not be loaded. " +
                                "Check the location and file format."
                );
            }
        } catch (InvalidPathException e) {
            error("The file location is invalid.");
        }
    }

    /*
     * Method: makeColorMenu
     * Purpose: Shows the colors the user can choose from.
     * Takes in: The color already saved for the item.
     * Returns: A color drop-down menu.
     */
    private JComboBox<String> makeColorMenu(String currentColor) {

        JComboBox<String> menu = new JComboBox<>(colors);

        menu.setFont(new Font("SansSerif", Font.PLAIN, 13));

        if (currentColor != null) {
            String chosen = currentColor.equalsIgnoreCase("grey")
                    ? "gray" : currentColor.toLowerCase();

            menu.setSelectedItem(chosen);
        }

        return menu;
    }

    /*
     * Method: makeCategoryMenu
     * Purpose: Shows the clothing categories the program accepts.
     * Takes in: The category already saved for the item.
     * Returns: A category drop-down menu.
     */
    private JComboBox<String> makeCategoryMenu(String currentCategory) {

        JComboBox<String> menu = new JComboBox<>(categories);

        if (currentCategory != null) {
            menu.setSelectedItem(currentCategory.toLowerCase());
        }

        return menu;
    }

    /*
     * Method: makeSizeMenu
     * Purpose: Shows the clothing sizes the program accepts.
     * Takes in: The size already saved for the item.
     * Returns: A size drop-down menu.
     */
    private JComboBox<String> makeSizeMenu(String currentSize) {

        JComboBox<String> menu = new JComboBox<>(sizes);

        if (currentSize != null) {
            for (String size : sizes) {
                if (size.equalsIgnoreCase(currentSize)) {
                    menu.setSelectedItem(size);
                    break;
                }
            }
        }

        return menu;
    }

    /*
     * Method: editForm
     * Purpose: Opens the form used to add a new clothing item.
     * Takes in: Nothing.
     * Returns: A new clothing item or null if canceled.
     */
    private ClothingItem editForm() {

        JComboBox<String> category = makeCategoryMenu("shirts");
        JComboBox<String> size = makeSizeMenu("M");
        JComboBox<String> color = makeColorMenu("blue");

        JTextField date = new JTextField(
                LocalDate.now().toString()
        );
        JTextField wears = new JTextField("0");
        JTextField expected = new JTextField("50");

        JPanel fields = new JPanel(new GridLayout(0, 2, 12, 12));
        fields.setBorder(new EmptyBorder(12, 12, 12, 12));

        fields.add(new JLabel("Category"));
        fields.add(category);

        fields.add(new JLabel("Size"));
        fields.add(size);

        fields.add(new JLabel("Color"));
        fields.add(color);

        fields.add(new JLabel("Purchase Date (YYYY-MM-DD)"));
        fields.add(date);

        fields.add(new JLabel("Number of Wears"));
        fields.add(wears);

        fields.add(new JLabel("Expected Wears"));
        fields.add(expected);

        while (true) {

            int choice = JOptionPane.showConfirmDialog(
                    this, fields, "Add Clothing",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (choice != JOptionPane.OK_OPTION) {
                return null;
            }

            try {
                LocalDate purchaseDate =
                        LocalDate.parse(date.getText().trim());

                int numberOfWears =
                        Integer.parseInt(wears.getText().trim());

                int expectedWears =
                        Integer.parseInt(expected.getText().trim());

                if (expectedWears <= 0 ||
                        numberOfWears < 0 ||
                        numberOfWears > expectedWears ||
                        purchaseDate.isAfter(LocalDate.now())) {

                    error(
                            "Check the date and wear counts. " +
                                    "The date cannot be in the future, and " +
                                    "the number of wears cannot be more " +
                                    "than the expected wears."
                    );
                    continue;
                }

                return new ClothingItem(
                        (String) category.getSelectedItem(),
                        (String) size.getSelectedItem(),
                        (String) color.getSelectedItem(),
                        purchaseDate,
                        numberOfWears,
                        expectedWears
                );

            } catch (DateTimeParseException e) {
                error("Enter the date using YYYY-MM-DD.");
            } catch (NumberFormatException e) {
                error("Both wear counts must be whole numbers.");
            }
        }
    }

    /*
     * Method: addClothing
     * Purpose: Adds a clothing item and updates the table.
     * Takes in: The information entered in the form.
     * Returns: Nothing.
     */
    private void addClothing() {

        while (true) {

            ClothingItem item = editForm();

            if (item == null) {
                return;
            }

            if (tracker.addClothingItem(item)) {
                refreshTable();

                JOptionPane.showMessageDialog(
                        this, "Clothing item added successfully."
                );
                return;
            }

            error("The clothing item could not be added.");
        }
    }

    /*
     * Method: selectedItem
     * Purpose: Finds the correct item after searching or sorting.
     * Takes in: The row the user selected.
     * Returns: The clothing item or null if none was selected.
     */
    private ClothingItem selectedItem() {

        int row = table.getSelectedRow();

        if (row < 0) {
            error("Select a clothing item from the table first.");
            return null;
        }

        int modelRow = table.convertRowIndexToModel(row);

        return displayedItems.get(modelRow);
    }

    /*
     * Method: updateClothing
     * Purpose: Lets the user choose one part of an item to change.
     * Takes in: The selected clothing item.
     * Returns: Nothing.
     */
    private void updateClothing() {

        ClothingItem original = selectedItem();

        if (original == null) {
            return;
        }

        String[] options = {
                "Category",
                "Size",
                "Color",
                "Purchase Date",
                "Number of Wears",
                "Expected Wears",
                "Return"
        };

        while (true) {

            String selectedField = (String)
                    JOptionPane.showInputDialog(
                            this,
                            "Which part of this clothing item " +
                                    "would you like to change?",
                            "Update Clothing",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]
                    );

            if (selectedField == null ||
                    selectedField.equals("Return")) {
                return;
            }

            // Make a copy so the original is not changed
            // unless the new information passes the checks.
            ClothingItem updated = new ClothingItem(
                    original.getCategory(),
                    original.getSize(),
                    original.getColor(),
                    original.getPurchaseDate(),
                    original.getNumberOfWears(),
                    original.getExpectedWears()
            );

            boolean changed = false;

            switch (selectedField) {

                case "Category": {

                    JComboBox<String> menu =
                            makeCategoryMenu(original.getCategory());

                    int choice = JOptionPane.showConfirmDialog(
                            this, menu, "Change Category",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE
                    );

                    if (choice == JOptionPane.OK_OPTION) {
                        updated.setCategory(
                                (String) menu.getSelectedItem()
                        );
                        changed = true;
                    }
                    break;
                }

                case "Size": {

                    JComboBox<String> menu =
                            makeSizeMenu(original.getSize());

                    int choice = JOptionPane.showConfirmDialog(
                            this, menu, "Change Size",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE
                    );

                    if (choice == JOptionPane.OK_OPTION) {
                        updated.setSize(
                                (String) menu.getSelectedItem()
                        );
                        changed = true;
                    }
                    break;
                }

                case "Color": {

                    JComboBox<String> menu =
                            makeColorMenu(original.getColor());

                    int choice = JOptionPane.showConfirmDialog(
                            this,
                            new Object[]{
                                    "Choose the closest available color:",
                                    menu
                            },
                            "Change Color",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE
                    );

                    if (choice == JOptionPane.OK_OPTION) {
                        updated.setColor(
                                (String) menu.getSelectedItem()
                        );
                        changed = true;
                    }
                    break;
                }

                case "Purchase Date": {

                    while (true) {

                        String input = JOptionPane.showInputDialog(
                                this,
                                "Enter the purchase date (YYYY-MM-DD):",
                                original.getPurchaseDate().toString()
                        );

                        if (input == null) {
                            break;
                        }

                        try {
                            LocalDate newDate =
                                    LocalDate.parse(input.trim());

                            if (newDate.isAfter(LocalDate.now())) {
                                error(
                                        "The purchase date cannot " +
                                                "be in the future."
                                );
                                continue;
                            }

                            updated.setPurchaseDate(newDate);
                            changed = true;
                            break;

                        } catch (DateTimeParseException e) {
                            error("Enter the date using YYYY-MM-DD.");
                        }
                    }
                    break;
                }

                case "Number of Wears": {

                    while (true) {

                        String input = JOptionPane.showInputDialog(
                                this,
                                "Enter the number of wears:",
                                String.valueOf(
                                        original.getNumberOfWears()
                                )
                        );

                        if (input == null) {
                            break;
                        }

                        try {
                            int newWears =
                                    Integer.parseInt(input.trim());

                            if (newWears < 0 ||
                                    newWears >
                                            original.getExpectedWears()) {

                                error(
                                        "Enter a number from 0 to " +
                                                original.getExpectedWears() + "."
                                );
                                continue;
                            }

                            updated.setNumberOfWears(newWears);
                            changed = true;
                            break;

                        } catch (NumberFormatException e) {
                            error("Enter a whole number.");
                        }
                    }
                    break;
                }

                case "Expected Wears": {

                    while (true) {

                        String input = JOptionPane.showInputDialog(
                                this,
                                "Enter the expected number of wears:",
                                String.valueOf(
                                        original.getExpectedWears()
                                )
                        );

                        if (input == null) {
                            break;
                        }

                        try {
                            int newExpected =
                                    Integer.parseInt(input.trim());

                            if (newExpected <= 0 ||
                                    newExpected <
                                            original.getNumberOfWears()) {

                                error(
                                        "Expected wears must be at least " +
                                                original.getNumberOfWears() +
                                                " and greater than zero."
                                );
                                continue;
                            }

                            updated.setExpectedWears(newExpected);
                            changed = true;
                            break;

                        } catch (NumberFormatException e) {
                            error("Enter a whole number.");
                        }
                    }
                    break;
                }
            }

            if (!changed) {
                continue;
            }

            if (tracker.updateClothingItem(original, updated)) {

                refreshTable();

                // Keep the updated item selected after the table refreshes.
                int modelRow = displayedItems.indexOf(updated);

                if (modelRow >= 0) {
                    int viewRow =
                            table.convertRowIndexToView(modelRow);

                    if (viewRow >= 0) {
                        table.setRowSelectionInterval(viewRow, viewRow);
                    }
                }

                JOptionPane.showMessageDialog(
                        this,
                        selectedField + " updated successfully."
                );

                return;

            } else {
                error(
                        "The change could not be saved. " +
                                "Please check the information."
                );
            }
        }
    }

    /*
     * Method: removeClothing
     * Purpose: Removes an item after the user confirms it.
     * Takes in: The selected clothing item.
     * Returns: Nothing.
     */
    private void removeClothing() {

        ClothingItem item = selectedItem();

        if (item == null) {
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Remove the selected clothing item?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation == JOptionPane.YES_OPTION) {

            if (tracker.removeClothingItem(item)) {

                refreshTable();

                JOptionPane.showMessageDialog(
                        this, "Clothing item removed."
                );

            } else {
                error("The item could not be removed.");
            }
        }
    }

    /*
     * Method: calculateWear
     * Purpose: Shows how much the selected clothing item has been worn.
     * Takes in: The selected clothing item.
     * Returns: Nothing.
     */
    private void calculateWear() {

        ClothingItem item = selectedItem();

        if (item == null) {
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Category: " + item.getCategory() +
                        "\nColor: " + item.getColor() +
                        "\nWears: " + item.getNumberOfWears() +
                        " / " + item.getExpectedWears() +
                        "\nWear Percentage: " +
                        String.format(
                                "%.2f%%",
                                tracker.calculateWearPercentage(item)
                        ),
                "Wear Calculation",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /*
     * Method: error
     * Purpose: Shows an error without closing the program.
     * Takes in: The message explaining the problem.
     * Returns: Nothing.
     */
    private void error(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Please Check Your Input",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /*
     * Method: exitApplication
     * Purpose: Asks before closing the program.
     * Takes in: Nothing.
     * Returns: Nothing.
     */
    private void exitApplication() {

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Exit Clothing Replacement Tracker?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation == JOptionPane.YES_OPTION) {
            dispose();
        }
    }
}

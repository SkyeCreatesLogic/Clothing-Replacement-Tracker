
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ClothingReplacementTracker extends JFrame {

    // Clothing data is now stored in SQLite.
    private String databasePath = Path.of("clothing.db")
            .toAbsolutePath().toString();

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

    private final JTextField databaseField = new JTextField(36);
    private final JTextField searchField = new JTextField(18);

    private final JLabel itemsValue = new JLabel("0");
    private final JLabel categoriesValue = new JLabel("0");
    private final JLabel visibleValue = new JLabel("0");

    private final JLabel status =
            new JLabel("Ready to manage your clothing inventory");

    // Sets up the main GUI.
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

        databaseField.setText(databasePath);
        loadDatabase();
    }

    // Creates the top section.
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

    // Creates a number card.
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

    // Creates the database controls, table, and buttons.
    private JPanel content() {

        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(background);
        main.setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel databaseCard = new JPanel(new BorderLayout(0, 12));
        databaseCard.setBackground(Color.WHITE);
        databaseCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(224, 231, 239)
                ),
                new EmptyBorder(16, 20, 17, 20)
        ));

        JPanel controls = new JPanel(new BorderLayout(10, 0));
        controls.setOpaque(false);

        databaseField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        databaseField.setToolTipText(
                "Enter the location of your SQLite database file"
        );

        JPanel databaseButtons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        databaseButtons.setOpaque(false);

        JButton browse = button(
                "Browse", new Color(92, 108, 129)
        );
        JButton load = button("Load Database", blue);
        JButton refresh = button(
                "Refresh", new Color(36, 128, 94)
        );

        browse.addActionListener(e -> browseDatabase());
        load.addActionListener(e -> loadDatabase());
        refresh.addActionListener(e -> refreshTable());

        databaseButtons.add(browse);
        databaseButtons.add(load);
        databaseButtons.add(refresh);

        controls.add(databaseField, BorderLayout.CENTER);
        controls.add(databaseButtons, BorderLayout.EAST);

        databaseCard.add(
                heading("SQLite Database Location"),
                BorderLayout.NORTH
        );
        databaseCard.add(controls, BorderLayout.CENTER);

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

        main.add(databaseCard, BorderLayout.NORTH);
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

    // Keeps the original table design and category colors.
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
                            setHorizontalAlignment(
                                    SwingConstants.CENTER
                            );
                        } else {
                            setHorizontalAlignment(
                                    SwingConstants.LEFT
                            );
                        }

                        if (isSelected) {
                            setBackground(
                                    new Color(198, 222, 250)
                            );
                            setForeground(navy);
                            return this;
                        }

                        setBackground(
                                row % 2 == 0
                                        ? Color.WHITE
                                        : new Color(240, 246, 253)
                        );
                        setForeground(new Color(35, 48, 65));

                        if (column == 1 && value != null) {
                            switch (value.toString().toLowerCase()) {
                                case "shirts":
                                    setBackground(
                                            new Color(220, 235, 255)
                                    );
                                    break;
                                case "pants":
                                    setBackground(
                                            new Color(224, 243, 231)
                                    );
                                    break;
                                case "socks":
                                    setBackground(
                                            new Color(255, 241, 216)
                                    );
                                    break;
                                case "coats":
                                    setBackground(
                                            new Color(237, 225, 250)
                                    );
                                    break;
                                case "underwear":
                                    setBackground(
                                            new Color(255, 229, 237)
                                    );
                                    break;
                                case "bras":
                                    setBackground(
                                            new Color(226, 244, 244)
                                    );
                                    break;
                            }
                        }

                        if (column == 7 && value != null) {
                            try {
                                double percentage = Double.parseDouble(
                                        value.toString().replace("%", "")
                                );

                                if (percentage >= 80) {
                                    setBackground(
                                            new Color(255, 219, 219)
                                    );
                                    setForeground(
                                            new Color(160, 35, 35)
                                    );
                                } else if (percentage >= 50) {
                                    setBackground(
                                            new Color(255, 237, 203)
                                    );
                                    setForeground(
                                            new Color(145, 85, 20)
                                    );
                                } else {
                                    setBackground(
                                            new Color(218, 243, 227)
                                    );
                                    setForeground(
                                            new Color(30, 115, 70)
                                    );
                                }

                            } catch (NumberFormatException ignored) {
                                // Keep normal color.
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

        sorter.setSortKeys(
                java.util.Collections.singletonList(
                        new RowSorter.SortKey(
                                4, SortOrder.DESCENDING
                        )
                )
        );
    }

    private JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(navy);
        return label;
    }

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

    private JPanel footer() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(navy);
        panel.setBorder(new EmptyBorder(11, 28, 11, 28));

        status.setForeground(Color.WHITE);
        panel.add(status, BorderLayout.WEST);

        return panel;
    }

    // Opens the database file chooser.
    private void browseDatabase() {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose SQLite Database");

        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter(
                        "SQLite Database (*.db)", "db"
                )
        );

        if (chooser.showOpenDialog(this)
                == JFileChooser.APPROVE_OPTION) {
            databaseField.setText(
                    chooser.getSelectedFile().getAbsolutePath()
            );
        }
    }

    // Connects to the database selected by the user.
    private void loadDatabase() {

        String input = databaseField.getText().trim();

        if (input.isEmpty()) {
            error("Enter a database location or click Browse.");
            return;
        }

        try {
            Path chosen = Path.of(input).toAbsolutePath();

            if (!java.nio.file.Files.isRegularFile(chosen)) {
                error("Database file not found. Choose an existing .db file.");
                return;
            }

            try (Connection connection =
                         ClothingDatabase.connect(chosen.toString())) {

                ClothingDatabase.createTable(connection);
            }

            databasePath = chosen.toString();
            searchField.setText("");
            refreshTable();

        } catch (java.nio.file.InvalidPathException e) {
            error("The database location is invalid.");
        } catch (SQLException e) {
            error("Cannot open database: " + e.getMessage());
        }
    }

    // Reads the current clothing records from SQLite.
    private void refreshTable() {

        String sql = """
                SELECT id, category, size, color,
                       purchase_date, number_of_wears, expected_wears
                FROM clothing
                ORDER BY purchase_date DESC, id DESC
                """;

        try (Connection connection =
                     ClothingDatabase.connect(databasePath);
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            java.util.List<Object[]> rows =
                    new java.util.ArrayList<>();

            Set<String> usedCategories = new HashSet<>();

            while (results.next()) {

                int id = results.getInt("id");
                String category = results.getString("category");
                String size = results.getString("size");
                String color = results.getString("color");

                LocalDate date = LocalDate.parse(
                        results.getString("purchase_date")
                );

                int wears = results.getInt("number_of_wears");
                int expected = results.getInt("expected_wears");

                double percentage = expected > 0
                        ? (wears * 100.0) / expected
                        : 0;

                usedCategories.add(category.toLowerCase());

                rows.add(new Object[]{
                        id,
                        category,
                        size,
                        color,
                        date,
                        wears,
                        expected,
                        String.format("%.2f%%", percentage)
                });
            }

            model.setRowCount(0);

            for (Object[] row : rows) {
                model.addRow(row);
            }

            itemsValue.setText(String.valueOf(rows.size()));
            categoriesValue.setText(
                    String.valueOf(usedCategories.size())
            );

            filterTable();

            status.setText(
                    "SQLite connected | " + rows.size() +
                            " total items"
            );

        } catch (SQLException | RuntimeException e) {
            error("Could not load clothing records: " + e.getMessage());
        }
    }

    // Filters the table without changing the database.
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

    // Creates the color menu.
    private JComboBox<String> makeColorMenu(String currentColor) {

        JComboBox<String> menu = new JComboBox<>(colors);

        if (currentColor != null) {
            String chosen = currentColor.equalsIgnoreCase("grey")
                    ? "gray"
                    : currentColor.toLowerCase();

            menu.setSelectedItem(chosen);
        }

        return menu;
    }

    // Creates the category menu.
    private JComboBox<String> makeCategoryMenu(
            String currentCategory) {

        JComboBox<String> menu = new JComboBox<>(categories);

        if (currentCategory != null) {
            menu.setSelectedItem(currentCategory.toLowerCase());
        }

        return menu;
    }

    // Creates the size menu.
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

    // Opens the Add or Update form.
    // Users can change multiple fields at once.
    private ClothingItem editForm(
            ClothingItem original, String title) {

        JComboBox<String> category = makeCategoryMenu(
                original == null ? "shirts" : original.getCategory()
        );

        JComboBox<String> size = makeSizeMenu(
                original == null ? "M" : original.getSize()
        );

        JComboBox<String> color = makeColorMenu(
                original == null ? "blue" : original.getColor()
        );

        JTextField date = new JTextField(
                original == null
                        ? LocalDate.now().toString()
                        : original.getPurchaseDate().toString()
        );

        JTextField wears = new JTextField(
                original == null
                        ? "0"
                        : String.valueOf(original.getNumberOfWears())
        );

        JTextField expected = new JTextField(
                original == null
                        ? "50"
                        : String.valueOf(original.getExpectedWears())
        );

        JPanel fields = new JPanel(
                new GridLayout(0, 2, 12, 12)
        );
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
                    this,
                    fields,
                    title,
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

                if (purchaseDate.isAfter(LocalDate.now())) {
                    error("Purchase date cannot be in the future.");
                    continue;
                }

                if (numberOfWears < 0 || expectedWears <= 0) {
                    error(
                            "Number of wears cannot be negative. " +
                                    "Expected wears must be greater than zero."
                    );
                    continue;
                }

                if (numberOfWears > expectedWears) {
                    error(
                            "Number of wears cannot be greater " +
                                    "than expected wears."
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

    // Adds a clothing item to SQLite.
    private void addClothing() {

        ClothingItem item = editForm(null, "Add Clothing");

        if (item == null) {
            return;
        }

        String sql = """
                INSERT INTO clothing
                (category, size, color, purchase_date,
                 number_of_wears, expected_wears)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection =
                     ClothingDatabase.connect(databasePath);
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            fillStatement(statement, item);
            statement.executeUpdate();

            refreshTable();

            JOptionPane.showMessageDialog(
                    this,
                    "Clothing item added successfully."
            );

        } catch (SQLException e) {
            error("Could not add clothing: " + e.getMessage());
        }
    }

    // Gets the selected record's permanent SQLite ID.
    private Integer selectedId() {

        int viewRow = table.getSelectedRow();

        if (viewRow < 0) {
            error("Select a clothing item from the table first.");
            return null;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);

        return (Integer) model.getValueAt(modelRow, 0);
    }

    // Gets the selected clothing information from the table.
    private ClothingItem selectedItem() {

        int viewRow = table.getSelectedRow();

        if (viewRow < 0) {
            error("Select a clothing item from the table first.");
            return null;
        }

        int row = table.convertRowIndexToModel(viewRow);

        return new ClothingItem(
                (String) model.getValueAt(row, 1),
                (String) model.getValueAt(row, 2),
                (String) model.getValueAt(row, 3),
                (LocalDate) model.getValueAt(row, 4),
                (Integer) model.getValueAt(row, 5),
                (Integer) model.getValueAt(row, 6)
        );
    }

    // Updates the selected SQLite record.
    private void updateClothing() {

        Integer id = selectedId();

        if (id == null) {
            return;
        }

        ClothingItem original = selectedItem();

        ClothingItem updated = editForm(
                original, "Update Clothing"
        );

        if (updated == null) {
            return;
        }

        String sql = """
                UPDATE clothing
                SET category = ?,
                    size = ?,
                    color = ?,
                    purchase_date = ?,
                    number_of_wears = ?,
                    expected_wears = ?
                WHERE id = ?
                """;

        try (Connection connection =
                     ClothingDatabase.connect(databasePath);
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            fillStatement(statement, updated);
            statement.setInt(7, id);

            int changed = statement.executeUpdate();

            if (changed > 0) {
                refreshTable();
                selectId(id);

                JOptionPane.showMessageDialog(
                        this,
                        "Clothing item updated successfully."
                );
            } else {
                error("The clothing item was not found.");
            }

        } catch (SQLException e) {
            error("Could not update clothing: " + e.getMessage());
        }
    }

    // Keeps the updated record selected.
    private void selectId(int id) {

        for (int row = 0; row < model.getRowCount(); row++) {

            if (((Integer) model.getValueAt(row, 0)) == id) {

                int viewRow = table.convertRowIndexToView(row);

                if (viewRow >= 0) {
                    table.setRowSelectionInterval(
                            viewRow, viewRow
                    );
                }

                return;
            }
        }
    }

    // Removes a record from SQLite after confirmation.
    private void removeClothing() {

        Integer id = selectedId();

        if (id == null) {
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Remove the selected clothing item?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM clothing WHERE id = ?";

        try (Connection connection =
                     ClothingDatabase.connect(databasePath);
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            int removed = statement.executeUpdate();

            if (removed > 0) {
                refreshTable();

                JOptionPane.showMessageDialog(
                        this,
                        "Clothing item removed successfully."
                );
            } else {
                error("The clothing item was not found.");
            }

        } catch (SQLException e) {
            error("Could not remove clothing: " + e.getMessage());
        }
    }

    // Calculates wear percentage for the selected item.
    private void calculateWear() {

        ClothingItem item = selectedItem();

        if (item == null) {
            return;
        }

        double percentage =
                item.getExpectedWears() > 0
                        ? item.getNumberOfWears() * 100.0
                        / item.getExpectedWears()
                        : 0;

        JOptionPane.showMessageDialog(
                this,
                "Category: " + item.getCategory() +
                        "\nColor: " + item.getColor() +
                        "\nWears: " + item.getNumberOfWears() +
                        " / " + item.getExpectedWears() +
                        "\nWear Percentage: " +
                        String.format("%.2f%%", percentage),
                "Wear Calculation",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // Places clothing values into a prepared SQL statement.
    private void fillStatement(
            PreparedStatement statement,
            ClothingItem item) throws SQLException {

        statement.setString(1, item.getCategory());
        statement.setString(2, item.getSize());
        statement.setString(3, item.getColor());
        statement.setString(
                4, item.getPurchaseDate().toString()
        );
        statement.setInt(5, item.getNumberOfWears());
        statement.setInt(6, item.getExpectedWears());
    }

    // Displays an error without closing the program.
    private void error(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Please Check Your Input",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // Exits only after the user confirms.
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

    // Starts the GUI.
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new ClothingReplacementTracker().setVisible(true);
        });
    }
}

package ratingseller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class RatingSeller extends JFrame {
    private Connection connection;
    private Statement statement;
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> sellerComboBox;
    private JTextField ratingTextField;

    public RatingSeller() {
        initializeDatabase();
        initComponents();
        loadData();
    }

    private void initializeDatabase() {
    try {
        String url = "jdbc:mysql://localhost:3306/ratingseller";
        String username = "root";
        String password = "";
        connection = DriverManager.getConnection(url, username, password);
        statement = connection.createStatement();

        // Create table if not exists
        String createTableQuery = "CREATE TABLE IF NOT EXISTS seller_rating ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "customer_name VARCHAR(100) NOT NULL,"
                + "seller_name VARCHAR(100) NOT NULL,"  // Added seller_name column
                + "rating INT NOT NULL,"
                + "created_at DATETIME NOT NULL)";
        statement.executeUpdate(createTableQuery);

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Failed to connect to the database.");
        System.exit(0);
    }
}

    private void initComponents() {
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel titleLabel = new JLabel("Seller Rating");
        titleLabel.setBounds(200, 10, 200, 30);
        add(titleLabel);

        JLabel sellerLabel = new JLabel("Select Seller:");
        sellerLabel.setBounds(30, 60, 100, 20);
        add(sellerLabel);

        String[] sellers = {"Toko Mainan", "Toko Makanan"};
        sellerComboBox = new JComboBox<>(sellers);
        sellerComboBox.setBounds(140, 60, 150, 20);
        add(sellerComboBox);

        JLabel ratingLabel = new JLabel("Enter Rating (1-5):");
        ratingLabel.setBounds(30, 100, 120, 20);
        add(ratingLabel);

        ratingTextField = new JTextField();
        ratingTextField.setBounds(160, 100, 50, 20);
        add(ratingTextField);

        JButton saveButton = new JButton("Save Rating");
        saveButton.setBounds(230, 100, 120, 20);
        add(saveButton);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveRating();
                loadData();
            }
        });

        model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Customer Name");
        model.addColumn("Rating");
        model.addColumn("Created At");

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(30, 140, 540, 200);
        add(scrollPane);

        JButton filterButton = new JButton("Filter Seller");
        filterButton.setBounds(400, 60, 120, 20);
        add(filterButton);
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadData();
            }
        });
    }

    private void saveRating() {
    try {
        String seller = (String) sellerComboBox.getSelectedItem();
        int rating = Integer.parseInt(ratingTextField.getText());

        if (rating < 1 || rating > 5) {
            JOptionPane.showMessageDialog(null, "Rating dari 1-5.");
            return;
        }

        String customerName = JOptionPane.showInputDialog("Masukkan nama Anda:");
        if (customerName == null || customerName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nama tidak boleh kosong.");
            return;
        }

        String insertQuery = "INSERT INTO seller_rating (customer_name, seller_name, rating, created_at) VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.setString(1, customerName);
        preparedStatement.setString(2, seller); // Use seller name from combo box
        preparedStatement.setInt(3, rating);
        preparedStatement.setString(4, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        preparedStatement.executeUpdate();

        JOptionPane.showMessageDialog(null, "Rating berhasil disimpan.", "Success", JOptionPane.INFORMATION_MESSAGE);

    } catch (SQLException | NumberFormatException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Gagal menyimpan.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void loadData() {
    try {
        model.setRowCount(0); // Clear table

        String seller = (String) sellerComboBox.getSelectedItem();
        String filterQuery = "SELECT * FROM seller_rating WHERE seller_name = '" + seller + "' ORDER BY created_at DESC";
        ResultSet resultSet = statement.executeQuery(filterQuery);

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String customerName = resultSet.getString("customer_name");
            int rating = resultSet.getInt("rating");
            String createdAt = resultSet.getString("created_at");

            model.addRow(new Object[]{id, customerName, rating, createdAt});
        }

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Gagal memuat data.");
    }
}

    public static void main(String[] args) {
        RatingSeller app = new RatingSeller();
        app.setVisible(true);
    }
}


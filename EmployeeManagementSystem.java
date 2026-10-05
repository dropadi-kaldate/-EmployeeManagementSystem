import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeManagementSystem extends JFrame {

    private final List<Employee> employees = new ArrayList<>();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;
    private final File dataFile = new File("employees.dat");

    private final JTextField idField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField fatherField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField contactField = new JTextField();
    private final JTextField positionField = new JTextField();
    private final JTextField salaryField = new JTextField();

    public EmployeeManagementSystem() {
        setTitle("Employee Management System");
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loadEmployees();

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(12, 12, 12, 12));
        main.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("EMPLOYEE MANAGEMENT SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(new Color(45, 55, 72));
        title.setBorder(new EmptyBorder(8, 5, 15, 5));
        main.add(title, BorderLayout.NORTH);

        JPanel formPanel = createFormPanel();
        main.add(formPanel, BorderLayout.WEST);

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Father's Name", "Email", "Contact", "Position", "Salary"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        JScrollPane tableScroll = new JScrollPane(table);
        main.add(tableScroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);

        searchField = new JTextField();
        searchField.setToolTipText("Search by ID, name, position or email");
        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        searchPanel.setPreferredSize(new Dimension(430, 40));

        bottom.add(searchPanel, BorderLayout.WEST);
        bottom.add(showAllButton, BorderLayout.EAST);
        main.add(bottom, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> searchEmployees());
        showAllButton.addActionListener(e -> refreshTable(employees));
        searchField.addActionListener(e -> searchEmployees());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                fillFormFromTable(table.getSelectedRow());
            }
        });

        setContentPane(main);
        refreshTable(employees);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(310, 470));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        addLabelAndField(panel, gbc, 0, "Employee ID *", idField);
        addLabelAndField(panel, gbc, 1, "Employee Name *", nameField);
        addLabelAndField(panel, gbc, 2, "Father's Name", fatherField);
        addLabelAndField(panel, gbc, 3, "Email", emailField);
        addLabelAndField(panel, gbc, 4, "Contact", contactField);
        addLabelAndField(panel, gbc, 5, "Position", positionField);
        addLabelAndField(panel, gbc, 6, "Salary", salaryField);

        JButton addButton = new JButton("Add Employee");
        JButton updateButton = new JButton("Update Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton clearButton = new JButton("Clear");

        addButton.addActionListener(e -> addEmployee());
        updateButton.addActionListener(e -> updateEmployee());
        deleteButton.addActionListener(e -> deleteEmployee());
        clearButton.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new GridLayout(2, 2, 6, 6));
        buttons.setOpaque(false);
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        panel.add(buttons, gbc);

        return panel;
    }

    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, int row,
                                  String label, JTextField field) {
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        field.setPreferredSize(new Dimension(150, 28));
        panel.add(field, gbc);
    }

    private void addEmployee() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();

        if (id.isEmpty() || name.isEmpty()) {
            showMessage("Employee ID and Employee Name are required.");
            return;
        }

        if (findById(id) != null) {
            showMessage("Employee ID already exists.");
            return;
        }

        Employee emp = readForm();
        employees.add(emp);
        saveEmployees();
        refreshTable(employees);
        clearForm();
        showMessage("Employee added successfully.");
    }

    private void updateEmployee() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showMessage("Please select an employee from the table.");
            return;
        }

        String oldId = tableModel.getValueAt(row, 0).toString();
        Employee existing = findById(oldId);

        if (existing == null) {
            showMessage("Employee not found.");
            return;
        }

        String newId = idField.getText().trim();
        String newName = nameField.getText().trim();

        if (newId.isEmpty() || newName.isEmpty()) {
            showMessage("Employee ID and Employee Name are required.");
            return;
        }

        Employee sameId = findById(newId);
        if (sameId != null && sameId != existing) {
            showMessage("Another employee already uses this ID.");
            return;
        }

        Employee updated = readForm();
        int index = employees.indexOf(existing);
        employees.set(index, updated);

        saveEmployees();
        refreshTable(employees);
        clearForm();
        showMessage("Employee updated successfully.");
    }

    private void deleteEmployee() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showMessage("Please select an employee from the table.");
            return;
        }

        String id = tableModel.getValueAt(row, 0).toString();
        Employee employee = findById(id);

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Delete employee " + id + " - " + employee.name + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (answer == JOptionPane.YES_OPTION) {
            employees.remove(employee);
            saveEmployees();
            refreshTable(employees);
            clearForm();
            showMessage("Employee deleted successfully.");
        }
    }

    private Employee readForm() {
        return new Employee(
                idField.getText().trim(),
                nameField.getText().trim(),
                fatherField.getText().trim(),
                emailField.getText().trim(),
                contactField.getText().trim(),
                positionField.getText().trim(),
                salaryField.getText().trim()
        );
    }

    private void fillFormFromTable(int row) {
        idField.setText(tableModel.getValueAt(row, 0).toString());
        nameField.setText(tableModel.getValueAt(row, 1).toString());
        fatherField.setText(tableModel.getValueAt(row, 2).toString());
        emailField.setText(tableModel.getValueAt(row, 3).toString());
        contactField.setText(tableModel.getValueAt(row, 4).toString());
        positionField.setText(tableModel.getValueAt(row, 5).toString());
        salaryField.setText(tableModel.getValueAt(row, 6).toString());
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        fatherField.setText("");
        emailField.setText("");
        contactField.setText("");
        positionField.setText("");
        salaryField.setText("");
        table.clearSelection();
        idField.requestFocus();
    }

    private Employee findById(String id) {
        for (Employee e : employees) {
            if (e.id.equalsIgnoreCase(id)) {
                return e;
            }
        }
        return null;
    }

    private void refreshTable(List<Employee> list) {
        tableModel.setRowCount(0);
        for (Employee e : list) {
            tableModel.addRow(new Object[]{
                    e.id, e.name, e.fatherName, e.email,
                    e.contact, e.position, e.salary
            });
        }
    }

    private void searchEmployees() {
        String q = searchField.getText().trim().toLowerCase();

        if (q.isEmpty()) {
            refreshTable(employees);
            return;
        }

        List<Employee> result = new ArrayList<>();
        for (Employee e : employees) {
            if (e.id.toLowerCase().contains(q)
                    || e.name.toLowerCase().contains(q)
                    || e.position.toLowerCase().contains(q)
                    || e.email.toLowerCase().contains(q)) {
                result.add(e);
            }
        }

        refreshTable(result);
    }

    @SuppressWarnings("unchecked")
    private void loadEmployees() {
        if (!dataFile.exists()) {
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(dataFile))) {
            employees.addAll((List<Employee>) in.readObject());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load existing employee data.\nA new data file will be used.",
                    "Data Warning",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void saveEmployees() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dataFile))) {
            out.writeObject(employees);
        } catch (IOException e) {
            showMessage("Could not save employee data: " + e.getMessage());
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private static class Employee implements Serializable {
        private static final long serialVersionUID = 1L;

        String id, name, fatherName, email, contact, position, salary;

        Employee(String id, String name, String fatherName, String email,
                 String contact, String position, String salary) {
            this.id = id;
            this.name = name;
            this.fatherName = fatherName;
            this.email = email;
            this.contact = contact;
            this.position = position;
            this.salary = salary;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            new EmployeeManagementSystem().setVisible(true);
        });
    }
}

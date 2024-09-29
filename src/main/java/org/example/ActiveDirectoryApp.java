package org.example;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ActiveDirectoryApp extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;

    final int ACCOUNT_DISABLED_FLAG = 2;
    final int ACCOUNT_NORMAL_FLAG = 512;
    private String ACCOUNT_CURRENT_STATUS_USER;
    private String LOGIN = "";
    private String PASSWORD = "";

    Hashtable<String, String> env = new Hashtable<>();

    public ActiveDirectoryApp() {
        showLoginDialog();
    }
    private void initializeMainInterface(String username, char[] password) {
        setTitle("Управление пользователями Active Directory");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 600);
        LOGIN = username;
        PASSWORD = new String(password);

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://192.168.75.9");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, LOGIN + "@aues.student");
        env.put(Context.SECURITY_CREDENTIALS, PASSWORD);

        // Создание таблицы
        tableModel = new DefaultTableModel();
        tableModel.addColumn("Имя");
        tableModel.addColumn("Должность");
        tableModel.addColumn("Номер телефона");
        tableModel.addColumn("Email");
        tableModel.addColumn("Отдел");
        tableModel.addColumn("Организация");
        tableModel.addColumn("Дата создания");
        tableModel.addColumn("Последние изменения");
        tableModel.addColumn("Статус");

        table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Обновить");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshUserData();
            }
        });

        JButton editButton = new JButton("Изменить");
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    showEditDialog(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(ActiveDirectoryApp.this, "Выберите пользователя для редактирования.");
                }
            }
        });

        JButton lockButton = new JButton("Заблокировать/Разблокировать");
        lockButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    lockUser(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(ActiveDirectoryApp.this, "Выберите пользователя для блокировки.");
                }
            }
        });

        JButton addButton = new JButton("Добавить пользователя");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUser();
            }
        });

        JButton deleteButton = new JButton("Удалить пользователя");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    deleteUser(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(ActiveDirectoryApp.this, "Выберите пользователя для блокировки.");
                }
            }
        });

        // Создание панели для размещения кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(refreshButton);
        buttonPanel.add(editButton);
        buttonPanel.add(lockButton);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);

        // Добавление панели с кнопками внизу окна
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        refreshUserData(); // Инициализация данных при запуске
    }
    private void deleteUser(final int selectedRow){
        try {
            String userName = (String) tableModel.getValueAt(selectedRow, 0);

            System.out.println("UserName delete: " + userName);

            String userDN = "CN=" + userName + ",CN=Users,DC=aues,DC=student";

            DirContext context = new InitialDirContext(env);

            // Удаляем запись пользователя
            context.destroySubcontext(userDN);

            System.out.println("Пользователь успешно удален из Active Directory.");

            // Закрываем соединение
            context.close();

        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    private void addUser(){
        final JDialog editDialog = new JDialog(this, "Редактировать данные пользователя", true);
        editDialog.setSize(400, 300);
        editDialog.setLayout(new GridLayout(11, 2));

        final JTextField firstNameField = new JTextField();
        final JTextField secondNameField = new JTextField();
        final JTextField passwordField = new JTextField();
        final JTextField loginField = new JTextField();
        final JTextField titleField = new JTextField();
        final JTextField phoneNumberField = new JTextField();
        final JTextField emailField = new JTextField();
        final JTextField otdelField = new JTextField();
        final JTextField orgField = new JTextField();

        editDialog.add(new JLabel("Имя:"));
        editDialog.add(firstNameField);
        editDialog.add(new JLabel("Фамилия:"));
        editDialog.add(secondNameField);
        editDialog.add(new JLabel("Логин(на англ.):"));
        editDialog.add(loginField);
        editDialog.add(new JLabel("Пароль:"));
        editDialog.add(passwordField);
        editDialog.add(new JLabel("Должность:"));
        editDialog.add(titleField);
        editDialog.add(new JLabel("Номер телефона:"));
        editDialog.add(phoneNumberField);
        editDialog.add(new JLabel("Email:"));
        editDialog.add(emailField);
        editDialog.add(new JLabel("Отдел:"));
        editDialog.add(otdelField);
        editDialog.add(new JLabel("Организация:"));
        editDialog.add(orgField);

        JButton saveButton = new JButton("Добавить");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String firstName = !Objects.equals(firstNameField.getText(), "") ? firstNameField.getText() : " ";
                    String secondName = !Objects.equals(secondNameField.getText(), "") ? secondNameField.getText() : " ";
                    String newLoginName = !Objects.equals(loginField.getText(), "") ? loginField.getText() : " ";
                    String newTitle = !Objects.equals(titleField.getText(), "") ? titleField.getText() : " ";
                    String newPhoneNumber = !Objects.equals(phoneNumberField.getText(), "") ? phoneNumberField.getText() : " ";
                    String newEmail = !Objects.equals(emailField.getText(), "") ? emailField.getText() : " ";
                    String newDepartment = !Objects.equals(otdelField.getText(), "") ? otdelField.getText() : " ";
                    String newCompany = !Objects.equals(orgField.getText(), "") ? orgField.getText() : " ";
                    String newPassword = !Objects.equals(passwordField.getText(), "") ? passwordField.getText() : " ";;

                    DirContext context = new InitialDirContext(env);

                    String userDN = "CN=" + firstName + " " + secondName + ",CN=Users,DC=aues,DC=student"; // Укажите путь к Users в вашем домене
                    Attribute attribute = new BasicAttribute("objectClass");
                    attribute.add("top");
                    attribute.add("person");
                    attribute.add("organizationalPerson");
                    attribute.add("user");

                    Attribute sAMAccountName = new BasicAttribute("sAMAccountName", newLoginName);
                    Attribute userPrincipalName = new BasicAttribute("userPrincipalName", newLoginName + "@aues.student");
                    Attribute givenName = new BasicAttribute("givenName", firstName);
                    Attribute sn = new BasicAttribute("sn", secondName);
                    Attribute displayName = new BasicAttribute("displayName", firstName + " " + secondName);
                    Attribute title = new BasicAttribute("title", newTitle);
                    Attribute phoneNumber = new BasicAttribute("telephoneNumber", newPhoneNumber);
                    Attribute email = new BasicAttribute("mail", newEmail);
                    Attribute department = new BasicAttribute("department", newDepartment);
                    Attribute company = new BasicAttribute("company", newCompany);

                    Attributes entry = new BasicAttributes();
                    entry.put(attribute);
                    entry.put(sAMAccountName);
                    entry.put(userPrincipalName);
                    entry.put(givenName);
                    entry.put(sn);
                    entry.put(displayName);
                    entry.put(title);
                    entry.put(phoneNumber);
                    entry.put(email);
                    entry.put(department);
                    entry.put(company);

                    context.createSubcontext(userDN, entry);

                    System.out.println("Пользователь успешно добавлен в Active Directory.");

                    // Закрываем соединение
                    context.close();

                } catch (NamingException er) {
                    er.printStackTrace();
                }

                editDialog.dispose(); // Закрыть диалог
            }
        });


        editDialog.add(saveButton);
        editDialog.setVisible(true);
    }
    private void showLoginDialog() {
        final JDialog loginDialog = new JDialog(this, "Вход в систему", true);
        loginDialog.setSize(400, 200);
        loginDialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Отступы

        final JTextField usernameField = new JTextField(20);
        final JPasswordField passwordField = new JPasswordField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        loginDialog.add(new JLabel("Логин:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        loginDialog.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        loginDialog.add(new JLabel("Пароль:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        loginDialog.add(passwordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        JButton loginButton = new JButton("Войти");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String enteredUsername = usernameField.getText();
                char[] enteredPassword = passwordField.getPassword();

                if (authenticateUser(enteredUsername, enteredPassword)) {
                    loginDialog.dispose();
                    initializeMainInterface(enteredUsername, enteredPassword);
                } else {
                    JOptionPane.showMessageDialog(loginDialog, "Неверный логин или пароль.");
                }
            }
        });
        loginDialog.add(loginButton, gbc);

        loginDialog.setLocationRelativeTo(null);
        loginDialog.setVisible(true);
    }
    private boolean authenticateUser(String username, char[] password) {
        return !username.isEmpty() && password.length > 0;
    }
    private void showEditDialog(final int selectedRow) {
        final String name = (String) tableModel.getValueAt(selectedRow, 0);
        final String title = (String) tableModel.getValueAt(selectedRow, 1);
        final String phoneNumber = (String) tableModel.getValueAt(selectedRow, 2);
        final String email = (String) tableModel.getValueAt(selectedRow, 3);
        final String otdel = (String) tableModel.getValueAt(selectedRow, 4);
        final String org = (String) tableModel.getValueAt(selectedRow, 5);

        final JDialog editDialog = new JDialog(this, "Редактировать данные пользователя", true);
        editDialog.setSize(400, 250);
        editDialog.setLayout(new GridLayout(8, 2));

        final JTextField nameField = new JTextField(name);
        final JTextField titleField = new JTextField(title);
        final JTextField phoneNumberField = new JTextField(phoneNumber);
        final JTextField emailField = new JTextField(email);
        final JTextField otdelField = new JTextField(otdel);
        final JTextField orgField = new JTextField(org);

        editDialog.add(new JLabel("Имя:"));
        editDialog.add(nameField);
        editDialog.add(new JLabel("Должность:"));
        editDialog.add(titleField);
        editDialog.add(new JLabel("Номер телефона:"));
        editDialog.add(phoneNumberField);
        editDialog.add(new JLabel("Email:"));
        editDialog.add(emailField);
        editDialog.add(new JLabel("Отдел:"));
        editDialog.add(otdelField);
        editDialog.add(new JLabel("Организация:"));
        editDialog.add(orgField);


        JButton saveButton = new JButton("Сохранить");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.setValueAt(nameField.getText(), selectedRow, 0);
                tableModel.setValueAt(titleField.getText(), selectedRow, 1);
                tableModel.setValueAt(phoneNumberField.getText(), selectedRow, 2);
                tableModel.setValueAt(emailField.getText(), selectedRow, 3);
                tableModel.setValueAt(otdelField.getText(), selectedRow, 4);
                tableModel.setValueAt(orgField.getText(), selectedRow, 5);

                try {
                    DirContext ctx = new InitialDirContext(env);

                    String encodedName = new String(nameField.getText().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                    //String userDN = "CN=" + nameField.getText() + ",CN=Users,DC=aues,DC=student";
                    String userDN = "CN=" + encodedName + ",CN=Users,DC=aues,DC=student";

                    ModificationItem[] mods = new ModificationItem[5];
                    mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("title", !Objects.equals(titleField.getText(), "") ? titleField.getText() : " "));
                    mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("telephoneNumber", !Objects.equals(phoneNumberField.getText(), "") ? phoneNumberField.getText() : " "));
                    mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("mail", !Objects.equals(emailField.getText(), "") ? emailField.getText() : " "));
                    mods[3] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("department", !Objects.equals(otdelField.getText(), "") ? otdelField.getText() : " "));
                    mods[4] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("company", !Objects.equals(orgField.getText(), "") ? orgField.getText() : " "));

                    ctx.modifyAttributes(userDN, mods);

                    // Закрытие соединения
                    ctx.close();
                } catch (NamingException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(editDialog, "Ошибка при обновлении данных в Active Directory.");
                }

                editDialog.dispose();
            }
        });


        editDialog.add(saveButton);
        editDialog.setVisible(true);
    }
    private void refreshUserData() {
        tableModel.setRowCount(0);

        try {
            DirContext ctx = new InitialDirContext(env);

            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> results = ctx.search("CN=Users,DC=aues,DC=student", "(objectClass=user)", controls);

            while (results.hasMore()) {
                SearchResult result = results.next();
                Attributes attributes = result.getAttributes();

                String name = attributes.get("cn").get().toString();
                String title = attributes.get("title") != null ? attributes.get("title").get().toString() : "";
                String phoneNumber = attributes.get("telephoneNumber") != null ? attributes.get("telephoneNumber").get().toString() : "";
                String email = attributes.get("mail") != null ? attributes.get("mail").get().toString() : "";
                String otdel = attributes.get("department") != null ? attributes.get("department").get().toString() : "";
                String org = attributes.get("company") != null ? attributes.get("company").get().toString() : "";
                String whenCreated = attributes.get("whenCreated") != null ? attributes.get("whenCreated").get().toString() : "";
                String whenChanged = attributes.get("whenChanged") != null ? attributes.get("whenChanged").get().toString() : "";
                String statusUser = attributes.get("userAccountControl") != null ? attributes.get("userAccountControl").get().toString() : "";

                statusUser = Objects.equals(statusUser, "514") ? "Заблокирован" : "Разблокирован";

                String formattedWhenCreated = formatDate(whenCreated);
                String formattedWhenChanged = formatDate(whenChanged);
                Object[] rowData = {name, title, phoneNumber, email, otdel, org, formattedWhenCreated, formattedWhenChanged, statusUser};
                tableModel.addRow(rowData);
            }

            ctx.close();
        } catch (NamingException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при обновлении данных из Active Directory.");
        }
    }
    private void lockUser(int selectedRow) {
        try {

            DirContext ctx = new InitialDirContext(env);

            String userName = (String) tableModel.getValueAt(selectedRow, 0);
            ACCOUNT_CURRENT_STATUS_USER = (String) tableModel.getValueAt(selectedRow, 8);

            System.out.println("userAccountControl: " + ACCOUNT_CURRENT_STATUS_USER);
            String encodedName = new String(userName.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            String userDN = "CN=" + encodedName + ",CN=Users,DC=aues,DC=student";

            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(
                    DirContext.REPLACE_ATTRIBUTE,
                    new BasicAttribute("userAccountControl", Objects.equals(ACCOUNT_CURRENT_STATUS_USER, "514") ? Integer.toString(ACCOUNT_NORMAL_FLAG) : Integer.toString(ACCOUNT_DISABLED_FLAG))
            );

            System.out.println(ACCOUNT_CURRENT_STATUS_USER);
            System.out.println(ACCOUNT_NORMAL_FLAG);
            System.out.println(ACCOUNT_DISABLED_FLAG);
            System.out.println(Integer.parseInt(ACCOUNT_CURRENT_STATUS_USER) - ACCOUNT_DISABLED_FLAG);
            System.out.println(ACCOUNT_CURRENT_STATUS_USER);

            ctx.modifyAttributes(userDN, mods);

            ctx.close();

            JOptionPane.showMessageDialog(this, "Пользователь " + userName + " заблокирован/разблокирован.");
        } catch (NamingException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при блокировке пользователя в Active Directory.");
        }
    }
    private static String formatDate(String rawDate) {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyyMMddHHmmss.S'Z'");
        inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        outputDateFormat.setTimeZone(TimeZone.getDefault());

        try {
            Date date = inputDateFormat.parse(rawDate);

            return outputDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return rawDate;
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ActiveDirectoryApp().setVisible(true);
            }
        });
    }
}

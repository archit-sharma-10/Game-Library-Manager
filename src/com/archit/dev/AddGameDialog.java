package com.archit.dev;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class AddGameDialog extends JDialog {
    private JTextField titleField = new JTextField(20);
    private JTextField genreField = new JTextField();
    private JTextField ratingField = new JTextField();
    private JTextField hoursField = new JTextField();
    private JComboBox<String> statusDropdown;

    private JList<String> suggestionList = new JList<>();
    private JScrollPane suggestionScrollPane = new JScrollPane(suggestionList);
    private JPopupMenu suggestionPopup = new JPopupMenu();

    private Game newGame = null;

    public AddGameDialog(JFrame parent, Trie masterTrie, HashMap<String, Game> masterMap) {
        super(parent, "Add a New Game (with Auto-fill)", true);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Theme.BACKGROUND);
        setContentPane(mainPanel);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBackground(Theme.BACKGROUND);
        
        formPanel.add(createStyledLabel("Title:"));
        formPanel.add(createStyledTextField(titleField));
        formPanel.add(createStyledLabel("Genre:"));
        formPanel.add(createStyledTextField(genreField));
        formPanel.add(createStyledLabel("Rating (0-10):"));
        formPanel.add(createStyledTextField(ratingField));
        formPanel.add(createStyledLabel("Hours to Beat:"));
        formPanel.add(createStyledTextField(hoursField));
        formPanel.add(createStyledLabel("Status:"));
        statusDropdown = createStyledComboBox(new String[]{"Backlog", "Playing", "Completed", "Wishlist"});
        formPanel.add(statusDropdown);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Theme.BACKGROUND);
        JButton okButton = createStyledButton("OK");
        JButton cancelButton = createStyledButton("Cancel");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        suggestionPopup.setBorder(new LineBorder(Theme.ACCENT_COLOR_1));
        suggestionList.setBackground(Theme.COMPONENT_BACKGROUND);
        suggestionList.setForeground(Theme.FOREGROUND);
        suggestionList.setFont(Theme.MAIN_FONT);
        suggestionPopup.add(suggestionScrollPane);

        titleField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String prefix = titleField.getText().toLowerCase();
                if (prefix.length() < 2) {
                    suggestionPopup.setVisible(false);
                    return;
                }
                List<String> suggestions = masterTrie.findByPrefix(prefix);
                if (!suggestions.isEmpty()) {
                    suggestionList.setListData(suggestions.toArray(new String[0]));
                    suggestionPopup.show(titleField, 0, titleField.getHeight());
                    suggestionPopup.setFocusable(false);
                    titleField.requestFocusInWindow();
                } else {
                    suggestionPopup.setVisible(false);
                }
            }
        });
        
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String selectedTitle = suggestionList.getSelectedValue();
                if (selectedTitle != null) {
                    Game gameData = masterMap.get(selectedTitle.toLowerCase());
                    if (gameData != null) {
                        titleField.setText(gameData.getTitle());
                        genreField.setText(gameData.getGenre());
                        ratingField.setText(String.valueOf(gameData.getRating()));
                        hoursField.setText(String.valueOf(gameData.getHours()));
                        suggestionPopup.setVisible(false);
                    }
                }
            }
        });

        okButton.addActionListener(e -> {
            if (processInput()) {
                dispose();
            }
        });
        cancelButton.addActionListener(e -> {
            newGame = null;
            dispose();
        });

        pack();
        setLocationRelativeTo(parent);
    }

    public Game getNewGame() {
        return newGame;
    }

    // --- THIS METHOD IS NOW CORRECTED ---
    private boolean processInput() {
        try {
            // Retrieve the data from the form
            String title = titleField.getText();
            String genre = genreField.getText();
            int rating = Integer.parseInt(ratingField.getText());
            int hours = Integer.parseInt(hoursField.getText());
            String status = (String) statusDropdown.getSelectedItem();

            // Validate the data
            if (title.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false; // Stay on the dialog
            }
            if (rating < 0 || rating > 10) {
                JOptionPane.showMessageDialog(this, "Rating must be between 0 and 10.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false; // Stay on the dialog
            }

            // Create the new game object and store it
            newGame = new Game(title, genre, rating, hours, status);
            return true; // Input is valid, dialog can close

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for Rating and Hours.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false; // Stay on the dialog
        }
    }

    // --- Helper methods for styling components (unchanged) ---
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.FOREGROUND);
        label.setFont(Theme.BOLD_FONT);
        return label;
    }

    private JTextField createStyledTextField(JTextField textField) {
        textField.setBackground(Theme.COMPONENT_BACKGROUND);
        textField.setForeground(Theme.FOREGROUND);
        textField.setFont(Theme.MAIN_FONT);
        textField.setCaretColor(Theme.FOREGROUND);
        textField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.ACCENT_COLOR_1, 1),
            new EmptyBorder(5, 5, 5, 5)
        ));
        return textField;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setBackground(Theme.COMPONENT_BACKGROUND);
        comboBox.setForeground(Theme.FOREGROUND);
        comboBox.setFont(Theme.MAIN_FONT);
        return comboBox;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Theme.ACCENT_COLOR_1);
        button.setForeground(Color.WHITE);
        button.setFont(Theme.BOLD_FONT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(5, 15, 5, 15));
        return button;
    }
}
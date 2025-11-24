package com.archit.dev;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Main {
    private static List<Game> gameList = new ArrayList<>();
    private static Trie gameTrie = new Trie();
    private static DefaultTableModel tableModel;
    private static Trie masterGameTrie = new Trie();
    private static HashMap<String, Game> masterGameMap = new HashMap<>();
    private static int sortedColumn = -1;
    private static boolean isAscending = true;
    private static String[] columnNames = {"Title", "Genre", "Rating", "Hours", "Status"};

    private static JLabel totalGamesLabel;
    private static JLabel completedGamesLabel;

    public static void main(String[] args) {

        // Theme
        try {
            UIManager.put("Panel.background", Theme.BACKGROUND);
            UIManager.put("OptionPane.background", Theme.BACKGROUND);
            UIManager.put("OptionPane.messageForeground", Theme.FOREGROUND);
            UIManager.put("Button.background", Theme.ACCENT_COLOR_1);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.font", Theme.BOLD_FONT);
            UIManager.put("Button.focus", new Color(0,0,0,0));
            UIManager.put("Menu.foreground", Theme.FOREGROUND);
            UIManager.put("MenuItem.foreground", Theme.FOREGROUND);
            UIManager.put("Menu.background", Theme.COMPONENT_BACKGROUND);
            UIManager.put("MenuItem.background", Theme.COMPONENT_BACKGROUND);
            UIManager.put("Menu.selectionForeground", Color.WHITE);
            UIManager.put("MenuItem.selectionForeground", Color.WHITE);
            UIManager.put("Menu.selectionBackground", Theme.ACCENT_COLOR_1);
            UIManager.put("MenuItem.selectionBackground", Theme.ACCENT_COLOR_1);
            UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(Theme.ACCENT_COLOR_1));
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadUserLibrary();
        loadMasterDatabase();
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }
    
    // JFrame for the main UI window
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Game Library Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1024, 768);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Theme.BACKGROUND);

        // Search bar
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        searchPanel.setBackground(Theme.BACKGROUND);
        JLabel searchLabel = new JLabel("Search Library:");
        searchLabel.setFont(Theme.HEADER_FONT);
        searchLabel.setForeground(Theme.FOREGROUND);
        JTextField searchField = new JTextField();
        searchField.setBackground(Theme.COMPONENT_BACKGROUND);
        searchField.setForeground(Theme.FOREGROUND);
        searchField.setFont(Theme.MAIN_FONT);
        searchField.setCaretColor(Theme.FOREGROUND);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.ACCENT_COLOR_1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        updateTableWithData(gameList);

        //Table
        JTable table = new JTable(tableModel);

        table.setFont(Theme.MAIN_FONT);
        table.setRowHeight(28);
        table.setBackground(Theme.COMPONENT_BACKGROUND);
        table.setForeground(Theme.FOREGROUND);
        table.setGridColor(Theme.TABLE_GRID);
        table.setSelectionBackground(Theme.TABLE_SELECTION_BACKGROUND);
        table.setSelectionForeground(Theme.TABLE_SELECTION_FOREGROUND);
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusColumnCellRenderer());

        //Table headers
        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.BOLD_FONT);
        header.setBackground(Theme.BACKGROUND);
        header.setForeground(Theme.ACCENT_COLOR_1);
        header.setReorderingAllowed(false);
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = header.columnAtPoint(e.getPoint());
                handleSort(col, header);
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.getViewport().setBackground(Theme.COMPONENT_BACKGROUND);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(Theme.ACCENT_COLOR_1));

        //To see filtered rows only and update table
        //TRIE IMPLEMENTATION FOR GAMES.JSON 
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override public void changedUpdate(DocumentEvent e) {}
            private void filterTable() {
                String query = searchField.getText().toLowerCase();
                if (query.isEmpty()) {
                    updateTableWithData(gameList);
                } else {
                    List<String> matchingTitles = gameTrie.findByPrefix(query);
                    List<Game> filteredGames = gameList.stream()
                        .filter(game -> matchingTitles.contains(game.getTitle().toLowerCase()))
                        .collect(Collectors.toList());
                    updateTableWithData(filteredGames);
                }
            }
        });

        //Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Theme.BACKGROUND);
        bottomPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        totalGamesLabel = createStyledInfoLabel("Total Games: 0");
        completedGamesLabel = createStyledInfoLabel("Completed: 0");

        JPanel buttonContainer = new JPanel();
        buttonContainer.setBackground(Theme.BACKGROUND);
        
        // PRIORITY QUEUE IMPLEMENTATION IS HERE FOR RECOMMENDATION ALGORITHMS

        JButton addGameButton = createStyledButton("Add New Game");
        JButton quickWinButton = createStyledButton("Need a Quick Win");
        JButton topQualityButton = createStyledButton("Suggest Top Quality");
        JButton luckyButton = createStyledButton("I'm Feeling Lucky!");

        addGameButton.addActionListener(e -> showAddGameDialog(frame));
        quickWinButton.addActionListener(e -> {
            List<Game> backlogGames = getBacklogGames();
            if (handleEmptyBacklog(frame, backlogGames)) return;
            Comparator<Game> comparator = (g1, g2) -> Double.compare(
                (g2.getRating() * 10) - (g2.getHours() / 5.0),
                (g1.getRating() * 10) - (g1.getHours() / 5.0)
            );
            PriorityQueue<Game> pq = new PriorityQueue<>(comparator);
            pq.addAll(backlogGames);
            showRecommendation(frame, "Quick Win?", "We suggest you to play:\n" + pq.peek().getTitle());
        });
        topQualityButton.addActionListener(e -> {
            List<Game> backlogGames = getBacklogGames();
            if (handleEmptyBacklog(frame, backlogGames)) return;
            Comparator<Game> comparator = Comparator.comparingInt(Game::getRating).reversed();
            PriorityQueue<Game> pq = new PriorityQueue<>(comparator);
            pq.addAll(backlogGames);
            showRecommendation(frame, "Top Quality Suggestion?", "Ignoring time, the Top Quality game is:\n" + pq.peek().getTitle());
        });
        luckyButton.addActionListener(e -> {
            List<Game> backlogGames = getBacklogGames();
            if (handleEmptyBacklog(frame, backlogGames)) return;
            Random random = new Random();
            Game recommendedGame = backlogGames.get(random.nextInt(backlogGames.size()));
            showRecommendation(frame, "Feeling Lucky Huh?", "Trust me with this one:\n" + recommendedGame.getTitle());
        });

        buttonContainer.add(addGameButton);
        buttonContainer.add(quickWinButton);
        buttonContainer.add(topQualityButton);
        buttonContainer.add(luckyButton);
        
        bottomPanel.add(totalGamesLabel, BorderLayout.WEST);
        bottomPanel.add(buttonContainer, BorderLayout.CENTER);
        bottomPanel.add(completedGamesLabel, BorderLayout.EAST);

        //Right click menu
        JPopupMenu contextMenu = new JPopupMenu();
        JMenu setStatusMenu = new JMenu("Set Status");
        styleMenuItem(setStatusMenu);
        JMenuItem playingItem = new JMenuItem("Playing");
        JMenuItem backlogItem = new JMenuItem("Backlog");
        JMenuItem completedItem = new JMenuItem("Completed");
        JMenuItem wishlistItem = new JMenuItem("Wishlist");
        playingItem.addActionListener(e -> updateGameStatus("Playing", table));
        backlogItem.addActionListener(e -> updateGameStatus("Backlog", table));
        completedItem.addActionListener(e -> updateGameStatus("Completed", table));
        wishlistItem.addActionListener(e -> updateGameStatus("Wishlist", table));
        setStatusMenu.add(playingItem);
        setStatusMenu.add(backlogItem);
        setStatusMenu.add(completedItem);
        setStatusMenu.add(wishlistItem);
        contextMenu.add(setStatusMenu);
        contextMenu.addSeparator();
        JMenuItem deleteItem = new JMenuItem("Delete Game");
        deleteItem.addActionListener(e -> deleteGame(frame, table));
        contextMenu.add(deleteItem);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < table.getRowCount()) {
                        table.setRowSelectionInterval(row, row);
                        contextMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });


        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                saveData();
            }
        });

        frame.add(searchPanel, BorderLayout.NORTH);
        frame.add(tableScrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        updateStatsLabels();
        frame.setVisible(true);
    }

    private static void styleMenuItem(JMenuItem item){
        item.setBackground(Theme.COMPONENT_BACKGROUND);
        item.setForeground(Theme.FOREGROUND);
        item.setFont(Theme.MAIN_FONT);
        if(item instanceof JMenu){
            ((JMenu) item).setOpaque(true);
        }else{
            item.setOpaque(true);
        }
    }

    //Sorting comparators and calling Sorters.java
    //MERGE SORT CALLED HERE
    private static void handleSort(int column, JTableHeader header) {
        Comparator<Game> comparator = null;
        switch (column) {
            case 0: comparator = Comparator.comparing(Game::getTitle, String.CASE_INSENSITIVE_ORDER); break;
            case 1: comparator = Comparator.comparing(Game::getGenre, String.CASE_INSENSITIVE_ORDER); break;
            case 2: comparator = Comparator.comparingInt(Game::getRating); break;
            case 3: comparator = Comparator.comparingInt(Game::getHours); break;
            case 4: comparator = Comparator.comparing(Game::getStatus, String.CASE_INSENSITIVE_ORDER); break;
            default: return;
        }
        if (column == sortedColumn) {
            isAscending = !isAscending;
        } else {
            sortedColumn = column;
            isAscending = true;
        }
        if (!isAscending) {
            comparator = comparator.reversed();
        }
        Sorters.mergeSort(gameList, comparator);
        updateTableWithData(gameList);
        updateHeaderSortIndicator(header);
    }

    private static void updateHeaderSortIndicator(JTableHeader header) {
        TableColumnModel colModel = header.getColumnModel();
        for (int i = 0; i < colModel.getColumnCount(); i++) {
            String originalText = columnNames[i];
            if (i == sortedColumn) {
                String indicator = isAscending ? " ▲" : " ▼";
                colModel.getColumn(i).setHeaderValue(originalText + indicator);
            } else {
                colModel.getColumn(i).setHeaderValue(originalText);
            }
        }
        header.repaint();
    }
    
    //ADD GAME DIALOG BOX USES HASHMAP
    private static void showAddGameDialog(JFrame parent) {
        AddGameDialog dialog = new AddGameDialog(parent, masterGameTrie, masterGameMap);
        dialog.setVisible(true);
        Game newGame = dialog.getNewGame();
        if (newGame != null) {
            gameList.add(newGame);
            gameTrie.insert(newGame.getTitle().toLowerCase());
            updateTableWithData(gameList);
            updateStatsLabels();
        }
    }

    private static void updateGameStatus(String newStatus, JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String gameTitle = (String) table.getValueAt(selectedRow, 0);
            for (Game game : gameList) {
                if (game.getTitle().equals(gameTitle)) {
                    game.setStatus(newStatus);
                    break;
                }
            }
            updateTableWithData(gameList);
            updateStatsLabels();
        }
    }
    
    private static void deleteGame(JFrame frame, JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String gameTitle = (String) table.getValueAt(selectedRow, 0);
            int response = JOptionPane.showConfirmDialog(frame, 
                "Are you sure you want to permanently delete '" + gameTitle + "'?", 
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                gameList.removeIf(game -> game.getTitle().equals(gameTitle));
                gameTrie = new Trie();
                for (Game game : gameList) {
                    gameTrie.insert(game.getTitle().toLowerCase());
                }
                updateTableWithData(gameList);
                updateStatsLabels();
            }
        }
    }
    
    private static void updateStatsLabels() {
        int totalCount = gameList.size();
        long completedCount = gameList.stream()
            .filter(game -> "Completed".equalsIgnoreCase(game.getStatus()))
            .count();
        totalGamesLabel.setText("Total Games: " + totalCount);
        completedGamesLabel.setText("Completed: " + completedCount);
    }

    private static void loadUserLibrary() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("data/games.json")) {
            Type type = new TypeToken<ArrayList<Game>>(){}.getType();
            gameList = gson.fromJson(reader, type);
            if (gameList == null) gameList = new ArrayList<>();
            gameTrie = new Trie();
            for (Game game : gameList) {
                gameTrie.insert(game.getTitle().toLowerCase());
            }
        } catch (Exception e) {
            System.err.println("Could not load user library (games.json). Starting with an empty library.");
            gameList = new ArrayList<>();
        }
    }

    private static void loadMasterDatabase() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("data/master_games.json")) {
            Type type = new TypeToken<ArrayList<Game>>(){}.getType();
            List<Game> masterList = gson.fromJson(reader, type);
            if (masterList == null) return;
            for (Game game : masterList) {
                String titleLower = game.getTitle().toLowerCase();
                masterGameTrie.insert(titleLower);
                masterGameMap.put(titleLower, game);
            }
            System.out.println("Loaded " + masterList.size() + " games into master database.");
        } catch (Exception e) {
            System.err.println("Could not load master game database (master_games.json). Auto-fill will not work.");
        }
    }

    private static void saveData() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("data/games.json")) {
            gson.toJson(gameList, writer);
            System.out.println("Game data saved successfully.");
        } catch (Exception e) {
            System.err.println("Error saving game data.");
            e.printStackTrace();
        }
    }

    private static void updateTableWithData(List<Game> games) {
        tableModel.setRowCount(0);
        for (Game game : games) {
            Object[] rowData = {
                game.getTitle(), game.getGenre(), game.getRating(),
                game.getHours(), game.getStatus()
            };
            tableModel.addRow(rowData);
        }
    }

    private static List<Game> getBacklogGames() {
        return gameList.stream()
            .filter(game -> game.getStatus().equalsIgnoreCase("Backlog"))
            .collect(Collectors.toList());
    }

    private static boolean handleEmptyBacklog(JFrame frame, List<Game> backlogGames) {
        if (backlogGames.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Your backlog is empty. Good job!");
            return true;
        }
        return false;
    }

    private static void showRecommendation(JFrame frame, String title, String message) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Theme.ACCENT_COLOR_1);
        button.setForeground(Color.WHITE);
        button.setFont(Theme.BOLD_FONT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(5, 15, 5, 15));
        return button;
    }
    
    private static JLabel createStyledInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.ACCENT_COLOR_2);
        label.setFont(Theme.BOLD_FONT);
        return label;
    }

    static class StatusColumnCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = (String) value;
            if (!isSelected) {
                switch (status.toLowerCase()) {
                    case "completed":
                        c.setBackground(Theme.ACCENT_COLOR_2.darker());
                        c.setForeground(Color.BLACK);
                        break;
                    case "playing":
                        c.setBackground(Theme.ACCENT_COLOR_1.darker());
                        c.setForeground(Color.WHITE);
                        break;
                    case "backlog":
                        c.setBackground(new Color(0x505050));
                        c.setForeground(Theme.FOREGROUND);
                        break;
                    case "wishlist":
                        c.setBackground(new Color(0x6A5ACD).darker());
                        c.setForeground(Color.WHITE);
                        break;
                    default:
                        c.setBackground(table.getBackground());
                        c.setForeground(table.getForeground());
                        break;
                }
            } else {
                c.setBackground(Theme.TABLE_SELECTION_BACKGROUND);
                c.setForeground(Theme.TABLE_SELECTION_FOREGROUND);
            }
            setHorizontalAlignment(JLabel.CENTER);
            setFont(getFont().deriveFont(Font.BOLD));
            return c;
        }
    }
}
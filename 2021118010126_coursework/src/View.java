import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class View implements Observer {
    private IModel model;
    private Controller controller;
    private JFrame frame;
    private JPanel boardPanel;
    private JScrollPane boardScrollPane;
    private JPanel keyboardPanel;
    private JButton resetButton;
    private JButton newGameButton;
    private JCheckBox showErrorMessagesCheckBox;
    private JCheckBox showPathCheckBox;
    private JCheckBox randomWordsCheckBox;
    private JLabel statusLabel;

    private JPanel startWordPanel;
    private JPanel attemptsPanel;
    private JPanel targetWordPanel;
    private JPanel currentInputPanel;
    private JButton[] keyButtons;
    private StringBuilder currentInput;

    // Initializes the view with model reference
    public View(IModel model) {
        this.model = model;
        this.currentInput = new StringBuilder();
        initComponents();
        updateBoard();
    }

    // Sets controller for handling user interactions
    public void setController(Controller controller) {
        this.controller = controller;
    }

    // Sets up UI components and layouts
    private void initComponents() {
        // Create main frame
        frame = new JFrame("Weaver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setMinimumSize(new Dimension(800, 600));

        // Create board panel with vertical box layout
        boardPanel = new JPanel();
        boardPanel.setLayout(new BoxLayout(boardPanel, BoxLayout.Y_AXIS));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create start word panel
        startWordPanel = createWordPanel();
        boardPanel.add(startWordPanel);
        boardPanel.add(Box.createVerticalStrut(10));

        // Create attempts panel
        attemptsPanel = new JPanel();
        attemptsPanel.setLayout(new BoxLayout(attemptsPanel, BoxLayout.Y_AXIS));
        boardPanel.add(attemptsPanel);

        // Create current input panel
        currentInputPanel = createWordPanel();
        boardPanel.add(currentInputPanel);
        boardPanel.add(Box.createVerticalStrut(10));

        // Create target word panel
        targetWordPanel = createWordPanel();
        boardPanel.add(targetWordPanel);

        // Add scroll pane for the board
        boardScrollPane = new JScrollPane(boardPanel);
        boardScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        boardScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        boardScrollPane.getVerticalScrollBar().setUnitIncrement(10);

        // Create keyboard panel
        keyboardPanel = new JPanel();
        keyboardPanel.setLayout(new BoxLayout(keyboardPanel, BoxLayout.Y_AXIS));
        keyboardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] keyRows = {
                "qwertyuiop",
                "asdfghjkl",
                "zxcvbnm"
        };

        keyButtons = new JButton[26];
        int keyIndex = 0;

        for (String row : keyRows) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

            for (char c : row.toCharArray()) {
                JButton keyButton = new JButton(String.valueOf(c).toUpperCase());
                keyButton.setFont(new Font("Arial", Font.BOLD, 16));
                keyButton.setPreferredSize(new Dimension(50, 50));
                keyButton.setBackground(Color.DARK_GRAY);
                keyButton.setForeground(Color.WHITE);
                keyButton.setFocusPainted(false);

                final char keyChar = c;
                keyButton.addActionListener(e -> {
                    if (controller != null) {
                        controller.handleKeyPress(keyChar);
                    }
                });

                keyButtons[keyIndex++] = keyButton;
                rowPanel.add(keyButton);
            }

            keyboardPanel.add(rowPanel);
        }

        // Create bottom row with Enter and Delete buttons
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JButton deleteButton = new JButton("Delete");
        deleteButton.setFont(new Font("Arial", Font.BOLD, 16));
        deleteButton.setPreferredSize(new Dimension(100, 50));
        deleteButton.addActionListener(e -> {
            if (controller != null) {
                controller.handleKeyPress('\b');
            }
        });

        JButton enterButton = new JButton("Enter");
        enterButton.setFont(new Font("Arial", Font.BOLD, 16));
        enterButton.setPreferredSize(new Dimension(100, 50));
        enterButton.addActionListener(e -> {
            if (controller != null) {
                controller.handleSubmit();
            }
        });

        bottomRow.add(deleteButton);
        bottomRow.add(enterButton);
        keyboardPanel.add(bottomRow);

        // Create controls panel
        JPanel controlsPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Status label
        statusLabel = new JLabel();
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Reset and New Game buttons
        resetButton = new JButton("Reset Game");
        resetButton.setFont(new Font("Arial", Font.PLAIN, 14));
        resetButton.addActionListener(e -> {
            if (controller != null) {
                controller.handleReset();
            }
        });

        newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("Arial", Font.PLAIN, 14));
        newGameButton.addActionListener(e -> {
            if (controller != null) {
                controller.handleNewGame();
            }
        });

        // Checkboxes for flags
        showErrorMessagesCheckBox = new JCheckBox("Show Error Messages");
        showErrorMessagesCheckBox.setSelected(model.isShowErrorMessages());
        showErrorMessagesCheckBox.addActionListener(e -> {
            if (controller != null) {
                controller.handleFlagChange("showErrorMessages", showErrorMessagesCheckBox.isSelected());
            }
        });

        showPathCheckBox = new JCheckBox("Show Path");
        showPathCheckBox.setSelected(model.isShowPath());
        showPathCheckBox.addActionListener(e -> {
            if (controller != null) {
                controller.handleFlagChange("showPath", showPathCheckBox.isSelected());
            }
        });

        randomWordsCheckBox = new JCheckBox("Random Words");
        randomWordsCheckBox.setSelected(model.isRandomWords());
        randomWordsCheckBox.addActionListener(e -> {
            if (controller != null) {
                controller.handleFlagChange("randomWords", randomWordsCheckBox.isSelected());
            }
        });

        controlsPanel.add(statusLabel);
        controlsPanel.add(resetButton);
        controlsPanel.add(newGameButton);
        controlsPanel.add(showErrorMessagesCheckBox);
        controlsPanel.add(showPathCheckBox);
        controlsPanel.add(randomWordsCheckBox);

        // Add components to frame
        frame.add(boardScrollPane, BorderLayout.CENTER);
        frame.add(keyboardPanel, BorderLayout.SOUTH);
        frame.add(controlsPanel, BorderLayout.EAST);

        // Add key listener for physical keyboard input
        frame.setFocusable(true);
        frame.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (controller == null) return;

                int keyCode = evt.getKeyCode();
                char keyChar = evt.getKeyChar();

                // Handle special keys
                if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                    controller.handleSubmit();
                } else if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                    controller.handleKeyPress('\b');
                } else if (Character.isLetter(keyChar)) {
                    // Handle letter keys
                    controller.handleKeyPress(keyChar);
                }

                // Keep focus on the frame
                frame.requestFocusInWindow();
            }
        });

        // Add window focus listener to ensure keyboard input works
        frame.addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                frame.requestFocusInWindow();
            }
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Request focus after the window is visible
        SwingUtilities.invokeLater(() -> {
            frame.requestFocusInWindow();
        });
    }

    // Creates a word panel with 4 cells for displaying letters
    private JPanel createWordPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 5, 5));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        for (int i = 0; i < 4; i++) {
            JLabel label = new JLabel(" ");
            label.setOpaque(true);
            label.setBackground(Color.LIGHT_GRAY);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 24));
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            label.setPreferredSize(new Dimension(60, 60));
            panel.add(label);
        }

        return panel;
    }

    // Updates the board display with current game state
    private void updateBoard() {
        // Update start word
        String startWord = model.getStartWord().toUpperCase();
        Component[] startComponents = startWordPanel.getComponents();
        for (int j = 0; j < 4; j++) {
            ((JLabel) startComponents[j]).setText(String.valueOf(startWord.charAt(j)));
            ((JLabel) startComponents[j]).setBackground(Color.LIGHT_GRAY);
        }

        // Update target word
        String targetWord = model.getTargetWord().toUpperCase();
        Component[] targetComponents = targetWordPanel.getComponents();
        for (int j = 0; j < 4; j++) {
            ((JLabel) targetComponents[j]).setText(String.valueOf(targetWord.charAt(j)));
            ((JLabel) targetComponents[j]).setBackground(Color.LIGHT_GRAY);
        }

        // Clear attempts panel and recreate it with all attempts
        attemptsPanel.removeAll();

        List<String> attempts = model.getAttempts();
        for (int i = 0; i < attempts.size(); i++) {
            String attempt = attempts.get(i).toUpperCase();
            int[] feedback = model.getFeedback(attempt.toLowerCase());

            JPanel attemptPanel = createWordPanel();
            Component[] attemptComponents = attemptPanel.getComponents();

            for (int j = 0; j < 4; j++) {
                ((JLabel) attemptComponents[j]).setText(String.valueOf(attempt.charAt(j)));

                // Set background color based on feedback
                if (feedback[j] == 1) {
                    ((JLabel) attemptComponents[j]).setBackground(new Color(76, 175, 80)); // Green
                } else if (feedback[j] == 0) {
                    ((JLabel) attemptComponents[j]).setBackground(new Color(255, 235, 59)); // Yellow
                } else {
                    ((JLabel) attemptComponents[j]).setBackground(new Color(158, 158, 158)); // Grey
                }
            }

            attemptsPanel.add(attemptPanel);
            attemptsPanel.add(Box.createVerticalStrut(5));
        }

        // Clear current input panel
        Component[] inputComponents = currentInputPanel.getComponents();
        for (int j = 0; j < 4; j++) {
            ((JLabel) inputComponents[j]).setText(" ");
            ((JLabel) inputComponents[j]).setBackground(Color.LIGHT_GRAY);
        }

        // Update status label
        statusLabel.setText("<html>Start: " + startWord + "<br>Target: " + targetWord + "</html>");

        // Revalidate and repaint the board panel
        attemptsPanel.revalidate();
        attemptsPanel.repaint();
        boardPanel.revalidate();
        boardPanel.repaint();

        // Scroll to the bottom to show the current input
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalScrollBar = boardScrollPane.getVerticalScrollBar();
            verticalScrollBar.setValue(verticalScrollBar.getMaximum());
        });

        // Check if the game is won
        if (model.hasWon()) {
            showWinMessage();
        }

        // Show path if enabled
        if (model.isShowPath()) {
            showPath();
        }
    }

    // Enables or disables the reset button based on game state
    public void setResetButtonEnabled(boolean enabled) {
        resetButton.setEnabled(enabled);
    }

    // Enables or disables the new game button based on game state
    public void setNewGameButtonEnabled(boolean enabled) {
        newGameButton.setEnabled(enabled);
    }

    // Updates the display of the current word being typed
    public void updateCurrentInput(String input) {
        Component[] inputComponents = currentInputPanel.getComponents();

        // Clear current input
        for (int j = 0; j < 4; j++) {
            ((JLabel) inputComponents[j]).setText(" ");
            ((JLabel) inputComponents[j]).setBackground(Color.LIGHT_GRAY);
        }

        // Display current input
        for (int j = 0; j < input.length(); j++) {
            ((JLabel) inputComponents[j]).setText(String.valueOf(input.charAt(j)).toUpperCase());
            ((JLabel) inputComponents[j]).setBackground(Color.WHITE);
        }
    }

    // Displays victory message when player successfully reaches target word
    public void showWinMessage() {
        JOptionPane.showMessageDialog(frame,
                "Congratulations! You've successfully transformed " +
                        model.getStartWord().toUpperCase() + " into " + model.getTargetWord().toUpperCase() + " in " +
                        model.getCurrentAttempt() + " attempts.",
                "You Win!", JOptionPane.INFORMATION_MESSAGE);
    }

    // Displays the optimal solution path from start to target word
    public void showPath() {
        List<String> path = model.findPath();

        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "No path found from " + model.getStartWord().toUpperCase() +
                            " to " + model.getTargetWord().toUpperCase(),
                    "No Path", JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder pathStr = new StringBuilder("<html>Path from " +
                    model.getStartWord().toUpperCase() + " to " +
                    model.getTargetWord().toUpperCase() + ":<br>");

            for (String word : path) {
                pathStr.append(word.toUpperCase()).append("<br>");
            }
            pathStr.append("</html>");

            JOptionPane.showMessageDialog(frame, pathStr.toString(),
                    "Path", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Displays error messages if error messages are enabled
    public void showErrorMessage(String message) {
        if (model.isShowErrorMessages()) {
            JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Observer pattern implementation to update UI when model changes
    @Override
    public void update(Observable o, Object arg) {
        updateBoard();
    }
}
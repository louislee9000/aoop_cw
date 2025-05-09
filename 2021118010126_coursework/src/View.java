import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * View component for the Weaver game GUI
 */
public class View implements Observer {
    private IModel model;
    private Controller controller;
    private JFrame frame;
    private JPanel boardPanel;
    private JPanel keyboardPanel;
    private JButton resetButton;
    private JButton newGameButton;
    private JCheckBox showErrorMessagesCheckBox;
    private JCheckBox showPathCheckBox;
    private JCheckBox randomWordsCheckBox;
    private JLabel statusLabel;

    private JLabel[][] letterLabels;
    private JButton[] keyButtons;
    private StringBuilder currentInput;

    /**
     * Constructor
     * @param model the model
     */
    public View(IModel model) {
        this.model = model;
        this.currentInput = new StringBuilder();
        initComponents();
        updateBoard();
    }

    /**
     * Set the controller
     * @param controller the controller
     */
    public void setController(Controller controller) {
        this.controller = controller;
    }

    /**
     * Initialize the components
     */
    private void initComponents() {
        // Create main frame
        frame = new JFrame("Weaver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setMinimumSize(new Dimension(800, 600));

        // Create board panel
        boardPanel = new JPanel(new GridLayout(6, 4, 5, 5));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        letterLabels = new JLabel[6][4]; // 6 rows for start word, 4 attempts, and current input

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                letterLabels[i][j] = new JLabel(" ");
                letterLabels[i][j].setOpaque(true);
                letterLabels[i][j].setBackground(Color.LIGHT_GRAY);
                letterLabels[i][j].setHorizontalAlignment(SwingConstants.CENTER);
                letterLabels[i][j].setFont(new Font("Arial", Font.BOLD, 24));
                letterLabels[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                letterLabels[i][j].setPreferredSize(new Dimension(60, 60));
                boardPanel.add(letterLabels[i][j]);
            }
        }

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
        resetButton.setEnabled(false);

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
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(keyboardPanel, BorderLayout.SOUTH);
        frame.add(controlsPanel, BorderLayout.EAST);

        // Add key listener for physical keyboard input
        frame.setFocusable(true);
        frame.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if (controller != null) {
                    controller.handleKeyPress(evt.getKeyChar());
                }
            }
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Update the board display
     */
    private void updateBoard() {
        // Clear the board
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                letterLabels[i][j].setText(" ");
                letterLabels[i][j].setBackground(Color.LIGHT_GRAY);
            }
        }

        // Set the first row to the start word
        String startWord = model.getStartWord().toUpperCase();
        for (int j = 0; j < 4; j++) {
            letterLabels[0][j].setText(String.valueOf(startWord.charAt(j)));
        }

        // Set the last row to the target word
        String targetWord = model.getTargetWord().toUpperCase();
        for (int j = 0; j < 4; j++) {
            letterLabels[5][j].setText(String.valueOf(targetWord.charAt(j)));
        }

        // Display attempts
        List<String> attempts = model.getAttempts();
        for (int i = 0; i < Math.min(attempts.size(), 4); i++) {
            String attempt = attempts.get(i).toUpperCase();
            int[] feedback = model.getFeedback(attempt.toLowerCase());

            for (int j = 0; j < 4; j++) {
                letterLabels[i + 1][j].setText(String.valueOf(attempt.charAt(j)));

                // Set background color based on feedback
                if (feedback[j] == 1) {
                    letterLabels[i + 1][j].setBackground(new Color(76, 175, 80)); // Green
                } else if (feedback[j] == 0) {
                    letterLabels[i + 1][j].setBackground(new Color(255, 235, 59)); // Yellow
                } else {
                    letterLabels[i + 1][j].setBackground(new Color(158, 158, 158)); // Grey
                }
            }
        }

        // Update status label
        statusLabel.setText("<html>Start: " + startWord + "<br>Target: " + targetWord + "</html>");

        // Update reset button
        resetButton.setEnabled(model.getCurrentAttempt() > 0);

        // Check if the game is won
        if (model.hasWon()) {
            showWinMessage();
        }

        // Show path if enabled
        if (model.isShowPath()) {
            showPath();
        }
    }

    /**
     * Update current input display
     * @param input the current input
     */
    public void updateCurrentInput(String input) {
        // Input row is based on the number of attempts
        int inputRow = Math.min(model.getCurrentAttempt() + 1, 4);

        // Clear input row
        for (int j = 0; j < 4; j++) {
            letterLabels[inputRow][j].setText(" ");
            letterLabels[inputRow][j].setBackground(Color.LIGHT_GRAY);
        }

        // Display current input
        for (int j = 0; j < input.length(); j++) {
            letterLabels[inputRow][j].setText(String.valueOf(input.charAt(j)).toUpperCase());
            letterLabels[inputRow][j].setBackground(Color.WHITE);
        }
    }

    /**
     * Show a win message
     */
    public void showWinMessage() {
        JOptionPane.showMessageDialog(frame,
                "Congratulations! You've successfully transformed " +
                        model.getStartWord().toUpperCase() + " into " + model.getTargetWord().toUpperCase() + " in " +
                        model.getCurrentAttempt() + " attempts.",
                "You Win!", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show the path from start to target
     */
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

    /**
     * Show an error message
     * @param message the message to show
     */
    public void showErrorMessage(String message) {
        if (model.isShowErrorMessages()) {
            JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Update the view when the model changes
     */
    @Override
    public void update(Observable o, Object arg) {
        updateBoard();
    }
}
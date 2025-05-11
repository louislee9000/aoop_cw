import java.util.List;
import java.util.Scanner;

public class CLI {
    private IModel model;
    private Scanner scanner;

    // Constructor - sets up the game environment with default settings
    public CLI() {
        model = new Model();
        scanner = new Scanner(System.in);

        // Set default game configuration flags
        model.setShowErrorMessages(true);
        model.setShowPath(false);
        model.setRandomWords(false);

        // Launch the game
        playGame();
    }

    private void playGame() {
        // Display welcome message and instructions
        System.out.println("Welcome to Weaver!");
        System.out.println("Change one letter at a time to transform the start word into the target word.");
        System.out.println("All intermediate steps must be valid words.");
        System.out.println("Type 'exit' to quit, 'restart' to reset the game, or 'new' for a new game.");
        System.out.println();

        printGameState();

        // Main game loop continues until player wins
        while (!model.hasWon()) {
            System.out.print("Enter a word: ");
            String input = scanner.nextLine().trim().toLowerCase();

            // Process special commands
            if (input.equals("exit")) {
                System.out.println("Thanks for playing!");
                break;
            } else if (input.equals("restart")) {
                model.resetGame();
                System.out.println("Game reset.");
                printGameState();
                continue;
            } else if (input.equals("new")) {
                model.setRandomWords(true);
                model.newGame();
                System.out.println("New game started.");
                printGameState();
                continue;
            } else if (input.startsWith("set ")) {
                // Handle game configuration commands
                handleFlagCommand(input.substring(4));
                continue;
            }

            // Validate word length (must be 4 letters)
            if (input.length() != 4) {
                if (model.isShowErrorMessages()) {
                    System.out.println("Error: Word must be 4 letters long");
                }
                continue;
            }

            // Check if the word is valid (exists in dictionary and differs by one letter)
            if (!model.isValidWord(input)) {
                if (model.isShowErrorMessages()) {
                    System.out.println("Error: Invalid word. It must be in the dictionary and differ by exactly one letter from the previous word.");
                }
                continue;
            }

            // Add valid word to the game
            model.submitWord(input);
            printGameState();

            // Handle win condition
            if (model.hasWon()) {
                showWinMessage();

                // Offer to play again
                System.out.print("Would you like to play again? (yes/no): ");
                input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("yes") || input.equals("y")) {
                    model.newGame();
                    printGameState();
                } else {
                    System.out.println("Thanks for playing!");
                    break;
                }
            }
        }

        scanner.close();
    }

    private void handleFlagCommand(String command) {
        // Parse the command into flag name and value
        String[] parts = command.split(" ");
        if (parts.length != 2) {
            System.out.println("Invalid command. Use 'set <flag> <value>'");
            return;
        }

        String flag = parts[0].toLowerCase();
        String value = parts[1].toLowerCase();
        boolean boolValue = value.equals("true") || value.equals("on") || value.equals("1");

        // Process different flag types
        switch (flag) {
            case "errors":
                // Toggle display of error messages
                model.setShowErrorMessages(boolValue);
                System.out.println("Show error messages: " + boolValue);
                break;
            case "path":
                // Toggle display of solution path
                model.setShowPath(boolValue);
                System.out.println("Show path: " + boolValue);
                if (boolValue) {
                    showPath();
                }
                break;
            case "random":
                // Toggle random word selection
                model.setRandomWords(boolValue);
                System.out.println("Random words: " + boolValue);
                if (boolValue) {
                    model.newGame();
                    printGameState();
                }
                break;
            default:
                // Handle unknown flag
                System.out.println("Unknown flag: " + flag);
                System.out.println("Available flags: errors, path, random");
                break;
        }
    }

    private void printGameState() {
        // Display current game information
        System.out.println("\n---------------------------");
        System.out.println("Start word: " + model.getStartWord().toUpperCase());
        System.out.println("Target word: " + model.getTargetWord().toUpperCase());
        System.out.println("---------------------------");

        // Show player's progress so far
        List<String> attempts = model.getAttempts();
        if (attempts.isEmpty()) {
            System.out.println("No attempts yet");
        } else {
            System.out.println("Your attempts:");
            for (int i = 0; i < attempts.size(); i++) {
                String attempt = attempts.get(i);
                System.out.print((i + 1) + ". " + attempt.toUpperCase() + " ");

                // Indicate correctness of each letter with color codes (G=correct, Y=wrong position, X=not in target)
                int[] feedback = model.getFeedback(attempt);
                System.out.print("[");
                for (int j = 0; j < 4; j++) {
                    char c = attempt.charAt(j);
                    if (feedback[j] == 1) {
                        System.out.print("G"); // Green - correct position
                    } else if (feedback[j] == 0) {
                        System.out.print("Y"); // Yellow - in word but wrong position
                    } else {
                        System.out.print("X"); // Grey - not in word
                    }
                }
                System.out.println("]");
            }
        }

        System.out.println("---------------------------");

        // Show optimal solution if enabled
        if (model.isShowPath()) {
            showPath();
        }
    }

    private void showWinMessage() {
        // Display victory message with statistics
        System.out.println("\n*******************************");
        System.out.println("* Congratulations! You won!   *");
        System.out.println("* You transformed " + model.getStartWord().toUpperCase() + " into " +
                model.getTargetWord().toUpperCase() + " *");
        System.out.println("* in " + model.getCurrentAttempt() + " attempts.              *");
        System.out.println("*******************************\n");
    }

    private void showPath() {
        // Find and display the optimal path between start and target words
        List<String> path = model.findPath();

        if (path.isEmpty()) {
            System.out.println("No path found from " + model.getStartWord().toUpperCase() +
                    " to " + model.getTargetWord().toUpperCase());
        } else {
            System.out.println("Path from " + model.getStartWord().toUpperCase() +
                    " to " + model.getTargetWord().toUpperCase() + ":");

            for (int i = 0; i < path.size(); i++) {
                System.out.println((i + 1) + ". " + path.get(i).toUpperCase());
            }

            System.out.println("---------------------------");
        }
    }

    // Program entry point
    public static void main(String[] args) {
        new CLI();
    }
}
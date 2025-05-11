import java.util.List;

public interface IModel {
    // Returns the starting word that player must transform
    String getStartWord();

    // Returns the target word that player needs to reach
    String getTargetWord();

    // Returns the current number of attempts made
    int getCurrentAttempt();

    // Processes a word submission and adds it to attempts if valid
    boolean submitWord(String word);

    // Validates if word exists in dictionary and differs by exactly one letter
    boolean isValidWord(String word);

    // Checks if player has reached the target word
    boolean hasWon();

    // Resets the game with the same start and target words
    void resetGame();

    // Starts a new game with different words
    void newGame();

    // Checks if error messages are enabled
    boolean isShowErrorMessages();

    // Enables or disables error messages
    void setShowErrorMessages(boolean showErrorMessages);

    // Checks if solution path display is enabled
    boolean isShowPath();

    // Enables or disables solution path display
    void setShowPath(boolean showPath);

    // Checks if random word selection is enabled
    boolean isRandomWords();

    // Enables or disables random word selection
    void setRandomWords(boolean randomWords);

    // Finds optimal solution path from start to target word
    List<String> findPath();

    // Returns list of all submitted words
    List<String> getAttempts();

    // Returns feedback array for word comparison (1=correct position, 0=wrong position, -1=not in word)
    int[] getFeedback(String word);
}
import java.util.List;

/**
 * Interface for the Weaver game model
 * This interface defines methods that the Model must implement
 */
public interface IModel {
    /**
     * Get the starting word
     * @return the starting word
     */
    String getStartWord();

    /**
     * Get the target word
     * @return the target word
     */
    String getTargetWord();

    /**
     * Get the current attempt number
     * @return the current attempt number
     */
    int getCurrentAttempt();

    /**
     * Submit a word as an attempt
     * @param word the word to submit
     * @return true if the word is valid and accepted, false otherwise
     */
    boolean submitWord(String word);

    /**
     * Check if a word is valid (exists in dictionary and differs by only one letter)
     * @param word the word to check
     * @return true if the word is valid, false otherwise
     */
    boolean isValidWord(String word);

    /**
     * Check if the player has won
     * @return true if the player has won, false otherwise
     */
    boolean hasWon();

    /**
     * Reset the game to its original state
     */
    void resetGame();

    /**
     * Start a new game
     */
    void newGame();

    /**
     * Check if error messages should be shown
     * @return true if error messages should be shown, false otherwise
     */
    boolean isShowErrorMessages();

    /**
     * Set whether error messages should be shown
     * @param showErrorMessages true if error messages should be shown, false otherwise
     */
    void setShowErrorMessages(boolean showErrorMessages);

    /**
     * Check if path should be shown
     * @return true if path should be shown, false otherwise
     */
    boolean isShowPath();

    /**
     * Set whether path should be shown
     * @param showPath true if path should be shown, false otherwise
     */
    void setShowPath(boolean showPath);

    /**
     * Check if random words should be used
     * @return true if random words should be used, false otherwise
     */
    boolean isRandomWords();

    /**
     * Set whether random words should be used
     * @param randomWords true if random words should be used, false otherwise
     */
    void setRandomWords(boolean randomWords);

    /**
     * Find a path from the start word to the target word
     * @return a list of words representing the path
     */
    List<String> findPath();

    /**
     * Get the list of attempts made so far
     * @return the list of attempts
     */
    List<String> getAttempts();

    /**
     * Get the feedback for a word
     * @param word the word to get feedback for
     * @return an array of feedback values (1 = correct, 0 = in word but wrong position, -1 = not in word)
     */
    int[] getFeedback(String word);
}
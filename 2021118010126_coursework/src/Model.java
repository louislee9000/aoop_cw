import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Model for the Weaver game.
 *
 * Class invariants:
 * - dictionary is not null and not empty
 * - startWord is not null and is 4 letters long
 * - targetWord is not null and is 4 letters long
 * - startWord and targetWord are different
 * - attempts is not null
 * - currentAttempt is equal to the size of attempts
 */
public class Model extends Observable implements IModel {
    private String startWord;
    private String targetWord;
    private List<String> dictionary;
    private int currentAttempt;
    private List<String> attempts;
    private boolean showErrorMessages;
    private boolean showPath;
    private boolean randomWords;

    // Default words if not using random words
    private static final String DEFAULT_START_WORD = "sale";
    private static final String DEFAULT_TARGET_WORD = "opal";

    /**
     * Constructor
     */
    public Model() {
        dictionary = new ArrayList<>();
        attempts = new ArrayList<>();
        loadDictionary();
        initializeGame(); // Changed from resetGame() to initializeGame()
    }

    /**
     * Load the dictionary from file
     */
    private void loadDictionary() {
        try (BufferedReader reader = new BufferedReader(new FileReader("dictionary.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Only add 4-letter words
                if (line.length() == 4) {
                    dictionary.add(line.toLowerCase());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading dictionary: " + e.getMessage());
        }

        // Assert that the dictionary is not empty
        assert !dictionary.isEmpty() : "Dictionary must not be empty";
    }

    /**
     * Get a random 4-letter word from the dictionary
     * @return a random word
     */
    private String getRandomWord() {
        List<String> fourLetterWords = new ArrayList<>();
        for (String word : dictionary) {
            if (word.length() == 4) {
                fourLetterWords.add(word);
            }
        }

        if (fourLetterWords.isEmpty()) {
            return DEFAULT_START_WORD; // Fallback
        }

        Random random = new Random();
        return fourLetterWords.get(random.nextInt(fourLetterWords.size()));
    }

    /**
     * Check if two words differ by exactly one letter
     * @param word1 the first word
     * @param word2 the second word
     * @return true if the words differ by one letter, false otherwise
     */
    private boolean isDifferByOneLetter(String word1, String word2) {
        // Precondition: words are not null and have the same length
        assert word1 != null && word2 != null : "Words cannot be null";
        assert word1.length() == word2.length() : "Words must have the same length";

        int differences = 0;
        for (int i = 0; i < word1.length(); i++) {
            if (word1.charAt(i) != word2.charAt(i)) {
                differences++;
            }
            if (differences > 1) {
                return false;
            }
        }

        return differences == 1;
    }

    @Override
    public String getStartWord() {
        return startWord;
    }

    @Override
    public String getTargetWord() {
        return targetWord;
    }

    @Override
    public int getCurrentAttempt() {
        return currentAttempt;
    }

    @Override
    public List<String> getAttempts() {
        return new ArrayList<>(attempts);
    }

    /**
     * @requires word != null
     * @requires word.length() == 4
     * @ensures if result == true then attempts.size() == \old(attempts.size()) + 1
     * @ensures if result == true then currentAttempt == \old(currentAttempt) + 1
     * @ensures if result == true then attempts.get(attempts.size()-1).equals(word)
     */
    @Override
    public boolean submitWord(String word) {
        // Precondition: word is not null
        assert word != null : "Word cannot be null";
        // Precondition: word is 4 letters long
        assert word.length() == 4 : "Word must be 4 letters long";

        word = word.toLowerCase();

        // Class invariant: dictionary is loaded
        assert !dictionary.isEmpty() : "Dictionary must be loaded";
        // Class invariant: start and target words are set
        assert startWord != null && targetWord != null : "Start and target words must be set";

        // Check if the word is valid
        if (!isValidWord(word)) {
            return false;
        }

        // Save old state for postcondition checking
        int oldSize = attempts.size();
        int oldAttempt = currentAttempt;

        // Add the word to the attempts
        attempts.add(word);
        currentAttempt++;

        // Notify observers
        setChanged();
        notifyObservers();

        // Postcondition: currentAttempt increased by 1
        assert currentAttempt == oldAttempt + 1 : "Current attempt should increase by 1";
        // Postcondition: attempts list size increased by 1
        assert attempts.size() == oldSize + 1 : "Attempts size should increase by 1";
        // Postcondition: last attempt is the submitted word
        assert attempts.get(attempts.size() - 1).equals(word) : "Last attempt should be the submitted word";

        return true;
    }

    /**
     * @requires word != null
     * @requires word.length() == 4
     * @ensures if result == true then word is in dictionary
     * @ensures if result == true && !attempts.isEmpty() then isDifferByOneLetter(attempts.get(attempts.size()-1), word)
     * @ensures if result == true && attempts.isEmpty() then isDifferByOneLetter(startWord, word)
     */
    @Override
    public boolean isValidWord(String word) {
        // Precondition: word is not null
        assert word != null : "Word cannot be null";
        // Precondition: word is 4 letters long
        assert word.length() == 4 : "Word must be 4 letters long";

        // Class invariant: dictionary is loaded
        assert !dictionary.isEmpty() : "Dictionary must be loaded";

        word = word.toLowerCase();

        // Check if the word is in the dictionary
        if (!dictionary.contains(word)) {
            return false;
        }

        // Check if the word differs by one letter from the last attempt or start word
        if (!attempts.isEmpty()) {
            String lastAttempt = attempts.get(attempts.size() - 1);
            return isDifferByOneLetter(lastAttempt, word);
        } else {
            // First attempt must differ by one letter from the start word
            return isDifferByOneLetter(startWord, word);
        }
    }

    @Override
    public boolean hasWon() {
        if (attempts.isEmpty()) {
            return false;
        }

        return attempts.get(attempts.size() - 1).equals(targetWord);
    }

    /**
     * Reset the game - clears attempts but keeps the same words
     * @ensures attempts.isEmpty()
     * @ensures currentAttempt == 0
     * @ensures startWord == \old(startWord)
     * @ensures targetWord == \old(targetWord)
     */
    @Override
    public void resetGame() {
        // Class invariant: dictionary is loaded
        assert !dictionary.isEmpty() : "Dictionary must be loaded";

        // Save current words
        String oldStartWord = startWord;
        String oldTargetWord = targetWord;

        attempts.clear();
        currentAttempt = 0;

        // Keep the same words - DO NOT regenerate them
        // This is the key fix for the reported issue

        setChanged();
        notifyObservers();

        // Postcondition: attempts list is empty
        assert attempts.isEmpty() : "Attempts list should be empty after reset";
        // Postcondition: currentAttempt is 0
        assert currentAttempt == 0 : "Current attempt should be 0 after reset";
        // Postcondition: words remain the same
        assert startWord == oldStartWord : "Start word should remain the same after reset";
        assert targetWord == oldTargetWord : "Target word should remain the same after reset";
    }

    /**
     * Start a new game - may generate new words if in random mode
     * @ensures attempts.isEmpty()
     * @ensures currentAttempt == 0
     * @ensures startWord != null
     * @ensures targetWord != null
     * @ensures !startWord.equals(targetWord)
     */
    @Override
    public void newGame() {
        // Class invariant: dictionary is loaded
        assert !dictionary.isEmpty() : "Dictionary must be loaded";

        attempts.clear();
        currentAttempt = 0;

        if (randomWords) {
            startWord = getRandomWord();

            // Make sure the target word is different from the start word
            do {
                targetWord = getRandomWord();
            } while (targetWord.equals(startWord));
        } else {
            startWord = DEFAULT_START_WORD;
            targetWord = DEFAULT_TARGET_WORD;
        }

        setChanged();
        notifyObservers();

        // Postcondition: attempts list is empty
        assert attempts.isEmpty() : "Attempts list should be empty after new game";
        // Postcondition: currentAttempt is 0
        assert currentAttempt == 0 : "Current attempt should be 0 after new game";
        // Postcondition: start and target words are set
        assert startWord != null && targetWord != null : "Start and target words must be set after new game";
        // Postcondition: start and target words are different
        assert !startWord.equals(targetWord) : "Start and target words must be different";
    }

    /**
     * Initialize the game - used only in constructor
     * Sets up initial words based on randomWords setting
     */
    private void initializeGame() {
        attempts.clear();
        currentAttempt = 0;

        if (randomWords) {
            startWord = getRandomWord();

            // Make sure the target word is different from the start word
            do {
                targetWord = getRandomWord();
            } while (targetWord.equals(startWord));
        } else {
            startWord = DEFAULT_START_WORD;
            targetWord = DEFAULT_TARGET_WORD;
        }
    }

    @Override
    public boolean isShowErrorMessages() {
        return showErrorMessages;
    }

    @Override
    public void setShowErrorMessages(boolean showErrorMessages) {
        this.showErrorMessages = showErrorMessages;
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean isShowPath() {
        return showPath;
    }

    @Override
    public void setShowPath(boolean showPath) {
        this.showPath = showPath;
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean isRandomWords() {
        return randomWords;
    }

    @Override
    public void setRandomWords(boolean randomWords) {
        this.randomWords = randomWords;
        newGame(); // When changing this setting, start a new game
    }

    /**
     * @requires dictionary != null && !dictionary.isEmpty()
     * @requires startWord != null && targetWord != null
     * @ensures if !result.isEmpty() then result.get(0).equals(startWord)
     * @ensures if !result.isEmpty() then result.get(result.size()-1).equals(targetWord)
     */
    @Override
    public List<String> findPath() {
        // Class invariant: dictionary is loaded
        assert !dictionary.isEmpty() : "Dictionary must be loaded";
        // Class invariant: start and target words are set
        assert startWord != null && targetWord != null : "Start and target words must be set";

        // Breadth-first search to find a path from startWord to targetWord
        Queue<String> queue = new LinkedList<>();
        Map<String, String> parents = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(startWord);
        visited.add(startWord);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            // If we've reached the target, reconstruct the path
            if (current.equals(targetWord)) {
                List<String> path = new ArrayList<>();
                String word = targetWord;
                while (word != null) {
                    path.add(0, word);
                    word = parents.get(word);
                }

                // Postcondition: path starts with startWord and ends with targetWord
                assert path.get(0).equals(startWord) : "Path should start with start word";
                assert path.get(path.size() - 1).equals(targetWord) : "Path should end with target word";

                return path;
            }

            // Try changing each letter to find neighbors
            for (int i = 0; i < current.length(); i++) {
                for (char c = 'a'; c <= 'z'; c++) {
                    if (current.charAt(i) == c) {
                        continue; // Same letter, no change
                    }

                    StringBuilder neighbor = new StringBuilder(current);
                    neighbor.setCharAt(i, c);
                    String neighborStr = neighbor.toString();

                    // Check if the neighbor is a valid word and hasn't been visited
                    if (dictionary.contains(neighborStr) && !visited.contains(neighborStr)) {
                        queue.add(neighborStr);
                        visited.add(neighborStr);
                        parents.put(neighborStr, current);
                    }
                }
            }
        }

        // No path found
        return Collections.emptyList();
    }

    @Override
    public int[] getFeedback(String word) {
        int[] feedback = new int[4];

        for (int i = 0; i < 4; i++) {
            if (word.charAt(i) == targetWord.charAt(i)) {
                feedback[i] = 1; // Correct letter in correct position (green)
            } else if (targetWord.indexOf(word.charAt(i)) != -1) {
                feedback[i] = 0; // Letter exists in target word but in wrong position
            } else {
                feedback[i] = -1; // Letter does not exist in target word (grey)
            }
        }

        return feedback;
    }
}

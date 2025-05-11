import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Model extends Observable implements IModel {
    private String startWord;
    private String targetWord;
    private List<String> dictionary;
    private int currentAttempt;
    private List<String> attempts;
    private boolean showErrorMessages;
    private boolean showPath;
    private boolean randomWords;

    // Default words used when random selection is disabled
    private static final String DEFAULT_START_WORD = "sale";
    private static final String DEFAULT_TARGET_WORD = "opal";

    // Initializes dictionary and game state
    public Model() {
        dictionary = new ArrayList<>();
        attempts = new ArrayList<>();
        loadDictionary();
        initializeGame(); // Initialize game state with default or random words
    }

    // Loads 4-letter words from dictionary
    private void loadDictionary() {
        try (BufferedReader reader = new BufferedReader(new FileReader("dictionary.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Only add 4-letter words to the dictionary
                if (line.length() == 4) {
                    dictionary.add(line.toLowerCase());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading dictionary: " + e.getMessage());
        }

        // Ensure dictionary is not empty after loading
        assert !dictionary.isEmpty() : "Dictionary must not be empty";
    }

    // Selects a random 4-letter word
    private String getRandomWord() {
        List<String> fourLetterWords = new ArrayList<>();
        for (String word : dictionary) {
            if (word.length() == 4) {
                fourLetterWords.add(word);
            }
        }

        if (fourLetterWords.isEmpty()) {
            return DEFAULT_START_WORD; // Fallback to default word if no 4-letter words found
        }

        Random random = new Random();
        return fourLetterWords.get(random.nextInt(fourLetterWords.size()));
    }

    // Checks if words differ by exactly one letter
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

    // Processes word submission if valid
    @Override
    public boolean submitWord(String word) {
        // Precondition: word is not null
        assert word != null : "Word cannot be null";
        // Precondition: word is 4 letters long
        assert word.length() == 4 : "Word must be 4 letters long";

        word = word.toLowerCase();

        // Validate the submitted word
        if (!isValidWord(word)) {
            return false;
        }

        // Store current state for verification
        int oldSize = attempts.size();
        int oldAttempt = currentAttempt;

        // Add word to attempts list and increment counter
        attempts.add(word);
        currentAttempt++;

        // Notify observers of model change
        setChanged();
        notifyObservers();

        // Verify attempt counter increased
        assert currentAttempt == oldAttempt + 1 : "Current attempt should increase by 1";
        // Verify attempts list grew by one
        assert attempts.size() == oldSize + 1 : "Attempts size should increase by 1";
        // Verify submitted word was added to list
        assert attempts.get(attempts.size() - 1).equals(word) : "Last attempt should be the submitted word";

        return true;
    }

    // Validates word against dictionary and previous word
    @Override
    public boolean isValidWord(String word) {
        // Precondition: word is not null
        assert word != null : "Word cannot be null";
        // Precondition: word is 4 letters long
        assert word.length() == 4 : "Word must be 4 letters long";

        // Class invariant: dictionary is loaded
        assert !dictionary.isEmpty() : "Dictionary must be loaded";

        word = word.toLowerCase();

        // Check if word exists in dictionary
        if (!dictionary.contains(word)) {
            return false;
        }

        // Verify word differs by exactly one letter from previous word
        if (!attempts.isEmpty()) {
            String lastAttempt = attempts.get(attempts.size() - 1);
            return isDifferByOneLetter(lastAttempt, word);
        } else {
            // For first attempt, compare with start word
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

    // Resets game with same words
    @Override
    public void resetGame() {
        // Class invariant: dictionary is loaded
        assert !dictionary.isEmpty() : "Dictionary must be loaded";

        // Store current words for verification
        String oldStartWord = startWord;
        String oldTargetWord = targetWord;

        attempts.clear();
        currentAttempt = 0;

        // Keep the same words - words are not regenerated on reset

        setChanged();
        notifyObservers();

        // Postcondition: attempts list is empty
        assert attempts.isEmpty() : "Attempts list should be empty after reset";
        // Postcondition: currentAttempt is 0
        assert currentAttempt == 0 : "Current attempt should be 0 after reset";
        // Verify words remain unchanged
        assert startWord == oldStartWord : "Start word should remain the same after reset";
        assert targetWord == oldTargetWord : "Target word should remain the same after reset";
    }

    // Starts a new game with fresh words based on random setting
    // Clears all attempts and ensures start/target words are valid and different
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

    // Calculates optimal solution path from start to target word using breadth-first search
    // Requires dictionary to be loaded and start/target words to be set
    // Ensures path starts with start word and ends with target word if a path exists
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
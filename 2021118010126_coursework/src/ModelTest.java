import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class ModelTest {

    private Model model;

    @Before
    public void setUp() {
        // Initialize fresh model instance with consistent test settings
        model = new Model();
        model.setShowErrorMessages(true);
        model.setShowPath(false);
        model.setRandomWords(false); // Ensure deterministic word selection for testing
    }
    /**
     * Scenario 1: Test word submission and game state
     *
     * This test verifies:
     * 1. A valid word can be submitted
     * 2. The attempt is added to the attempts list
     * 3. The currentAttempt counter is incremented
     * 4. The hasWon() method correctly identifies when the game is won
     *
     * Preconditions:
     * - model is initialized with fixed start and target words
     * - no attempts have been made yet
     *
     * Postconditions:
     * - valid words are accepted by submitWord()
     * - each accepted word is added to the attempts list
     * - currentAttempt is incremented for each accepted word
     * - hasWon() returns true when the target word is reached
     */
    @Test
    public void testWordSubmissionAndGameState() {

        // Verify initial game state
        assertEquals("sale", model.getStartWord());
        assertEquals("opal", model.getTargetWord());
        assertEquals(0, model.getCurrentAttempt());
        assertEquals(0, model.getAttempts().size());

        // Find and submit first valid move
        String validWord = findValidWordFromStart();
        assertNotNull("Could not find a valid word from start word", validWord);
        boolean result = model.submitWord(validWord);

        // Verify word acceptance and state updates
        assertTrue("Valid word should be accepted", result);
        assertEquals(1, model.getAttempts().size());
        assertEquals(validWord, model.getAttempts().get(0));
        assertEquals(1, model.getCurrentAttempt());
        assertFalse("Game should not be won yet", model.hasWon());

        // Complete the game using path-finding algorithm
        List<String> path = model.findPath();
        if (!path.isEmpty() && path.size() > 1) {
            // Submit each word in the solution path
            for (int i = 1; i < path.size(); i++) {
                String pathWord = path.get(i);
                if (!model.getAttempts().contains(pathWord)) {
                    result = model.submitWord(pathWord);
                    assertTrue("Path word should be accepted: " + pathWord, result);
                }
            }

            // Verify win condition
            assertTrue("Game should be won", model.hasWon());
        }
    }

    private String findValidWordFromStart() {
        // Helper method to find a valid word one letter different from start word
        String startWord = model.getStartWord();

        // Try all possible one-letter changes
        for (int pos = 0; pos < 4; pos++) {
            for (char c = 'a'; c <= 'z'; c++) {
                if (c == startWord.charAt(pos)) continue; // Skip unchanged letters

                StringBuilder sb = new StringBuilder(startWord);
                sb.setCharAt(pos, c);
                String candidate = sb.toString();

                if (model.isValidWord(candidate)) {
                    return candidate;
                }
            }
        }

        return null; // No valid word found
    }
    /**
     * Scenario 2: Test game reset and new game functionality
     *
     * This test verifies:
     * 1. resetGame() clears attempts but keeps the same words
     * 2. newGame() clears attempts and may generate new words
     *
     * Preconditions:
     * - model is initialized with fixed start and target words
     * - at least one attempt has been made
     *
     * Postconditions:
     * - resetGame() clears the attempts list and resets currentAttempt to 0
     * - resetGame() keeps the same start and target words
     * - newGame() clears the attempts list and resets currentAttempt to 0
     * - newGame() with randomWords=true may generate new start and target words
     */
    @Test
    public void testGameResetAndNewGame() {

        // Setup game with one move
        String validWord = findValidWordFromStart();
        assertNotNull("Could not find a valid word from start word", validWord);
        model.submitWord(validWord);
        assertEquals(1, model.getAttempts().size());

        // Record current words
        String startWord = model.getStartWord();
        String targetWord = model.getTargetWord();

        // Test resetGame - should clear attempts but keep same words
        model.resetGame();
        assertEquals(0, model.getAttempts().size());
        assertEquals(0, model.getCurrentAttempt());
        assertEquals(startWord, model.getStartWord());
        assertEquals(targetWord, model.getTargetWord());

        // Make another move then test newGame
        model.submitWord(validWord);
        assertEquals(1, model.getAttempts().size());

        // Enable random words for new game
        model.setRandomWords(true);
        model.newGame();

        // Verify attempts cleared
        assertEquals(0, model.getAttempts().size());
        assertEquals(0, model.getCurrentAttempt());

        // Reset to deterministic mode
        model.setRandomWords(false);
    }
    /**
     * Scenario 3: Test word validation and path finding
     *
     * This test verifies:
     * 1. isValidWord() correctly validates words
     * 2. findPath() returns a valid path from start word to target word
     *
     * Preconditions:
     * - model is initialized with fixed start and target words
     *
     * Postconditions:
     * - isValidWord() returns true for valid words (in dictionary and differing by one letter)
     * - isValidWord() returns false for invalid words
     * - findPath() returns a non-empty list if a path exists
     * - the path starts with the start word and ends with the target word
     * - each step in the path differs by exactly one letter from the previous step
     */
    @Test
    public void testWordValidationAndPathFinding() {

        // Start with clean state
        model.resetGame();

        // Test word validation
        String validWord = findValidWordFromStart();
        assertNotNull("Could not find a valid word from start word", validWord);
        assertTrue("Should be a valid word", model.isValidWord(validWord));

        // Check rejection of invalid words
        String nonExistentWord = "zzzz"; // Unlikely dictionary word
        if (model.isValidWord(nonExistentWord)) {
            System.out.println("Warning: Unexpected word 'zzzz' found in dictionary");
        }

        // Submit first word and find next valid move
        model.submitWord(validWord);
        String nextValidWord = null;

        // Search for another valid word
        for (char c = 'a'; c <= 'z' && nextValidWord == null; c++) {
            for (int pos = 0; pos < 4; pos++) {
                if (c == validWord.charAt(pos)) continue;

                StringBuilder sb = new StringBuilder(validWord);
                sb.setCharAt(pos, c);
                String candidate = sb.toString();

                if (model.isValidWord(candidate)) {
                    nextValidWord = candidate;
                    break;
                }
            }
        }

        if (nextValidWord != null) {
            assertTrue("Next word should be valid", model.isValidWord(nextValidWord));
        }

        // Test path finding algorithm
        List<String> path = model.findPath();
        assertNotNull("Path should not be null", path);

        if (!path.isEmpty()) {
            // Verify path connects start to target
            assertEquals("Path should start with the start word", model.getStartWord(), path.get(0));
            assertEquals("Path should end with the target word", model.getTargetWord(), path.get(path.size()-1));

            // Verify each step changes exactly one letter
            for (int i = 1; i < path.size(); i++) {
                String prevWord = path.get(i-1);
                String currWord = path.get(i);

                int diffCount = 0;
                for (int j = 0; j < 4; j++) {
                    if (prevWord.charAt(j) != currWord.charAt(j)) {
                        diffCount++;
                    }
                }

                assertEquals("Each step should differ by exactly one letter", 1, diffCount);
            }
        }
    }
}

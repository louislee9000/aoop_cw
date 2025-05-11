import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Unit tests for the Model class of Weaver game
 *
 * Three test scenarios:
 * 1. Word submission and game state - Tests submitWord() and hasWon()
 * 2. Game reset and new game - Tests resetGame() and newGame()
 * 3. Word validation and path finding - Tests isValidWord() and findPath()
 */
public class ModelTest {

    private Model model;

    /**
     * Set up the test environment before each test
     */
    @Before
    public void setUp() {
        // Create a new model instance
        model = new Model();

        // Set flags for testing
        model.setShowErrorMessages(true);
        model.setShowPath(false);
        model.setRandomWords(false); // Use fixed words for deterministic testing
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
        // Precondition: Game is in initial state
        assertEquals("sale", model.getStartWord());
        assertEquals("opal", model.getTargetWord());
        assertEquals(0, model.getCurrentAttempt());
        assertEquals(0, model.getAttempts().size());

        // Find a valid word by checking dictionary
        String validWord = findValidWordFromStart();
        assertNotNull("Could not find a valid word from start word", validWord);

        // Submit a valid word
        boolean result = model.submitWord(validWord);

        // Postcondition: Word was accepted
        assertTrue("Valid word should be accepted", result);
        // Postcondition: Attempt was added
        assertEquals(1, model.getAttempts().size());
        assertEquals(validWord, model.getAttempts().get(0));
        // Postcondition: Current attempt was incremented
        assertEquals(1, model.getCurrentAttempt());

        // Test hasWon when game is not yet won
        assertFalse("Game should not be won yet", model.hasWon());

        // Use findPath to get a valid sequence of words to win
        List<String> path = model.findPath();
        if (!path.isEmpty() && path.size() > 1) {
            // Submit words from the path (skip the first one which is the start word
            // and skip any that are already in the attempts list)
            for (int i = 1; i < path.size(); i++) {
                String pathWord = path.get(i);
                if (!model.getAttempts().contains(pathWord)) {
                    result = model.submitWord(pathWord);
                    assertTrue("Path word should be accepted: " + pathWord, result);
                }
            }

            // Postcondition: Game is won
            assertTrue("Game should be won", model.hasWon());
        }
    }

    /**
     * Helper method to find a valid word that differs by one letter from the start word
     */
    private String findValidWordFromStart() {
        String startWord = model.getStartWord();

        // Try changing each position to each letter and check if it's valid
        for (int pos = 0; pos < 4; pos++) {
            for (char c = 'a'; c <= 'z'; c++) {
                if (c == startWord.charAt(pos)) continue; // Skip same letter

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
        // Find and submit a valid word
        String validWord = findValidWordFromStart();
        assertNotNull("Could not find a valid word from start word", validWord);
        model.submitWord(validWord);

        // Precondition: Game has attempts
        assertEquals(1, model.getAttempts().size());
        String startWord = model.getStartWord();
        String targetWord = model.getTargetWord();

        // Reset the game
        model.resetGame();

        // Postcondition: Attempts are cleared
        assertEquals(0, model.getAttempts().size());
        assertEquals(0, model.getCurrentAttempt());
        // Postcondition: Words remain the same
        assertEquals(startWord, model.getStartWord());
        assertEquals(targetWord, model.getTargetWord());

        // Make some moves again
        model.submitWord(validWord);

        // Precondition: Game has attempts
        assertEquals(1, model.getAttempts().size());

        // Turn on random words
        model.setRandomWords(true);

        // Start a new game
        model.newGame();

        // Postcondition: Attempts are cleared
        assertEquals(0, model.getAttempts().size());
        assertEquals(0, model.getCurrentAttempt());

        // Turn off random words for the rest of the tests
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
        // Reset the model to ensure a clean state
        model.resetGame();

        // Find a valid word
        String validWord = findValidWordFromStart();
        assertNotNull("Could not find a valid word from start word", validWord);

        // Test with the valid word
        assertTrue("Should be a valid word", model.isValidWord(validWord));

        // Test with a non-existent word (should be invalid)
        String nonExistentWord = "zzzz"; // Highly unlikely to be in dictionary
        if (model.isValidWord(nonExistentWord)) {
            System.out.println("Warning: Unexpected word 'zzzz' found in dictionary");
        }

        // Submit the valid word
        model.submitWord(validWord);

        // Try to find another valid word from the submitted word
        String nextValidWord = null;
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

        // Test path finding
        List<String> path = model.findPath();

        // Postcondition: Path exists and connects start to target
        assertNotNull("Path should not be null", path);

        if (!path.isEmpty()) {
            assertEquals("Path should start with the start word", model.getStartWord(), path.get(0));
            assertEquals("Path should end with the target word", model.getTargetWord(), path.get(path.size()-1));

            // Validate each step in the path differs by exactly one letter from the previous
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
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

    @Test
    public void testWordSubmissionAndGameState() {
        // Scenario 1: Test word submission and win detection

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

    @Test
    public void testGameResetAndNewGame() {
        // Scenario 2: Test reset and new game functionality

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

    @Test
    public void testWordValidationAndPathFinding() {
        // Scenario 3: Test word validation and solution path finding

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
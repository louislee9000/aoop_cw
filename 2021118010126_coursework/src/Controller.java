import java.util.Observable;
import java.util.Observer;

public class Controller implements Observer {
    private IModel model;
    private View view;
    private StringBuilder currentWord;

    // Constructor - connects controller to model and view components
    public Controller(IModel model, View view) {
        this.model = model;
        this.view = view;
        this.currentWord = new StringBuilder();
    }

    public void handleKeyPress(char key) {
        if (key == '\b') {
            // Remove last character when backspace is pressed
            if (currentWord.length() > 0) {
                currentWord.deleteCharAt(currentWord.length() - 1);
            }
        } else if (Character.isLetter(key)) {
            // Add letters to input (max 4 characters)
            if (currentWord.length() < 4) {
                currentWord.append(Character.toLowerCase(key));
            }
        }

        // Reflect changes in the view
        view.updateCurrentInput(currentWord.toString());
    }

    public void handleSubmit() {
        // Validate word length
        if (currentWord.length() != 4) {
            view.showErrorMessage("Word must be 4 letters long");
            return;
        }

        String word = currentWord.toString().toLowerCase();

        // Check word validity against game rules
        if (!model.isValidWord(word)) {
            view.showErrorMessage("Invalid word: " + word +
                    "\nWord must be in the dictionary and differ by exactly one letter from the previous word.");
            return;
        }

        // Submit valid word and clear input field
        model.submitWord(word);
        currentWord.setLength(0);
        view.updateCurrentInput(currentWord.toString());

        updateButtonStates();
    }

    // Reset current game to starting state
    public void handleReset() {
        model.resetGame();
        currentWord.setLength(0);
        view.updateCurrentInput(currentWord.toString());

        updateButtonStates();
    }

    // Start a fresh game with new words
    public void handleNewGame() {
        model.newGame();
        currentWord.setLength(0);
        view.updateCurrentInput(currentWord.toString());

        updateButtonStates();
    }

    // Process changes to game configuration options
    public void handleFlagChange(String flagName, boolean value) {
        switch (flagName) {
            case "showErrorMessages":
                model.setShowErrorMessages(value);
                break;
            case "showPath":
                model.setShowPath(value);
                break;
            case "randomWords":
                model.setRandomWords(value);
                break;
        }

        updateButtonStates();
    }

    // Refresh UI elements based on current game state
    public void updateButtonStates() {
        view.setResetButtonEnabled(model.getCurrentAttempt() > 0);
        view.setNewGameButtonEnabled(true);
    }

    // Observer pattern implementation - responds to model changes
    @Override
    public void update(Observable o, Object arg) {
        updateButtonStates();
    }
}
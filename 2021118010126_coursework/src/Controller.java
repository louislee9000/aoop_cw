/**
 * Controller component for the Weaver game GUI
 */
public class Controller {
    private IModel model;
    private View view;
    private StringBuilder currentWord;

    /**
     * Constructor
     * @param model the model
     * @param view the view
     */
    public Controller(IModel model, View view) {
        this.model = model;
        this.view = view;
        this.currentWord = new StringBuilder();
    }

    /**
     * Handle a key press
     * @param key the key pressed
     */
    public void handleKeyPress(char key) {
        if (key == '\b') {
            // Backspace - delete the last character
            if (currentWord.length() > 0) {
                currentWord.deleteCharAt(currentWord.length() - 1);
            }
        } else if (Character.isLetter(key)) {
            // Letter key - add to current word if not full
            if (currentWord.length() < 4) {
                currentWord.append(Character.toLowerCase(key));
            }
        }

        // Update the current input in the view
        view.updateCurrentInput(currentWord.toString());
    }

    /**
     * Handle a submit action
     */
    public void handleSubmit() {
        if (currentWord.length() != 4) {
            view.showErrorMessage("Word must be 4 letters long");
            return;
        }

        String word = currentWord.toString().toLowerCase();

        if (!model.isValidWord(word)) {
            view.showErrorMessage("Invalid word: " + word +
                    "\nWord must be in the dictionary and differ by exactly one letter from the previous word.");
            return;
        }

        model.submitWord(word);
        currentWord.setLength(0);
        view.updateCurrentInput(currentWord.toString());
    }

    /**
     * Handle a reset action
     */
    public void handleReset() {
        model.resetGame();
        currentWord.setLength(0);
        view.updateCurrentInput(currentWord.toString());
    }

    /**
     * Handle a new game action
     */
    public void handleNewGame() {
        model.newGame();
        currentWord.setLength(0);
        view.updateCurrentInput(currentWord.toString());
    }

    /**
     * Handle a flag change
     * @param flagName the name of the flag
     * @param value the new value
     */
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
    }
}
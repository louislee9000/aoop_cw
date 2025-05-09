/**
 * Main class for the GUI version of the Weaver game
 */
public class GUIMain {
    /**
     * Main method
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Create the model, view, and controller
        Model model = new Model();
        View view = new View(model);
        Controller controller = new Controller(model, view);

        // Set up observers and relationships
        model.addObserver(view);
        view.setController(controller);

        // Set initial flag values
        model.setShowErrorMessages(true);
        model.setShowPath(false);
        model.setRandomWords(false);
    }
}
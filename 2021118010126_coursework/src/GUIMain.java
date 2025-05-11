public class GUIMain {
    public static void main(String[] args) {
        // Initialize the Model-View-Controller components
        Model model = new Model();
        View view = new View(model);
        Controller controller = new Controller(model, view);

        // Connect components using Observer pattern
        model.addObserver(view);
        model.addObserver(controller);
        view.setController(controller);

        // Configure initial game preferences
        model.setShowErrorMessages(true);
        model.setShowPath(false);
        model.setRandomWords(false);

        // Initialize UI element states
        controller.updateButtonStates();
    }
}
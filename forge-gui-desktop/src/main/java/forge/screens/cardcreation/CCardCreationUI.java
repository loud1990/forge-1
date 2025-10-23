package forge.screens.cardcreation;

import forge.Singletons;
import forge.gui.cardcreation.CardCreationData;
import forge.gui.cardcreation.CardCreationService;
import forge.gui.cardcreation.CardCreationType;
import forge.gui.framework.FScreen;
import forge.gui.framework.ICDoc;
import forge.toolbox.FOptionPane;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Controller for the Card Creator screen.
 * Handles user interactions and business logic for custom card creation.
 *
 * <br><br><i>(C at beginning of class name denotes a controller class.)</i>
 */
public enum CCardCreationUI implements ICDoc {
    SINGLETON_INSTANCE;

    private final VCardCreationUI view = VCardCreationUI.SINGLETON_INSTANCE;
    private final CardCreationData cardData = new CardCreationData();

    // Stub service implementation for now
    private final CardCreationService service = new CardCreationService() {
        @Override
        public CreationResult createCard(CardCreationData data) {
            // TODO: This will be implemented to actually create the card script
            // For now, just validate and show success
            List<String> errors = data.validate();
            if (!errors.isEmpty()) {
                return CreationResult.failure("Validation failed", String.join("\n", errors));
            }

            // Simulate success
            return CreationResult.success("Card '" + data.getName() + "' created successfully!\n" +
                    "(Note: Backend integration not yet implemented)");
        }

        @Override
        public CreationResult validateCard(CardCreationData data) {
            List<String> errors = data.validate();
            if (!errors.isEmpty()) {
                return CreationResult.failure("Validation failed", String.join("\n", errors));
            }
            return CreationResult.success("Validation passed");
        }
    };

    @Override
    public void register() {
        // Register for layout system if needed
        // For a simple form-based screen, this can be empty
    }

    @Override
    public void initialize() {
        // Set up event listeners
        setupListeners();

        // Initialize form state
        updateFieldStates();
    }

    @Override
    public void update() {
        // Refresh the UI state if needed
        updateFieldStates();
    }

    /**
     * Sets up all event listeners for the form components.
     */
    private void setupListeners() {
        // Card type selection changes field availability
        view.getCmbCardType().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateFieldStates();
            }
        });

        // Create button
        view.getBtnCreate().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCreate();
            }
        });

        // Clear button
        view.getBtnClear().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleClear();
            }
        });

        // Cancel button
        view.getBtnCancel().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCancel();
            }
        });
    }

    /**
     * Updates field enabled/disabled states based on card type selection.
     */
    private void updateFieldStates() {
        CardCreationType selectedType = (CardCreationType) view.getCmbCardType().getSelectedItem();

        if (selectedType != null) {
            // Enable/disable power and toughness based on card type
            boolean needsPowerToughness = selectedType.requiresPowerToughness();
            view.getTxtPower().setEnabled(needsPowerToughness);
            view.getTxtToughness().setEnabled(needsPowerToughness);

            // Enable/disable loyalty based on card type
            boolean needsLoyalty = selectedType.requiresLoyalty();
            view.getTxtLoyalty().setEnabled(needsLoyalty);
        }
    }

    /**
     * Handles the Create button action.
     */
    private void handleCreate() {
        // Collect form data
        collectFormData();

        // Validate and create card
        CardCreationService.CreationResult result = service.createCard(cardData);

        if (result.isSuccess()) {
            FOptionPane.showMessageDialog(
                    result.getMessage(),
                    "Success",
                    FOptionPane.INFORMATION_ICON);

            // Clear form after successful creation
            handleClear();
        } else {
            String errorMessage = result.getMessage();
            if (result.getErrorDetails() != null && !result.getErrorDetails().isEmpty()) {
                errorMessage += "\n\nDetails:\n" + result.getErrorDetails();
            }

            FOptionPane.showMessageDialog(
                    errorMessage,
                    "Error",
                    FOptionPane.ERROR_ICON);
        }
    }

    /**
     * Collects data from form fields into the CardCreationData model.
     */
    private void collectFormData() {
        cardData.setName(view.getTxtCardName().getText());
        cardData.setManaCost(view.getTxtManaCost().getText());
        cardData.setCardType((CardCreationType) view.getCmbCardType().getSelectedItem());
        cardData.setSubtype(view.getTxtSubtype().getText());
        cardData.setAbilities(view.getTxtAbilities().getText());
        cardData.setPower(view.getTxtPower().getText());
        cardData.setToughness(view.getTxtToughness().getText());
        cardData.setLoyalty(view.getTxtLoyalty().getText());
    }

    /**
     * Handles the Clear button action.
     */
    private void handleClear() {
        view.getTxtCardName().setText("");
        view.getTxtManaCost().setText("");
        view.getCmbCardType().setSelectedIndex(0);
        view.getTxtSubtype().setText("");
        view.getTxtAbilities().setText("");
        view.getTxtPower().setText("");
        view.getTxtToughness().setText("");
        view.getTxtLoyalty().setText("");

        cardData.clear();
        updateFieldStates();
    }

    /**
     * Handles the Cancel button action.
     */
    private void handleCancel() {
        // Return to home screen
        Singletons.getControl().setCurrentScreen(FScreen.HOME_SCREEN);
    }
}

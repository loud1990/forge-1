package forge.screens.cardcreation;

import forge.Singletons;
import forge.gui.cardcreation.CardCreationType;
import forge.gui.framework.FScreen;
import forge.gui.framework.IVTopLevelUI;
import forge.toolbox.*;
import forge.util.Localizer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Top level view class for the Card Creator screen.
 * Provides a form for creating custom Magic: The Gathering cards.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 */
public enum VCardCreationUI implements IVTopLevelUI {
    SINGLETON_INSTANCE;

    private final Localizer localizer = Localizer.getInstance();

    // Main panel and scroll pane
    private final JPanel pnlMain = new JPanel();
    private final FScrollPane scrContent = new FScrollPane(pnlMain, false,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    // Form components - Basic Info
    private final FLabel lblCardName = new FLabel.Builder().text("Card Name:").build();
    private final FTextField txtCardName = new FTextField.Builder().build();

    private final FLabel lblManaCost = new FLabel.Builder().text("Mana Cost:").build();
    private final FTextField txtManaCost = new FTextField.Builder().build();

    // Form components - Type Line
    private final FLabel lblCardType = new FLabel.Builder().text("Card Type:").build();
    private final FComboBox<CardCreationType> cmbCardType = new FComboBox<>();

    private final FLabel lblSubtype = new FLabel.Builder().text("Subtype:").build();
    private final FTextField txtSubtype = new FTextField.Builder().build();

    // Form components - Card Text
    private final FLabel lblAbilities = new FLabel.Builder().text("Abilities / Card Text:").build();
    private final FTextArea txtAbilities = new FTextArea();
    private final FScrollPane scrAbilities = new FScrollPane(txtAbilities, true,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // Form components - Stats
    private final FLabel lblPower = new FLabel.Builder().text("Power:").build();
    private final FTextField txtPower = new FTextField.Builder().build();

    private final FLabel lblToughness = new FLabel.Builder().text("Toughness:").build();
    private final FTextField txtToughness = new FTextField.Builder().build();

    private final FLabel lblLoyalty = new FLabel.Builder().text("Loyalty:").build();
    private final FTextField txtLoyalty = new FTextField.Builder().build();

    // Action buttons
    private final FButton btnCreate = new FButton("Create Card");
    private final FButton btnClear = new FButton("Clear");
    private final FButton btnCancel = new FButton("Cancel");

    // Constructor
    VCardCreationUI() {
        pnlMain.setLayout(new MigLayout("insets 20, gap 10, fillx", "[right][grow,fill]", ""));

        // Populate card type combo box
        for (CardCreationType type : CardCreationType.values()) {
            cmbCardType.addItem(type);
        }

        layoutForm();
    }

    /**
     * Lays out the form components using MigLayout.
     */
    private void layoutForm() {
        // Title
        FLabel lblTitle = new FLabel.Builder()
                .text("Custom Card Creator")
                .fontSize(16)
                .build();
        pnlMain.add(lblTitle, "span 2, align center, gapbottom 15, wrap");

        // Basic Info Section
        addSectionHeader("Basic Information");
        pnlMain.add(lblCardName);
        pnlMain.add(txtCardName, "growx, wrap");

        pnlMain.add(lblManaCost);
        pnlMain.add(txtManaCost, "growx, wrap");

        // Type Line Section
        addSectionHeader("Type Line");
        pnlMain.add(lblCardType);
        pnlMain.add(cmbCardType, "growx, wrap");

        pnlMain.add(lblSubtype);
        pnlMain.add(txtSubtype, "growx, wrap");

        // Card Text Section
        addSectionHeader("Card Text");
        pnlMain.add(lblAbilities, "top");
        pnlMain.add(scrAbilities, "growx, height 150::, wrap");

        // Stats Section
        addSectionHeader("Stats");
        pnlMain.add(lblPower);
        pnlMain.add(txtPower, "growx, wrap");

        pnlMain.add(lblToughness);
        pnlMain.add(txtToughness, "growx, wrap");

        pnlMain.add(lblLoyalty);
        pnlMain.add(txtLoyalty, "growx, wrap");

        // Action Buttons
        JPanel pnlButtons = new JPanel(new MigLayout("insets 0, gap 10"));
        pnlButtons.setOpaque(false);
        pnlButtons.add(btnCreate);
        pnlButtons.add(btnClear);
        pnlButtons.add(btnCancel);

        pnlMain.add(pnlButtons, "span 2, align center, gaptop 20");
    }

    /**
     * Adds a section header to the form.
     */
    private void addSectionHeader(String text) {
        FLabel lblSection = new FLabel.Builder()
                .text(text)
                .fontSize(14)
                .build();
        pnlMain.add(lblSection, "span 2, gaptop 10, gapbottom 5, wrap");
    }

    // Getters for components (for controller access)
    public FTextField getTxtCardName() { return txtCardName; }
    public FTextField getTxtManaCost() { return txtManaCost; }
    public FComboBox<CardCreationType> getCmbCardType() { return cmbCardType; }
    public FTextField getTxtSubtype() { return txtSubtype; }
    public FTextArea getTxtAbilities() { return txtAbilities; }
    public FTextField getTxtPower() { return txtPower; }
    public FTextField getTxtToughness() { return txtToughness; }
    public FTextField getTxtLoyalty() { return txtLoyalty; }
    public FButton getBtnCreate() { return btnCreate; }
    public FButton getBtnClear() { return btnClear; }
    public FButton getBtnCancel() { return btnCancel; }

    public FScrollPane getScrContent() { return scrContent; }

    // IVTopLevelUI implementation

    @Override
    public void instantiate() {
        // Components are already instantiated in the enum constructor
    }

    @Override
    public void populate() {
        // Called when the screen is displayed
        // Controller will handle populating/refreshing data if needed
    }

    @Override
    public boolean onSwitching(FScreen fromScreen, FScreen toScreen) {
        // Allow switching away from this screen
        // Could add unsaved changes check here if needed
        return true;
    }

    @Override
    public boolean onClosing(FScreen screen) {
        // Don't close tab, return to home screen instead
        Singletons.getControl().setCurrentScreen(FScreen.HOME_SCREEN);
        return false;
    }
}

package forge.screens.cardcreation;

import forge.Forge;
import forge.assets.FSkinImage;
import forge.gui.cardcreation.CardCreationData;
import forge.gui.cardcreation.CardCreationService;
import forge.screens.TabPageScreen.TabPage;
import forge.toolbox.FButton;
import forge.toolbox.FLabel;
import forge.toolbox.FNumericTextField;
import forge.toolbox.FOptionPane;
import forge.toolbox.FTextField;
import forge.util.Utils;

import java.util.List;

/**
 * Tab page for card stats (power, toughness, loyalty) and final card creation.
 */
public class StatsPage extends TabPage<CardCreationScreen> {
    private final FLabel lblTitle = add(new FLabel.Builder().text("Card Stats").build());

    private final FLabel lblPower = add(new FLabel.Builder().text("Power:").build());
    private final FTextField txtPower = add(new FTextField());

    private final FLabel lblToughness = add(new FLabel.Builder().text("Toughness:").build());
    private final FTextField txtToughness = add(new FTextField());

    private final FLabel lblLoyalty = add(new FLabel.Builder().text("Loyalty:").build());
    private final FTextField txtLoyalty = add(new FTextField());

    private final FButton btnCreate = add(new FButton("Create Card"));
    private final FButton btnClear = add(new FButton("Clear All"));

    // Stub service implementation for now
    private final CardCreationService service = new CardCreationService() {
        @Override
        public CreationResult createCard(CardCreationData data) {
            List<String> errors = data.validate();
            if (!errors.isEmpty()) {
                return CreationResult.failure("Validation failed", String.join("\n", errors));
            }
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

    protected StatsPage() {
        super("Stats", FSkinImage.HDLOGO);

        // Set up button handlers
        btnCreate.setCommand(e -> handleCreate());
        btnClear.setCommand(e -> handleClear());
    }

    @Override
    protected void doLayout(float width, float height) {
        float padding = Utils.scale(10);
        float fieldHeight = Utils.AVG_FINGER_HEIGHT;
        float buttonHeight = fieldHeight * 1.2f;
        float y = padding;

        // Title
        lblTitle.setBounds(padding, y, width - 2 * padding, fieldHeight);
        y += fieldHeight + padding;

        // Power
        lblPower.setBounds(padding, y, width - 2 * padding, fieldHeight * 0.6f);
        y += fieldHeight * 0.6f;
        txtPower.setBounds(padding, y, width - 2 * padding, fieldHeight);
        y += fieldHeight + padding;

        // Toughness
        lblToughness.setBounds(padding, y, width - 2 * padding, fieldHeight * 0.6f);
        y += fieldHeight * 0.6f;
        txtToughness.setBounds(padding, y, width - 2 * padding, fieldHeight);
        y += fieldHeight + padding;

        // Loyalty
        lblLoyalty.setBounds(padding, y, width - 2 * padding, fieldHeight * 0.6f);
        y += fieldHeight * 0.6f;
        txtLoyalty.setBounds(padding, y, width - 2 * padding, fieldHeight);
        y += fieldHeight + padding * 2;

        // Buttons
        float buttonWidth = (width - 3 * padding) / 2;
        btnCreate.setBounds(padding, y, buttonWidth, buttonHeight);
        btnClear.setBounds(padding * 2 + buttonWidth, y, buttonWidth, buttonHeight);
    }

    private void handleCreate() {
        // Get data from all tabs
        CardCreationData data = new CardCreationData();

        // Get data from BasicInfoPage
        BasicInfoPage basicInfo = (BasicInfoPage) parentScreen.tabPages.get(0);
        data.setName(basicInfo.getCardName());
        data.setManaCost(basicInfo.getManaCost());
        // Note: In a full implementation, we'd parse the card type properly
        data.setSubtype(basicInfo.getSubtype());

        // Get data from CardTextPage
        CardTextPage cardText = (CardTextPage) parentScreen.tabPages.get(1);
        data.setAbilities(cardText.getAbilities());

        // Get data from this page
        data.setPower(txtPower.getText());
        data.setToughness(txtToughness.getText());
        data.setLoyalty(txtLoyalty.getText());

        // Create card
        CardCreationService.CreationResult result = service.createCard(data);

        if (result.isSuccess()) {
            FOptionPane.showMessageDialog(result.getMessage(), "Success", null);
            handleClear();
        } else {
            String errorMessage = result.getMessage();
            if (result.getErrorDetails() != null && !result.getErrorDetails().isEmpty()) {
                errorMessage += "\n\nDetails:\n" + result.getErrorDetails();
            }
            FOptionPane.showErrorDialog(errorMessage, "Error");
        }
    }

    private void handleClear() {
        // Clear all fields in all tabs
        BasicInfoPage basicInfo = (BasicInfoPage) parentScreen.tabPages.get(0);
        basicInfo.clear();

        CardTextPage cardText = (CardTextPage) parentScreen.tabPages.get(1);
        cardText.clear();

        txtPower.setText("");
        txtToughness.setText("");
        txtLoyalty.setText("");
    }

    public void setPower(String power) {
        txtPower.setText(power);
    }

    public void setToughness(String toughness) {
        txtToughness.setText(toughness);
    }

    public void setLoyalty(String loyalty) {
        txtLoyalty.setText(loyalty);
    }
}

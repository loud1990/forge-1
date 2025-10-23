package forge.screens.cardcreation;

import forge.Forge;
import forge.assets.FSkinImage;
import forge.gui.cardcreation.CardCreationType;
import forge.screens.TabPageScreen.TabPage;
import forge.toolbox.FLabel;
import forge.toolbox.FNumericTextField;
import forge.toolbox.FOptionPane;
import forge.toolbox.FTextField;
import forge.util.Utils;

/**
 * Tab page for basic card information (name, mana cost, type, subtype).
 */
public class BasicInfoPage extends TabPage<CardCreationScreen> {
    private final FLabel lblTitle = add(new FLabel.Builder().text("Basic Information").build());

    private final FLabel lblCardName = add(new FLabel.Builder().text("Card Name:").build());
    private final FTextField txtCardName = add(new FTextField());

    private final FLabel lblManaCost = add(new FLabel.Builder().text("Mana Cost:").build());
    private final FTextField txtManaCost = add(new FTextField());

    private final FLabel lblCardType = add(new FLabel.Builder().text("Card Type:").build());
    // Note: For a proper implementation, this should be a combo box/spinner
    // Using FTextField as a placeholder - full implementation would use FComboBox equivalent
    private final FTextField txtCardType = add(new FTextField());

    private final FLabel lblSubtype = add(new FLabel.Builder().text("Subtype:").build());
    private final FTextField txtSubtype = add(new FTextField());

    protected BasicInfoPage() {
        super("Basic Info", FSkinImage.HDQUESTION);
    }

    @Override
    protected void doLayout(float width, float height) {
        float padding = Utils.scale(10);
        float fieldHeight = Utils.AVG_FINGER_HEIGHT;
        float y = padding;

        // Title
        lblTitle.setBounds(padding, y, width - 2 * padding, fieldHeight);
        y += fieldHeight + padding;

        // Card Name
        lblCardName.setBounds(padding, y, width - 2 * padding, fieldHeight * 0.6f);
        y += fieldHeight * 0.6f;
        txtCardName.setBounds(padding, y, width - 2 * padding, fieldHeight);
        y += fieldHeight + padding;

        // Mana Cost
        lblManaCost.setBounds(padding, y, width - 2 * padding, fieldHeight * 0.6f);
        y += fieldHeight * 0.6f;
        txtManaCost.setBounds(padding, y, width - 2 * padding, fieldHeight);
        y += fieldHeight + padding;

        // Card Type
        lblCardType.setBounds(padding, y, width - 2 * padding, fieldHeight * 0.6f);
        y += fieldHeight * 0.6f;
        txtCardType.setBounds(padding, y, width - 2 * padding, fieldHeight);
        y += fieldHeight + padding;

        // Subtype
        lblSubtype.setBounds(padding, y, width - 2 * padding, fieldHeight * 0.6f);
        y += fieldHeight * 0.6f;
        txtSubtype.setBounds(padding, y, width - 2 * padding, fieldHeight);
    }

    public String getCardName() {
        return txtCardName.getText();
    }

    public String getManaCost() {
        return txtManaCost.getText();
    }

    public String getCardType() {
        return txtCardType.getText();
    }

    public String getSubtype() {
        return txtSubtype.getText();
    }

    public void clear() {
        txtCardName.setText("");
        txtManaCost.setText("");
        txtCardType.setText("");
        txtSubtype.setText("");
    }
}

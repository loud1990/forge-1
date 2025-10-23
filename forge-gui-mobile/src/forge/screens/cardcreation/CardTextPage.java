package forge.screens.cardcreation;

import forge.Forge;
import forge.assets.FSkinImage;
import forge.screens.TabPageScreen.TabPage;
import forge.toolbox.FLabel;
import forge.toolbox.FTextArea;
import forge.util.Utils;

/**
 * Tab page for card text/abilities.
 */
public class CardTextPage extends TabPage<CardCreationScreen> {
    private final FLabel lblTitle = add(new FLabel.Builder().text("Card Text & Abilities").build());

    private final FLabel lblAbilities = add(new FLabel.Builder().text("Abilities / Card Text:").build());
    private final FTextArea txtAbilities = add(new FTextArea());

    private final FLabel lblHelpText = add(new FLabel.Builder()
            .text("Enter card abilities and rules text here")
            .fontSize(11)
            .build());

    protected CardTextPage() {
        super("Card Text", FSkinImage.HDLOGO);
    }

    @Override
    protected void doLayout(float width, float height) {
        float padding = Utils.scale(10);
        float fieldHeight = Utils.AVG_FINGER_HEIGHT;
        float y = padding;

        // Title
        lblTitle.setBounds(padding, y, width - 2 * padding, fieldHeight);
        y += fieldHeight + padding;

        // Abilities Label
        lblAbilities.setBounds(padding, y, width - 2 * padding, fieldHeight * 0.6f);
        y += fieldHeight * 0.6f;

        // Abilities Text Area (takes most of the remaining space)
        float textAreaHeight = height - y - fieldHeight - 3 * padding;
        txtAbilities.setBounds(padding, y, width - 2 * padding, textAreaHeight);
        y += textAreaHeight + padding;

        // Help Text
        lblHelpText.setBounds(padding, y, width - 2 * padding, fieldHeight * 0.6f);
    }

    public String getAbilities() {
        return txtAbilities.getText();
    }

    public void clear() {
        txtAbilities.setText("");
    }
}

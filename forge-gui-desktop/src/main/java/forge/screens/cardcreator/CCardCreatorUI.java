/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.screens.cardcreator;

import forge.StaticData;
import forge.card.CardDb;
import forge.gui.framework.ICDoc;
import forge.item.PaperCard;

import java.util.regex.Pattern;

/**
 * Controller for Card Creator UI.
 *
 * <br><i>(C at beginning of class name denotes a controller class.)</i>
 */
public enum CCardCreatorUI implements ICDoc {
    SINGLETON_INSTANCE;

    private static final Pattern MANA_COST_PATTERN = Pattern.compile("^[0-9XWUBRGCP/\\{\\} ]*$");

    @Override
    public void register() {
    }

    @Override
    public void initialize() {
    }

    @Override
    public void update() {
    }

    public void createCard() {
        VCardCreatorUI view = VCardCreatorUI.SINGLETON_INSTANCE;

        // Validate
        if (!validate()) {
            return;
        }

        try {
            // Generate card file content
            String cardFileContent = CardFileWriter.generateCardFile(
                view.getCardName(),
                view.getManaCost(),
                view.getCardType(),
                view.getSubtypes(),
                view.isLegendary(),
                view.getOracleText(),
                view.getFlavorText(),
                view.getKeywords(),
                view.getPower(),
                view.getToughness(),
                view.getLoyalty(),
                view.getCustomScript()
            );

            // Save card file
            File cardFile = CardFileWriter.saveCardFile(view.getCardName(), cardFileContent);

            // Add to set
            String rarityCode = getRarityCode(view.getRarity());
            CardFileWriter.addToSet(view.getCardName(), rarityCode);

            // Handle image if provided
            String imagePath = "";
            if (view.getSelectedImageFile() != null) {
                imagePath = CardFileWriter.saveCardImage(view.getCardName(), view.getSelectedImageFile());
            }

            // Show success
            String message = "Card '" + view.getCardName() + "' created successfully!\n\n" +
                    "Added to set: Edge of Eternities\n" +
                    "Rarity: " + view.getRarity() + "\n\n" +
                    "Card file: " + cardFile.getAbsolutePath() +
                    (imagePath.isEmpty() ? "\n\nUsing default card image." : "\nImage: " + imagePath);

            view.showSuccess(message);

        } catch (Exception e) {
            view.showError("Failed to create card: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validate() {
        VCardCreatorUI view = VCardCreatorUI.SINGLETON_INSTANCE;

        // Required fields
        if (view.getCardName().isEmpty()) {
            view.showError("Card name is required!");
            return false;
        }

        if (view.getManaCost().isEmpty()) {
            view.showError("Mana cost is required!");
            return false;
        }

        if (view.getOracleText().isEmpty()) {
            view.showError("Oracle text is required!");
            return false;
        }

        // Validate mana cost format
        if (!validateManaCost(view.getManaCost())) {
            view.showError("Invalid mana cost format!\n\nExamples:\nG\n1 U\n2 R R\nX U U\n3 W W W");
            return false;
        }

        // Check for duplicates
        CardDb db = StaticData.instance().getCommonCards();
        PaperCard existing = db.getCard(view.getCardName());
        if (existing != null) {
            view.showError("A card named '" + view.getCardName() + "' already exists!");
            return false;
        }

        // Conditional required fields
        String cardType = view.getCardType();
        if (("Creature".equals(cardType) || "Tribal".equals(cardType))) {
            if (view.getPower().isEmpty() || view.getToughness().isEmpty()) {
                view.showError("Power and Toughness are required for creatures!");
                return false;
            }
            if (!isNumeric(view.getPower()) && !"*".equals(view.getPower())) {
                view.showError("Power must be a number or '*'!");
                return false;
            }
            if (!isNumeric(view.getToughness()) && !"*".equals(view.getToughness())) {
                view.showError("Toughness must be a number or '*'!");
                return false;
            }
        }

        if ("Planeswalker".equals(cardType)) {
            if (view.getLoyalty().isEmpty()) {
                view.showError("Loyalty is required for planeswalkers!");
                return false;
            }
            if (!isNumeric(view.getLoyalty()) && !"X".equals(view.getLoyalty())) {
                view.showError("Loyalty must be a number or 'X'!");
                return false;
            }
        }

        // Validate image if provided
        if (view.getSelectedImageFile() != null) {
            String fileName = view.getSelectedImageFile().getName().toLowerCase();
            if (!fileName.matches(".*\\.(jpg|jpeg|png)$")) {
                view.showError("Image must be .jpg or .png format!");
                return false;
            }
        }

        return true;
    }

    private boolean validateManaCost(String cost) {
        // Allow simple format: numbers, W/U/B/R/G, X, spaces
        return MANA_COST_PATTERN.matcher(cost.toUpperCase()).matches();
    }

    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String getRarityCode(String rarity) {
        switch (rarity) {
            case "Common": return "C";
            case "Uncommon": return "U";
            case "Rare": return "R";
            case "Mythic Rare": return "M";
            case "Special": return "S";
            default: return "C";
        }
    }
}

package forge.gui.cardcreation;

import java.util.ArrayList;
import java.util.List;

/**
 * Data model for custom card creation.
 * Holds all the form data needed to create a custom Magic: The Gathering card.
 */
public class CardCreationData {
    private String name;
    private String manaCost;
    private CardCreationType cardType;
    private String subtype;
    private String abilities;
    private String power;
    private String toughness;
    private String loyalty;

    public CardCreationData() {
        this.name = "";
        this.manaCost = "";
        this.cardType = CardCreationType.CREATURE;
        this.subtype = "";
        this.abilities = "";
        this.power = "";
        this.toughness = "";
        this.loyalty = "";
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public String getManaCost() {
        return manaCost;
    }

    public void setManaCost(String manaCost) {
        this.manaCost = manaCost != null ? manaCost.trim() : "";
    }

    public CardCreationType getCardType() {
        return cardType;
    }

    public void setCardType(CardCreationType cardType) {
        this.cardType = cardType != null ? cardType : CardCreationType.CREATURE;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype != null ? subtype.trim() : "";
    }

    public String getAbilities() {
        return abilities;
    }

    public void setAbilities(String abilities) {
        this.abilities = abilities != null ? abilities.trim() : "";
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power != null ? power.trim() : "";
    }

    public String getToughness() {
        return toughness;
    }

    public void setToughness(String toughness) {
        this.toughness = toughness != null ? toughness.trim() : "";
    }

    public String getLoyalty() {
        return loyalty;
    }

    public void setLoyalty(String loyalty) {
        this.loyalty = loyalty != null ? loyalty.trim() : "";
    }

    /**
     * Validates the card data and returns a list of error messages.
     *
     * @return list of validation errors, empty if valid
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        // Name is always required
        if (name == null || name.trim().isEmpty()) {
            errors.add("Card name is required");
        }

        // Card type is always required
        if (cardType == null) {
            errors.add("Card type is required");
        }

        // Type-specific validations
        if (cardType != null) {
            if (cardType.requiresPowerToughness()) {
                if (power == null || power.trim().isEmpty()) {
                    errors.add("Power is required for " + cardType.getDisplayName());
                }
                if (toughness == null || toughness.trim().isEmpty()) {
                    errors.add("Toughness is required for " + cardType.getDisplayName());
                }
            }

            if (cardType.requiresLoyalty()) {
                if (loyalty == null || loyalty.trim().isEmpty()) {
                    errors.add("Loyalty is required for " + cardType.getDisplayName());
                }
            }
        }

        return errors;
    }

    /**
     * Checks if the data is valid.
     *
     * @return true if no validation errors exist
     */
    public boolean isValid() {
        return validate().isEmpty();
    }

    /**
     * Resets all fields to their default values.
     */
    public void clear() {
        this.name = "";
        this.manaCost = "";
        this.cardType = CardCreationType.CREATURE;
        this.subtype = "";
        this.abilities = "";
        this.power = "";
        this.toughness = "";
        this.loyalty = "";
    }

    /**
     * Creates a copy of this CardCreationData.
     *
     * @return a new CardCreationData instance with the same values
     */
    public CardCreationData copy() {
        CardCreationData copy = new CardCreationData();
        copy.name = this.name;
        copy.manaCost = this.manaCost;
        copy.cardType = this.cardType;
        copy.subtype = this.subtype;
        copy.abilities = this.abilities;
        copy.power = this.power;
        copy.toughness = this.toughness;
        copy.loyalty = this.loyalty;
        return copy;
    }

    @Override
    public String toString() {
        return "CardCreationData{" +
                "name='" + name + '\'' +
                ", manaCost='" + manaCost + '\'' +
                ", cardType=" + cardType +
                ", subtype='" + subtype + '\'' +
                ", power='" + power + '\'' +
                ", toughness='" + toughness + '\'' +
                ", loyalty='" + loyalty + '\'' +
                '}';
    }
}

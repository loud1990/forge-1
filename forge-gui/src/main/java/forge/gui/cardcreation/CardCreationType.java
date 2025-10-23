package forge.gui.cardcreation;

/**
 * Enum representing the main types of Magic: The Gathering cards.
 * Used to determine which fields are required/optional in the card creation form.
 */
public enum CardCreationType {
    CREATURE("Creature", true, false),
    PLANESWALKER("Planeswalker", false, true),
    INSTANT("Instant", false, false),
    SORCERY("Sorcery", false, false),
    ENCHANTMENT("Enchantment", false, false),
    ARTIFACT("Artifact", false, false),
    LAND("Land", false, false),
    TRIBAL("Tribal", false, false);

    private final String displayName;
    private final boolean requiresPowerToughness;
    private final boolean requiresLoyalty;

    CardCreationType(String displayName, boolean requiresPowerToughness, boolean requiresLoyalty) {
        this.displayName = displayName;
        this.requiresPowerToughness = requiresPowerToughness;
        this.requiresLoyalty = requiresLoyalty;
    }

    /**
     * @return the display name for this card type
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return true if this card type requires power and toughness fields (e.g., Creature)
     */
    public boolean requiresPowerToughness() {
        return requiresPowerToughness;
    }

    /**
     * @return true if this card type requires a loyalty field (e.g., Planeswalker)
     */
    public boolean requiresLoyalty() {
        return requiresLoyalty;
    }

    /**
     * Get CardCreationType by display name.
     *
     * @param displayName the display name to search for
     * @return the matching CardCreationType, or null if not found
     */
    public static CardCreationType fromDisplayName(String displayName) {
        for (CardCreationType type : values()) {
            if (type.displayName.equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

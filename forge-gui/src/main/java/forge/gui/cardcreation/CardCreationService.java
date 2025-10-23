package forge.gui.cardcreation;

/**
 * Service interface for creating custom cards.
 * This interface will be implemented to handle the actual card creation logic,
 * including validation, script generation, and persistence.
 */
public interface CardCreationService {

    /**
     * Result of a card creation attempt.
     */
    class CreationResult {
        private final boolean success;
        private final String message;
        private final String errorDetails;

        private CreationResult(boolean success, String message, String errorDetails) {
            this.success = success;
            this.message = message;
            this.errorDetails = errorDetails;
        }

        public static CreationResult success(String message) {
            return new CreationResult(true, message, null);
        }

        public static CreationResult failure(String message, String errorDetails) {
            return new CreationResult(false, message, errorDetails);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getErrorDetails() {
            return errorDetails;
        }
    }

    /**
     * Creates a custom card from the provided data.
     * This method validates the data, generates the card script,
     * and saves it to the appropriate location.
     *
     * @param data the card creation data
     * @return the result of the creation attempt
     */
    CreationResult createCard(CardCreationData data);

    /**
     * Validates card data without creating the card.
     *
     * @param data the card creation data to validate
     * @return the validation result
     */
    CreationResult validateCard(CardCreationData data);
}

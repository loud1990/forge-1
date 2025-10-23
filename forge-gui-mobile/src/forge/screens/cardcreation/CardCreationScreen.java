package forge.screens.cardcreation;

import forge.Forge;
import forge.assets.FSkinImage;
import forge.screens.FScreen;
import forge.screens.TabPageScreen;
import forge.screens.home.HomeScreen;

/**
 * Main screen for the Card Creator feature on mobile.
 * Provides a wizard-style interface with multiple tabs for creating custom MTG cards.
 */
public class CardCreationScreen extends TabPageScreen<CardCreationScreen> {
    private static CardCreationScreen cardCreationScreen;
    private static boolean fromHomeScreen;

    public static void show(boolean fromHomeScreen0) {
        if (cardCreationScreen == null) {
            cardCreationScreen = new CardCreationScreen();
        }
        fromHomeScreen = fromHomeScreen0;
        Forge.openScreen(cardCreationScreen);
    }

    @SuppressWarnings("unchecked")
    private CardCreationScreen() {
        super(new TabHeader<CardCreationScreen>(new TabPage[] {
                new BasicInfoPage(),
                new CardTextPage(),
                new StatsPage()
        }, true) {
            @Override
            protected boolean showBackButtonInLandscapeMode() {
                return !fromHomeScreen;
            }
        });
    }

    @Override
    public FScreen getLandscapeBackdropScreen() {
        if (fromHomeScreen) {
            return HomeScreen.instance;
        }
        return null;
    }

    @Override
    public void showMenu() {
        // Hide card creation screen when menu button pressed
        Forge.back();
    }
}

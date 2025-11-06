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

import forge.localinstance.properties.ForgeConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for writing card files and managing custom sets.
 */
public class CardFileWriter {

    private static final String SET_CODE = "EOE";
    private static final String SET_NAME = "Edge of Eternities";

    public static String generateCardFile(String cardName, String manaCost, String cardType,
                                          String subtypes, boolean isLegendary, String oracleText,
                                          String flavorText, String keywords, String power,
                                          String toughness, String loyalty, String customScript) {
        StringBuilder sb = new StringBuilder();

        // Name
        sb.append("Name:").append(cardName).append("\n");

        // Mana Cost
        sb.append("ManaCost:").append(manaCost).append("\n");

        // Types
        StringBuilder types = new StringBuilder();
        if (isLegendary) {
            types.append("Legendary ");
        }
        types.append(cardType);
        if (!subtypes.isEmpty()) {
            types.append(" ").append(subtypes);
        }
        sb.append("Types:").append(types).append("\n");

        // Power/Toughness for creatures
        if (("Creature".equals(cardType) || "Tribal".equals(cardType)) && !power.isEmpty() && !toughness.isEmpty()) {
            sb.append("PT:").append(power).append("/").append(toughness).append("\n");
        }

        // Loyalty for planeswalkers
        if ("Planeswalker".equals(cardType) && !loyalty.isEmpty()) {
            sb.append("Loyalty:").append(loyalty).append("\n");
        }

        // Keywords (one K: line per keyword)
        if (!keywords.isEmpty()) {
            String[] keywordLines = keywords.split("\n");
            for (String keyword : keywordLines) {
                keyword = keyword.trim();
                if (!keyword.isEmpty()) {
                    sb.append("K:").append(keyword).append("\n");
                }
            }
        }

        // Custom script or default
        if (!customScript.isEmpty()) {
            // User provided custom script - append as-is (already validated)
            String[] scriptLines = customScript.split("\n");
            for (String line : scriptLines) {
                line = line.trim();
                if (!line.isEmpty()) {
                    sb.append(line).append("\n");
                }
            }
        } else {
            // Default simple ability (like Llanowar Elves)
            sb.append("A:AB$ Mana | Cost$ T | Produced$ G | SpellDescription$ Add {G}.\n");
        }

        // Oracle text + flavor text
        sb.append("Oracle:").append(oracleText);
        if (!flavorText.isEmpty()) {
            sb.append("|").append(flavorText);
        }
        sb.append("\n");

        return sb.toString();
    }

    public static File saveCardFile(String cardName, String content) throws IOException {
        // Get custom cards directory
        File cardsDir = new File(ForgeConstants.USER_CUSTOM_CARDS_DIR);
        if (!cardsDir.exists()) {
            cardsDir.mkdirs();
        }

        // Generate filename
        String filename = cardName.toLowerCase()
                .replace(' ', '_')
                .replaceAll("[^a-z0-9_]", "") + ".txt";

        // Get first letter subdirectory
        char firstLetter = filename.charAt(0);
        File letterDir = new File(cardsDir, String.valueOf(firstLetter));
        if (!letterDir.exists()) {
            letterDir.mkdirs();
        }

        // Write file
        File cardFile = new File(letterDir, filename);
        Files.write(cardFile.toPath(), content.getBytes());

        return cardFile;
    }

    public static String saveCardImage(String cardName, File sourceImage) throws IOException {
        // Get cache directory for card images
        File cacheDir = new File(ForgeConstants.CACHE_CARD_PICS_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        // Set-specific directory
        File setDir = new File(cacheDir, SET_CODE);
        if (!setDir.exists()) {
            setDir.mkdirs();
        }

        // Determine file extension
        String ext = sourceImage.getName().substring(sourceImage.getName().lastIndexOf('.'));
        String destFilename = cardName + ".full" + ext;

        File destFile = new File(setDir, destFilename);
        Files.copy(sourceImage.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return destFile.getAbsolutePath();
    }

    public static void addToSet(String cardName, String rarityCode) throws IOException {
        File editionsDir = new File(ForgeConstants.USER_CUSTOM_EDITIONS_DIR);
        if (!editionsDir.exists()) {
            editionsDir.mkdirs();
        }

        File setFile = new File(editionsDir, SET_NAME.replace(' ', '_') + ".txt");

        if (!setFile.exists()) {
            createNewSetFile(setFile);
        }

        // Read existing content
        List<String> lines = Files.readAllLines(setFile.toPath());

        // Find next collector number and insert position
        int maxCollector = 0;
        int insertIndex = -1;
        boolean inCardsSection = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.equals("[cards]")) {
                inCardsSection = true;
                insertIndex = i + 1;
            } else if (inCardsSection && !line.isEmpty() && !line.startsWith("[")) {
                String[] parts = line.split("\\s+", 3);
                if (parts.length >= 2) {
                    try {
                        int num = Integer.parseInt(parts[0]);
                        maxCollector = Math.max(maxCollector, num);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        // Add new card entry
        int newCollector = maxCollector + 1;
        String newEntry = newCollector + " " + rarityCode + " " + cardName;

        if (insertIndex >= 0) {
            lines.add(insertIndex, newEntry);
        } else {
            lines.add(newEntry);
        }

        // Write back
        Files.write(setFile.toPath(), lines);
    }

    private static void createNewSetFile(File setFile) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("[metadata]");
        lines.add("Code=" + SET_CODE);
        lines.add("Name=" + SET_NAME);
        lines.add("Date=2025-01-01");
        lines.add("Type=Custom");
        lines.add("");
        lines.add("[cards]");

        Files.write(setFile.toPath(), lines);
    }
}

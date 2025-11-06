# Card Creator GUI

The Card Creator is a graphical user interface for creating custom Magic: The Gathering cards in Forge. This tool allows you to design cards with a user-friendly form without manually writing card script files.

## Accessing the Card Creator

The Card Creator is accessible from the main navigation bar in Forge Desktop:
1. Launch Forge Desktop
2. Click on the **Card Creator** tab in the navigation bar (located between Deck Editor and Workshop)

## Features

### Basic Card Information
- **Card Name** (Required): The name of your custom card
- **Mana Cost** (Required): The casting cost in Forge format
  - Examples: `G`, `1 U`, `2 R R`, `X U U`, `3 W W W`
  - Supports: Numbers (0-9), W/U/B/R/G, X, Phyrexian symbols, hybrid mana
- **Card Type** (Required): Select from dropdown
  - Creature, Instant, Sorcery, Enchantment, Artifact, Planeswalker, Land, Tribal
- **Subtypes**: Space-separated subtypes
  - Example: `Elf Druid` for Creature types
- **Legendary**: Checkbox to mark card as Legendary

### Set Information
- **Rarity** (Required): Common, Uncommon, Rare, Mythic Rare, or Special
- **Set**: Default is "Edge of Eternities" (custom set created automatically)

### Card Text
- **Oracle Text** (Required): The rules text that appears on the card
- **Flavor Text** (Optional): Italicized flavor text
- **Keywords** (Optional): One keyword per line
  - Examples: Flying, Haste, Trample, Vigilance, Lifelink

### Attributes
These fields are automatically enabled/disabled based on card type:
- **Power/Toughness**: Required for Creatures (also enabled for Tribal)
  - Supports numbers or `*` for variable P/T
- **Loyalty**: Required for Planeswalkers
  - Supports numbers or `X` for variable loyalty

### Card Image
- **Choose Image** (Optional): Upload a custom card image
  - Supported formats: `.jpg`, `.png`
  - Recommended size: 223x310 pixels (standard MTG card ratio)
  - Image preview shows your selection
  - If no image provided, Forge will use default card rendering

### Advanced: Custom Abilities
An optional collapsible section for advanced users who want to write custom card scripts:
- **Warning**: Custom scripts may break your card! Use only if you understand Forge scripting
- **Documentation**: Link to [Creating a Custom Card](https://github.com/Card-Forge/forge/blob/ec814fc706f49a4c05c60d437a5cad10a59a36cf/docs/Creating-a-Custom-Card.md)
- **Script Editor**: Multi-line text area for entering ability scripts
  - If left empty, a default script is used (simple mana ability like Llanowar Elves)
  - Supports all Forge script syntax: `A:`, `S:`, `T:`, `R:`, `SVar:` lines

## Validation

The Card Creator performs the following validations before saving:

### Required Field Validation
- Card Name must not be empty
- Mana Cost must not be empty
- Oracle Text must not be empty
- Power/Toughness required for Creatures
- Loyalty required for Planeswalkers

### Format Validation
- **Mana Cost**: Must contain only valid mana symbols and numbers
- **Power/Toughness**: Must be numeric or `*`
- **Loyalty**: Must be numeric or `X`
- **Image**: Must be `.jpg` or `.png` format

### Duplicate Detection
- Card names must be unique across all cards (standard and custom)
- If a card with the same name exists, creation will be blocked

## File Locations

When you create a card, the following files are generated:

### Card Script File
- **Location**: `~/.forge/custom/cards/[first_letter]/[card_name].txt`
  - Linux: `~/.forge/custom/cards/`
  - Windows: `%appdata%/Forge/custom/cards/`
- **Format**: Plain text with pipe-delimited properties
- **Example**: For "My Custom Card", file would be `~/.forge/custom/cards/m/my_custom_card.txt`

### Card Image
- **Location**: `%localappdata%/Forge/Cache/pics/cards/EOE/[Card Name].full.jpg`
  - Linux: `~/.local/share/Forge/Cache/pics/cards/EOE/`
  - Windows: `%localappdata%/Forge/Cache/pics/cards/EOE/`
- Images are copied to the set-specific cache directory

### Set File
- **Location**: `~/.forge/custom/editions/Edge_of_Eternities.txt`
- Automatically created on first card creation
- Contains metadata and card list with collector numbers

## Example Card Creation

Here's an example of creating a simple creature:

1. **Card Name**: `Forest Sprite`
2. **Mana Cost**: `G`
3. **Card Type**: `Creature`
4. **Subtypes**: `Faerie`
5. **Legendary**: Unchecked
6. **Rarity**: `Common`
7. **Oracle Text**: `Flying`
8. **Keywords**: `Flying` (one per line)
9. **Power**: `1`
10. **Toughness**: `1`
11. **Image**: Upload a 223x310 .jpg file (optional)
12. Click **Create Card**

## Generated Card File

The above example would generate this card file:

```
Name:Forest Sprite
ManaCost:G
Types:Creature Faerie
PT:1/1
K:Flying
A:AB$ Mana | Cost$ T | Produced$ G | SpellDescription$ Add {G}.
Oracle:Flying
```

## Custom Set: Edge of Eternities

All cards created through the Card Creator are automatically added to the "Edge of Eternities" (EOE) custom set.

### Set Properties
- **Set Code**: `EOE`
- **Set Name**: Edge of Eternities
- **Type**: Custom (prevents auto-downloading images)
- **Collector Numbers**: Auto-incremented starting from 1

### Using Your Custom Cards

After creating cards:
1. Cards are immediately available in Forge (may require restart)
2. Use them in deck building like any other card
3. Filter by set "EOE" to see all your custom cards
4. Cards appear in the card database alongside standard cards

## Tips and Best Practices

### Mana Cost Format
- Colorless mana first: `3 U U` not `U U 3`
- Use spaces between symbols: `2 R R` not `2RR`
- X spells: `X U U` for "X plus two blue"

### Oracle Text
- Write clear, concise rules text
- Use proper MTG templating for consistency
- Include reminder text for complex mechanics

### Keywords
- Use official Magic keyword names
- One keyword per line for clarity
- Check spelling - typos may cause issues

### Custom Scripts
- Only use if you understand Forge scripting
- Test your cards thoroughly
- Reference existing card scripts for syntax examples
- Start with simple abilities before attempting complex ones

### Image Guidelines
- Use high-quality images (223x310 recommended)
- Ensure images are properly cropped
- Supported formats: JPG, PNG
- Images are cached - clear cache if updating

## Troubleshooting

### Card Not Appearing
- Try restarting Forge to reload the card database
- Check that the card file was created in the correct directory
- Verify there are no typos in the card name

### Validation Errors
- Read error messages carefully - they indicate what's wrong
- Check mana cost format matches examples
- Ensure all required fields are filled

### Custom Script Issues
- Validate your script syntax against existing cards
- Check the [Card Scripting API](https://github.com/Card-Forge/forge) documentation
- Start with the default script and modify incrementally

### Image Not Showing
- Verify image file format (JPG/PNG only)
- Check image was copied to correct cache directory
- Clear Forge's image cache and restart

## Related Documentation

- [Creating a Custom Card](./Creating-a-Custom-Card.md) - Manual card scripting
- [Creating a Custom Set](./Creating-a-custom-set.md) - Set file format details
- [Card Scripting API](./Card-scripting-API/) - Complete scripting reference

## Future Enhancements

Planned features for future versions:
- Ability script builder/wizard
- Multiple abilities support UI
- Token creation
- Double-faced card support
- Full card preview/renderer
- Edit existing custom cards
- Export/import custom card sets
- Template system for common card types

## Support

For issues or questions:
- Check [Frequently Asked Questions](./Frequently-Asked-Questions.md)
- Visit the Forge forums
- Report bugs on the Forge GitHub repository

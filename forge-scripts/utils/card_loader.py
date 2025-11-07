"""Load and parse Forge card script files."""
import os
import re
from pathlib import Path
from typing import Dict, List, Optional

def parse_card_file(filepath: str) -> Optional[Dict[str, str]]:
    """Parse a single card file and extract metadata."""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        card = {'filepath': filepath, 'full_script': content}

        for line in content.split('\n'):
            if ':' not in line:
                continue
            key, value = line.split(':', 1)
            card[key.strip()] = value.strip()

        # Extract filename as ID
        card['id'] = Path(filepath).stem

        # Calculate priority weight
        card['priority_weight'] = calculate_priority(card)

        return card
    except Exception as e:
        print(f"Error parsing {filepath}: {e}")
        return None

def calculate_priority(card: Dict[str, str]) -> float:
    """Calculate priority weight for search results."""
    weight = 1.0

    # Multicolor cards: 1.5x
    mana_cost = card.get('ManaCost', '')
    colors = set(re.findall(r'[WUBRG]', mana_cost))
    if len(colors) >= 2:
        weight *= 1.5

    # Rares/Mythics: 1.3x
    rarity = card.get('Rarity', '')
    if rarity in ['Rare', 'Mythic', 'R', 'M']:
        weight *= 1.3

    # Set mechanics (keywords): 1.4x
    keywords = ['Convoke', 'Surveil', 'Escape', 'Mutate', 'Companion']
    oracle = card.get('Oracle', '').lower()
    for keyword in keywords:
        if keyword.lower() in oracle:
            weight *= 1.4
            break

    return weight

def load_all_cards(cardsfolder_path: str = '../forge-gui/res/cardsfolder') -> List[Dict[str, str]]:
    """Load all card files from cardsfolder directory."""
    cards = []
    script_dir = Path(__file__).parent.parent
    cards_path = (script_dir / cardsfolder_path).resolve()

    print(f"Loading cards from: {cards_path}")

    # Walk through all subdirectories
    for root, _, files in os.walk(cards_path):
        for filename in files:
            if filename.endswith('.txt'):
                filepath = os.path.join(root, filename)
                card = parse_card_file(filepath)
                if card:
                    cards.append(card)

    print(f"Loaded {len(cards)} cards")
    return cards

def get_card_text_for_embedding(card: Dict[str, str]) -> str:
    """Get text representation for embedding generation."""
    parts = []

    # Card name
    if 'Name' in card:
        parts.append(f"Name: {card['Name']}")

    # Mana cost
    if 'ManaCost' in card:
        parts.append(f"Cost: {card['ManaCost']}")

    # Types
    if 'Types' in card:
        parts.append(f"Types: {card['Types']}")

    # PT or Loyalty
    if 'PT' in card:
        parts.append(f"P/T: {card['PT']}")
    elif 'Loyalty' in card:
        parts.append(f"Loyalty: {card['Loyalty']}")

    # Oracle text (most important for similarity)
    if 'Oracle' in card:
        parts.append(f"Text: {card['Oracle']}")

    return ' | '.join(parts)

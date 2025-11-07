"""Unit tests for card_loader."""
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent.parent))

from utils.card_loader import parse_card_file, calculate_priority, get_card_text_for_embedding

def test_calculate_priority():
    """Test priority weight calculation."""
    # Multicolor card
    card1 = {'ManaCost': 'W U', 'Rarity': 'Common'}
    assert calculate_priority(card1) == 1.5

    # Rare card
    card2 = {'ManaCost': 'G', 'Rarity': 'Rare'}
    assert calculate_priority(card2) == 1.3

    # Multicolor + Rare
    card3 = {'ManaCost': 'W U', 'Rarity': 'Rare'}
    assert calculate_priority(card3) == 1.5 * 1.3

    # Common card
    card4 = {'ManaCost': 'G', 'Rarity': 'Common'}
    assert calculate_priority(card4) == 1.0

    print("✓ Priority calculation tests passed")

def test_get_card_text_for_embedding():
    """Test text extraction for embeddings."""
    card = {
        'Name': 'Test Card',
        'ManaCost': '2 G',
        'Types': 'Creature Elf',
        'PT': '2/2',
        'Oracle': 'Test ability'
    }

    text = get_card_text_for_embedding(card)
    assert 'Name: Test Card' in text
    assert 'Cost: 2 G' in text
    assert 'Types: Creature Elf' in text
    assert 'P/T: 2/2' in text
    assert 'Text: Test ability' in text

    print("✓ Embedding text extraction tests passed")

if __name__ == "__main__":
    test_calculate_priority()
    test_get_card_text_for_embedding()
    print("\nAll tests passed!")

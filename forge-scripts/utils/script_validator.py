"""Validate and write Forge card scripts."""
import os
import re
from pathlib import Path
from typing import Optional, Dict

def validate_script(script: str) -> tuple[bool, str]:
    """Validate Forge card script syntax."""
    required_fields = ['Name:', 'ManaCost:', 'Types:']

    for field in required_fields:
        if field not in script:
            return False, f"Missing required field: {field}"

    # Check name format
    name_match = re.search(r'Name:(.+)', script)
    if not name_match or not name_match.group(1).strip():
        return False, "Invalid Name field"

    return True, "Valid"

def write_card_script(card_name: str, script: str, output_dir: str = '../forge-gui/res/cardsfolder') -> Optional[str]:
    """Write card script to appropriate directory."""
    try:
        # Get first letter for subdirectory
        first_letter = card_name[0].lower()
        if not first_letter.isalpha():
            first_letter = 'other'

        # Create filename (lowercase, underscores)
        filename = re.sub(r'[^a-z0-9_]', '_', card_name.lower())
        filename = re.sub(r'_+', '_', filename).strip('_')
        filename += '.txt'

        # Build path
        script_dir = Path(__file__).parent.parent
        target_dir = (script_dir / output_dir / first_letter).resolve()
        target_dir.mkdir(parents=True, exist_ok=True)

        filepath = target_dir / filename

        # Write file
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(script)

        return str(filepath)
    except Exception as e:
        print(f"Error writing script: {e}")
        return None

def clean_llm_output(text: str) -> str:
    """Clean LLM output to extract just the card script."""
    # Remove markdown code blocks
    text = re.sub(r'```.*?\n', '', text)
    text = re.sub(r'```', '', text)

    # Remove common LLM prefixes
    prefixes = ['Here is', 'Here\'s', 'The card script', 'Output:', 'Script:']
    for prefix in prefixes:
        if text.strip().startswith(prefix):
            text = text[text.find('\n')+1:]

    # Find first "Name:" and extract from there
    name_pos = text.find('Name:')
    if name_pos > 0:
        text = text[name_pos:]

    return text.strip()

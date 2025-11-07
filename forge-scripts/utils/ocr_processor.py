"""OCR processor using Tesseract."""
import pytesseract
import cv2
import numpy as np
from PIL import Image
from typing import Optional, Dict

def preprocess_image(image_path: str) -> np.ndarray:
    """Preprocess image for better OCR accuracy."""
    # Read image
    img = cv2.imread(image_path)

    # Convert to grayscale
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    # Denoise
    denoised = cv2.fastNlMeansDenoising(gray)

    # Increase contrast
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8,8))
    enhanced = clahe.apply(denoised)

    # Threshold
    _, binary = cv2.threshold(enhanced, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)

    return binary

def extract_text_from_image(image_path: str) -> str:
    """Extract text from card image using OCR."""
    try:
        # Preprocess
        processed = preprocess_image(image_path)

        # OCR
        text = pytesseract.image_to_string(processed, config='--psm 6')

        return text.strip()
    except Exception as e:
        print(f"OCR error for {image_path}: {e}")
        return ""

def parse_card_text(ocr_text: str) -> Dict[str, str]:
    """Parse OCR text into card components."""
    lines = [l.strip() for l in ocr_text.split('\n') if l.strip()]

    card_data = {
        'name': '',
        'mana_cost': '',
        'types': '',
        'oracle_text': '',
        'pt': '',
        'loyalty': ''
    }

    if not lines:
        return card_data

    # First line is usually name
    card_data['name'] = lines[0]

    # Try to find mana cost (numbers and WUBRG letters)
    for line in lines[:3]:
        if any(c in line for c in 'WUBRGXC{}'.split()):
            card_data['mana_cost'] = line
            break

    # Find type line (contains "—" or keywords like Creature, Instant, etc.)
    for line in lines:
        if any(t in line for t in ['Creature', 'Instant', 'Sorcery', 'Enchantment', 'Artifact', 'Planeswalker', 'Land']):
            card_data['types'] = line
            break

    # Find P/T or Loyalty (numbers at end)
    for line in reversed(lines):
        if '/' in line and any(c.isdigit() for c in line):
            card_data['pt'] = line
            break
        elif line.isdigit():
            card_data['loyalty'] = line
            break

    # Rest is oracle text
    oracle_lines = []
    for line in lines[1:]:
        if line not in [card_data['mana_cost'], card_data['types'], card_data['pt'], card_data['loyalty']]:
            oracle_lines.append(line)

    card_data['oracle_text'] = ' '.join(oracle_lines)

    return card_data

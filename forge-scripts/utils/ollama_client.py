"""Ollama client for LLM script generation."""
import requests
import json
from typing import List, Dict

class OllamaClient:
    def __init__(self, url: str = "http://localhost:11434", model: str = "gpt-oss:20b"):
        self.url = url
        self.model = model

    def generate_card_script(self, card_data: Dict[str, str], similar_cards: List[Dict]) -> str:
        """Generate card script using Ollama."""
        # Build prompt
        prompt = self._build_prompt(card_data, similar_cards)

        # Call Ollama
        response = requests.post(
            f"{self.url}/api/generate",
            json={
                "model": self.model,
                "prompt": prompt,
                "temperature": 0.2,
                "max_tokens": 2048,
                "stream": False
            },
            timeout=120
        )

        if response.status_code == 200:
            result = response.json()
            return result.get('response', '')
        else:
            raise Exception(f"Ollama error: {response.status_code}")

    def _build_prompt(self, card_data: Dict[str, str], similar_cards: List[Dict]) -> str:
        """Build prompt for LLM."""
        prompt = f"""You are a Forge card script generator. Generate a valid Forge card script based on the card data and similar examples.

CARD DATA:
Name: {card_data.get('name', '')}
Mana Cost: {card_data.get('mana_cost', '')}
Types: {card_data.get('types', '')}
Oracle Text: {card_data.get('oracle_text', '')}
"""

        if card_data.get('pt'):
            prompt += f"P/T: {card_data['pt']}\n"
        if card_data.get('loyalty'):
            prompt += f"Loyalty: {card_data['loyalty']}\n"

        prompt += "\nSIMILAR CARD EXAMPLES:\n"
        for i, card in enumerate(similar_cards[:3], 1):
            prompt += f"\nExample {i} - {card['name']}:\n"
            prompt += f"{card['full_script']}\n"

        prompt += """\nGENERATE:
Create a Forge card script following the format of the examples. Include:
- Name: (exact name from card data)
- ManaCost: (in Forge format like "2 G G")
- Types: (e.g., "Creature Elf Druid")
- PT: or Loyalty: (if applicable)
- K: lines for keywords
- A: lines for activated abilities
- S: lines for static abilities
- T: lines for triggered abilities
- Oracle: (the oracle text)

Output ONLY the card script, no explanations.
"""

        return prompt

    def health_check(self) -> bool:
        """Check if Ollama is running."""
        try:
            response = requests.get(f"{self.url}/api/tags", timeout=5)
            return response.status_code == 200
        except:
            return False

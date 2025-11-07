"""Web scraper for mythicspoiler.com."""
import requests
from bs4 import BeautifulSoup
from typing import List, Dict
import time

def scrape_new_spoilers(url: str = "https://www.mythicspoiler.com/newspoilers.html") -> List[Dict[str, str]]:
    """Scrape new card spoilers from mythicspoiler.com."""
    try:
        # Set user agent
        headers = {
            'User-Agent': 'Forge-Automation/1.0 (Card Script Generator)'
        }

        # Fetch page
        response = requests.get(url, headers=headers, timeout=10)
        response.raise_for_status()

        soup = BeautifulSoup(response.content, 'html.parser')

        cards = []

        # Find card images (typically in <img> tags)
        for img in soup.find_all('img'):
            src = img.get('src', '')
            alt = img.get('alt', '')

            # Skip if not a card image
            if not src or 'cards' not in src.lower():
                continue

            # Make absolute URL
            if src.startswith('//'):
                src = 'https:' + src
            elif src.startswith('/'):
                src = 'https://www.mythicspoiler.com' + src

            cards.append({
                'image_url': src,
                'name': alt or '',
                'source': 'mythicspoiler'
            })

        return cards
    except Exception as e:
        print(f"Error scraping: {e}")
        return []

def download_image(url: str, save_path: str) -> bool:
    """Download image from URL."""
    try:
        response = requests.get(url, timeout=10)
        response.raise_for_status()

        with open(save_path, 'wb') as f:
            f.write(response.content)

        return True
    except Exception as e:
        print(f"Error downloading {url}: {e}")
        return False

def rate_limit_delay(seconds: int = 5):
    """Delay to respect rate limiting."""
    time.sleep(seconds)

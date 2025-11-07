#!/usr/bin/env python3
"""Main automation runner for card script generation."""
import sys
import os
import logging
import argparse
from pathlib import Path
from datetime import datetime
import schedule
import time

sys.path.insert(0, str(Path(__file__).parent))

from utils.web_scraper import scrape_new_spoilers, download_image, rate_limit_delay
from utils.ocr_processor import extract_text_from_image, parse_card_text
from utils.rag_search import RAGSearch
from utils.ollama_client import OllamaClient
from utils.script_validator import validate_script, write_card_script, clean_llm_output
import config

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(f'{config.LOGS_DIR}/automation_{datetime.now().strftime("%Y%m%d")}.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

def process_single_card(card_info: dict, rag: RAGSearch, ollama: OllamaClient) -> bool:
    """Process a single card spoiler."""
    try:
        card_name = card_info.get('name', 'unknown')
        logger.info(f"Processing: {card_name}")

        # Download image
        image_path = f"{config.CACHE_DIR}/{card_name.replace(' ', '_')}.jpg"
        if not os.path.exists(image_path):
            if not download_image(card_info['image_url'], image_path):
                logger.error(f"Failed to download image for {card_name}")
                return False

        # OCR
        logger.info(f"  Running OCR...")
        ocr_text = extract_text_from_image(image_path)
        card_data = parse_card_text(ocr_text)

        if not card_data.get('name'):
            card_data['name'] = card_name

        logger.info(f"  Extracted: {card_data}")

        # Find similar cards
        logger.info(f"  Finding similar cards...")
        similar_cards = rag.find_similar_cards(card_data, limit=config.SIMILAR_CARDS_LIMIT)

        if not similar_cards:
            logger.warning(f"  No similar cards found for {card_name}")
            return False

        logger.info(f"  Found {len(similar_cards)} similar cards")

        # Generate script
        logger.info(f"  Generating script with LLM...")
        generated_script = ollama.generate_card_script(card_data, similar_cards)
        generated_script = clean_llm_output(generated_script)

        # Validate
        is_valid, message = validate_script(generated_script)
        if not is_valid:
            logger.error(f"  Invalid script: {message}")
            return False

        # Write to file
        output_path = write_card_script(card_data['name'], generated_script)
        if output_path:
            logger.info(f"  ✓ Successfully generated: {output_path}")
            return True
        else:
            logger.error(f"  Failed to write script")
            return False

    except Exception as e:
        logger.error(f"Error processing {card_info}: {e}")
        return False

def run_automation():
    """Run the automation pipeline."""
    logger.info("=" * 60)
    logger.info("Starting Forge Card Script Automation")
    logger.info("=" * 60)

    # Check Ollama
    logger.info("Checking Ollama...")
    ollama = OllamaClient(config.OLLAMA_URL, config.OLLAMA_MODEL)
    if not ollama.health_check():
        logger.error("Ollama is not running!")
        return

    logger.info("✓ Ollama is running")

    # Initialize RAG
    logger.info("Initializing RAG search...")
    rag = RAGSearch(config.EMBEDDING_MODEL)
    logger.info("✓ RAG initialized")

    # Scrape spoilers
    logger.info(f"Scraping {config.MYTHICSPOILER_URL}...")
    cards = scrape_new_spoilers(config.MYTHICSPOILER_URL)
    logger.info(f"Found {len(cards)} potential cards")

    if not cards:
        logger.info("No new cards to process")
        return

    # Process each card
    success_count = 0
    for i, card in enumerate(cards, 1):
        logger.info(f"\n[{i}/{len(cards)}] Processing card...")
        if process_single_card(card, rag, ollama):
            success_count += 1

        # Rate limiting
        if i < len(cards):
            rate_limit_delay(config.RATE_LIMIT_SECONDS)

    logger.info("\n" + "=" * 60)
    logger.info(f"Automation complete: {success_count}/{len(cards)} cards processed successfully")
    logger.info("=" * 60)

def main():
    parser = argparse.ArgumentParser(description='Forge Card Script Automation')
    parser.add_argument('--once', action='store_true', help='Run once and exit')
    parser.add_argument('--schedule', action='store_true', help='Run on schedule (2 AM daily)')
    args = parser.parse_args()

    if args.once:
        run_automation()
    elif args.schedule:
        logger.info(f"Scheduling automation for {config.SCHEDULE_HOUR}:{config.SCHEDULE_MINUTE:02d} daily")
        schedule.every().day.at(f"{config.SCHEDULE_HOUR:02d}:{config.SCHEDULE_MINUTE:02d}").do(run_automation)

        logger.info("Scheduler started. Press Ctrl+C to exit.")
        while True:
            schedule.run_pending()
            time.sleep(60)
    else:
        parser.print_help()

if __name__ == "__main__":
    main()

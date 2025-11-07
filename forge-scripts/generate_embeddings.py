#!/usr/bin/env python3
"""Generate embeddings for all card scripts and upload to Qdrant."""
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent))

from sentence_transformers import SentenceTransformer
from tqdm import tqdm
from utils.card_loader import load_all_cards, get_card_text_for_embedding
from utils.qdrant_manager import QdrantManager

def main():
    print("=" * 60)
    print("Forge Card Script Embedding Generation")
    print("=" * 60)

    # Initialize
    print("\n1. Loading Sentence Transformer model...")
    model = SentenceTransformer('all-MiniLM-L6-v2')

    # Check Qdrant
    print("\n2. Checking Qdrant connection...")
    qdrant = QdrantManager()
    if not qdrant.health_check():
        print("ERROR: Qdrant is not running!")
        print("Start with: docker run -d --name qdrant -p 6333:6333 qdrant/qdrant")
        sys.exit(1)
    print("✓ Qdrant is running")

    # Load cards
    print("\n3. Loading card scripts...")
    cards = load_all_cards()
    print(f"✓ Loaded {len(cards)} cards")

    # Create collection
    print("\n4. Creating Qdrant collection...")
    qdrant.create_collection(vector_size=384)
    print("✓ Collection created")

    # Generate embeddings and upload in batches
    print("\n5. Generating embeddings and uploading...")
    batch_size = 100
    points = []

    for i, card in enumerate(tqdm(cards, desc="Processing cards")):
        # Get text for embedding
        text = get_card_text_for_embedding(card)

        # Generate embedding
        embedding = model.encode(text, convert_to_tensor=False).tolist()

        # Create point
        point = {
            "id": i,
            "vector": embedding,
            "payload": {
                "card_id": card['id'],
                "name": card.get('Name', ''),
                "mana_cost": card.get('ManaCost', ''),
                "types": card.get('Types', ''),
                "oracle": card.get('Oracle', ''),
                "full_script": card['full_script'],
                "filepath": card['filepath'],
                "priority_weight": card['priority_weight']
            }
        }
        points.append(point)

        # Upload batch
        if len(points) >= batch_size:
            qdrant.upload_batch(points)
            points = []

    # Upload remaining
    if points:
        qdrant.upload_batch(points)

    print(f"\n✓ Successfully processed {len(cards)} cards")
    print("\n" + "=" * 60)
    print("Embedding generation complete!")
    print("=" * 60)

if __name__ == "__main__":
    main()

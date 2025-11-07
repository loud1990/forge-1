#!/usr/bin/env python3
"""Verify that all components are properly set up."""
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent))

def check_qdrant():
    """Check Qdrant connection."""
    try:
        from utils.qdrant_manager import QdrantManager
        qdrant = QdrantManager()
        if qdrant.health_check():
            print("✓ Qdrant: Running")
            return True
        else:
            print("✗ Qdrant: Not running")
            print("  Start with: docker run -d --name qdrant -p 6333:6333 qdrant/qdrant")
            return False
    except Exception as e:
        print(f"✗ Qdrant: Error - {e}")
        return False

def check_ollama():
    """Check Ollama connection."""
    try:
        from utils.ollama_client import OllamaClient
        ollama = OllamaClient()
        if ollama.health_check():
            print("✓ Ollama: Running")
            return True
        else:
            print("✗ Ollama: Not running")
            print("  Start with: ollama serve")
            return False
    except Exception as e:
        print(f"✗ Ollama: Error - {e}")
        return False

def check_sentence_transformers():
    """Check Sentence Transformers."""
    try:
        from sentence_transformers import SentenceTransformer
        model = SentenceTransformer('all-MiniLM-L6-v2')
        print("✓ Sentence Transformers: Available")
        return True
    except Exception as e:
        print(f"✗ Sentence Transformers: Error - {e}")
        print("  Install with: pip install sentence-transformers")
        return False

def check_tesseract():
    """Check Tesseract OCR."""
    try:
        import pytesseract
        from PIL import Image
        # Try to get version
        version = pytesseract.get_tesseract_version()
        print(f"✓ Tesseract: v{version}")
        return True
    except Exception as e:
        print(f"✗ Tesseract: Error - {e}")
        print("  Install with: sudo apt-get install tesseract-ocr")
        return False

def check_cardsfolder():
    """Check if cardsfolder exists."""
    try:
        from utils.card_loader import load_all_cards
        cards = load_all_cards()
        if len(cards) > 0:
            print(f"✓ Cardsfolder: {len(cards)} cards found")
            return True
        else:
            print("✗ Cardsfolder: No cards found")
            return False
    except Exception as e:
        print(f"✗ Cardsfolder: Error - {e}")
        return False

def check_embeddings():
    """Check if embeddings are generated."""
    try:
        from utils.qdrant_manager import QdrantManager
        qdrant = QdrantManager()

        # Try a simple search
        test_vector = [0.0] * 384
        results = qdrant.search(test_vector, limit=1, score_threshold=0.0)

        if results:
            print(f"✓ Embeddings: Generated ({len(results)} test result)")
            return True
        else:
            print("✗ Embeddings: Not generated")
            print("  Generate with: python generate_embeddings.py")
            return False
    except Exception as e:
        print(f"⚠ Embeddings: Cannot verify - {e}")
        print("  Generate with: python generate_embeddings.py")
        return False

def main():
    print("=" * 60)
    print("Forge Automation Setup Verification")
    print("=" * 60)
    print()

    checks = [
        ("Python Dependencies", check_sentence_transformers),
        ("Tesseract OCR", check_tesseract),
        ("Qdrant Vector DB", check_qdrant),
        ("Ollama LLM", check_ollama),
        ("Forge Cardsfolder", check_cardsfolder),
        ("Vector Embeddings", check_embeddings),
    ]

    results = []
    for name, check_func in checks:
        print(f"{name}:")
        result = check_func()
        results.append(result)
        print()

    print("=" * 60)
    passed = sum(results)
    total = len(results)

    if passed == total:
        print(f"✓ All checks passed ({passed}/{total})")
        print("\nYou're ready to run:")
        print("  python run_automation.py --once")
    else:
        print(f"⚠ {passed}/{total} checks passed")
        print("\nFix the errors above before running automation.")

    print("=" * 60)

    return 0 if passed == total else 1

if __name__ == "__main__":
    sys.exit(main())

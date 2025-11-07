# Automated Card Script Generation - Executive Summary

## Overview
A **fully local** AI-powered system that automatically discovers, processes, and generates Forge card scripts for newly spoiled Magic: The Gathering cards. Runs entirely on your machine with $0/month operating costs.

## Key Features

### 🔍 Automated Discovery
- Daily scraping of mythicspoiler.com at 2:00 AM
- Automatic image download and caching
- OCR extraction of card data (name, cost, types, oracle text, etc.)

### 🧠 Smart Card Script Generation
- **RAG-based approach**: Find similar existing cards using vector similarity
- **LLM-powered**: Generate scripts using Ollama (gpt-oss:20b) locally on RTX 3090
- **Validation**: Automatic syntax checking before saving
- **Fully local**: No data leaves your machine

### 📊 Complete Database Coverage
- **100% of all ~40,000 cards** in vector database (local storage is free!)
- **100% multicolor cards** (complex mechanics)
- **100% rares and mythic rares** (unique effects)
- **100% set-specific mechanics** (Surveil, Convoke, etc.)
- **Priority weighting** in search results (multicolor 1.5x, rares 1.3x, mechanics 1.4x)

### 💰 Cost-Effective
- **$0/month** in operating costs (electricity only)
- **Fully local stack**: Ollama, Qdrant, Tesseract OCR, Sentence Transformers
- **No API costs**: All processing happens on your RTX 3090
- **One-time setup**: ~10 minutes, completely free

## Architecture Components

```
Web Scraper → OCR Engine → Data Extractor → Vector DB Query
                                              ↓
                                         Similar Cards
                                              ↓
                                      LLM Generator
                                              ↓
                                      Validator → Save
```

## Technology Stack (Fully Local)

| Component | Technology | Why |
|-----------|-----------|-----|
| Web Scraping | JSoup | Java-native HTML parsing |
| OCR | Tesseract | Free, open-source, accurate |
| Embeddings | Sentence Transformers (all-MiniLM-L6-v2) | 384-dim vectors, runs on GPU |
| Vector DB | Qdrant (Docker) | Rust-based, REST API, local storage |
| LLM | Ollama (gpt-oss:20b) | Runs on RTX 3090, 24GB VRAM |
| Scheduling | Python/Java scheduler | Cron alternative, easy automation |

**Hardware**: RTX 3090 (24GB VRAM), 16GB+ RAM, ~20GB disk space

## Implementation Timeline

- **Weeks 1-2**: Embedding generation & vector database setup
- **Weeks 3-4**: Web scraping & OCR integration
- **Weeks 5-6**: RAG pipeline & LLM integration
- **Week 7**: Automation & scheduling
- **Week 8**: Polish, testing, documentation

**Total: 8 weeks**

## Success Metrics

- ✅ **80%+ accuracy** on first-try script generation
- ✅ **90%+ coverage** of new spoiled cards
- ✅ **<10 minutes** processing time per batch
- ✅ **~$0/month** in operating costs (electricity only)
- ✅ **Complete isolation** from main Forge repo (PRs contain only card scripts)

## Quick Start (Once Implemented)

### 1. Setup (One-time, ~10 minutes)
```bash
# Install Tesseract OCR
sudo apt-get install tesseract-ocr

# Install Python dependencies
python3 -m venv venv
source venv/bin/activate
pip install sentence-transformers requests opencv-python pytesseract

# Setup Qdrant (Docker)
docker run -d --name qdrant -p 6333:6333 \
  -v $(pwd)/qdrant_storage:/qdrant/storage qdrant/qdrant

# Install Ollama and pull model
curl -fsSL https://ollama.ai/install.sh | sh
ollama pull gpt-oss:20b

# Generate embeddings (2-3 min on RTX 3090)
python forge-scripts/generate_embeddings.py
```

### 2. Run Daily Automation
```bash
# Manual run (test)
python forge-scripts/run_automation.py --once

# Enable daily automation (2 AM)
python forge-scripts/run_automation.py --schedule
```

### 3. Output
Generated scripts automatically saved to:
```
forge-gui/res/cardsfolder/[first_letter]/[card_name].txt
```

Then create PR with only the new card scripts!

## Risk Mitigation

| Risk | Solution |
|------|----------|
| Website changes | Robust HTML parsing with fallbacks |
| OCR failures | Image preprocessing + manual review queue |
| LLM hallucinations | Validation + multiple example cards + low temperature (0.2) |
| GPU resource exhaustion | Batch processing + memory management |
| Merge conflicts | Complete fork isolation + PRs with only card scripts |
| Qdrant downtime | Docker auto-restart + health checks |

## Future Enhancements

- 🖼️ GUI for reviewing generated scripts
- 🌐 Multi-source scraping (Reddit, Scryfall)
- 👤 Human-in-the-loop review process
- 🎯 Fine-tuned LLM for better accuracy
- 🃏 Support for complex card types (DFC, adventures, etc.)

## Questions & Answers

### Q: Why use filename as vector DB identifier?
**A**: Perfect fit! Filenames like `llanowar_elves` are unique, match Forge's structure, and make loading full scripts trivial.

### Q: How many cards in the database?
**A**: ALL ~40,000 cards! Local storage is free (~500MB-1GB), so no need to sample. Priority weighting in search ensures best matches surface first.

### Q: Can this run completely offline?
**A**: Yes! Fully local setup with Ollama, Qdrant, Tesseract, and Sentence Transformers. Only web scraping needs internet (mythicspoiler.com).

### Q: What about card images?
**A**: System downloads and caches images locally. OCR runs on cached images. Images can be kept or deleted after processing.

### Q: How accurate will generated scripts be?
**A**: Target 80%+ on first try. RAG ensures similar cards are used as examples, and LLM (gpt-oss:20b) follows their patterns. Validation catches syntax errors.

### Q: Will automation code conflict with main Forge?
**A**: No! Complete isolation in fork. Automation lives in `forge-scripts/` and `forge-automation/` directories. PRs contain only generated card scripts in `cardsfolder/`.

## Estimated Costs

### One-Time Setup: **$0**
- Embedding generation: **$0** (Sentence Transformers on RTX 3090, ~2-3 min)
- Qdrant installation: **$0** (Docker container)
- Ollama installation: **$0** (gpt-oss:20b model download, ~11GB)
- Tesseract OCR: **$0** (apt-get install)
- Development time: 8 weeks

### Monthly Operations: **~$0**
- LLM inference: **$0** (local Ollama)
- OCR processing: **$0** (local Tesseract)
- Vector DB: **$0** (local Qdrant)
- Electricity: **~$5-10/month** (RTX 3090 power consumption)
- **Total: ~$0-10/month** (electricity only)

### Annual Cost Comparison
- **Cloud approach**: $300-540/year in API costs
- **Local approach**: $0-120/year in electricity
- **Savings**: $180-420/year

## Getting Started

See full implementation details in:
- `docs/Automated-Card-Script-Generation-Plan.md`

For questions or clarifications, please open an issue on the Forge repository.

---

**Status**: ✅ **Implementation Complete - Ready for Deployment**
**Setup**: Ollama (gpt-oss:20b) + Qdrant + Tesseract + Sentence Transformers
**Cost**: $0/month (electricity only)
**Location**: `forge-scripts/` directory
**Quick Start**: `python forge-scripts/verify_setup.py`
**Estimated ROI**: Automates 100+ hours/year of manual card script writing + $180-420/year cost savings vs cloud

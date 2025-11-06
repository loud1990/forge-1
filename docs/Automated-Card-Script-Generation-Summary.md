# Automated Card Script Generation - Executive Summary

## Overview
An AI-powered system that automatically discovers, processes, and generates Forge card scripts for newly spoiled Magic: The Gathering cards.

## Key Features

### 🔍 Automated Discovery
- Daily scraping of mythicspoiler.com at 2:00 AM
- Automatic image download and caching
- OCR extraction of card data (name, cost, types, oracle text, etc.)

### 🧠 Smart Card Script Generation
- **RAG-based approach**: Find similar existing cards using vector similarity
- **LLM-powered**: Generate scripts using Claude/GPT-4 with context from similar cards
- **Validation**: Automatic syntax checking before saving

### 📊 Intelligent Sampling Strategy
- **60-70% database reduction** (40K → 12-15K cards)
- **100% multicolor cards** (complex mechanics)
- **100% mythic rares** (unique effects)
- **Set-specific mechanics** (Surveil, Convoke, etc.)
- Rarity-based sampling: 70% rare, 40% uncommon, 20% common

### 💰 Cost-Effective
- **Est. $25-45/month** for API costs
- Local alternatives available (Sentence Transformers, Tesseract OCR)
- Embedding generation: one-time $5-10 or free with local models

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

## Technology Stack

| Component | Technology | Alternative |
|-----------|-----------|-------------|
| Web Scraping | JSoup | - |
| OCR | Tesseract | Google Cloud Vision |
| Embeddings | Sentence Transformers | OpenAI ada-002 |
| Vector DB | ChromaDB (local) | Pinecone (cloud) |
| LLM | Claude 3.5 Sonnet | GPT-4 Turbo |
| Scheduling | Java ScheduledExecutor | Cron |

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
- ✅ **<$50/month** in API costs

## Quick Start (Once Implemented)

### 1. Setup
```bash
# Install Python dependencies
pip install -r forge-scripts/requirements.txt

# Generate embeddings (one-time)
python forge-scripts/generate_embeddings.py

# Configure API keys
export ANTHROPIC_API_KEY="your-key-here"
```

### 2. Run
```bash
# Manual run
java -cp forge.jar forge.ai.automation.CardScriptScheduler --run-once

# Enable daily automation
java -cp forge.jar forge.ai.automation.CardScriptScheduler --schedule
```

### 3. Output
Generated scripts saved to:
```
~/.forge/custom/cards/[first_letter]/[card_name].txt
```

## Risk Mitigation

| Risk | Solution |
|------|----------|
| Website changes | Robust HTML parsing with fallbacks |
| OCR failures | Multiple engines + manual review queue |
| LLM hallucinations | Validation + multiple example cards |
| API costs | Local model fallbacks + monitoring |

## Future Enhancements

- 🖼️ GUI for reviewing generated scripts
- 🌐 Multi-source scraping (Reddit, Scryfall)
- 👤 Human-in-the-loop review process
- 🎯 Fine-tuned LLM for better accuracy
- 🃏 Support for complex card types (DFC, adventures, etc.)

## Questions & Answers

### Q: Why use filename as vector DB identifier?
**A**: Perfect fit! Filenames like `llanowar_elves` are unique, match Forge's structure, and make loading full scripts trivial.

### Q: How much database space savings?
**A**: ~60-70% reduction (40K → 12-15K cards) while maintaining comprehensive mechanic coverage through intelligent sampling.

### Q: Can this run completely offline?
**A**: Partially. Use Tesseract (OCR) + Sentence Transformers (embeddings) + local LLM (llama.cpp). Only web scraping needs internet.

### Q: What about card images?
**A**: System downloads and caches images locally. OCR runs on cached images. Images can be kept or deleted after processing.

### Q: How accurate will generated scripts be?
**A**: Target 80%+ on first try. RAG ensures similar cards are used as examples, and LLM follows their patterns. Validation catches syntax errors.

## Estimated Costs

### One-Time Setup
- Embedding generation: $0-10 (free with local models)
- Development time: 8 weeks

### Monthly Operations
- LLM API: $10-30 (100-300 cards/month)
- OCR API (optional): $15
- Vector DB: $0 (local) or $70 (Pinecone)
- **Total: $25-45/month** (local setup)

## Getting Started

See full implementation details in:
- `docs/Automated-Card-Script-Generation-Plan.md`

For questions or clarifications, please open an issue on the Forge repository.

---

**Status**: 📋 Planning Phase
**Next Step**: Phase 1 - Foundation (Embedding Generation)
**Target Completion**: 8 weeks from start
**Estimated ROI**: Automates 100+ hours/year of manual card script writing

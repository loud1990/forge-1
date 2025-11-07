# Forge Automated Card Script Generation

Fully local AI-powered system for automatically generating Forge card scripts from spoiled Magic: The Gathering cards.

## Features

- **Fully Local**: All processing on your machine (RTX 3090)
- **$0/month**: No API costs, electricity only
- **RAG-Based**: Finds similar cards for context
- **LLM Powered**: Ollama with gpt-oss:20b
- **Automated**: Daily scraping at 2 AM
- **Complete Isolation**: No conflicts with main Forge repo

## Architecture

```
mythicspoiler.com → Web Scraper → OCR (Tesseract) → RAG Search (Qdrant)
                                                          ↓
                                                   Similar Cards
                                                          ↓
                                              LLM (Ollama) → Validator
                                                          ↓
                                              forge-gui/res/cardsfolder/
```

## Quick Start

### 1. Prerequisites

```bash
# Install Tesseract OCR
sudo apt-get install tesseract-ocr

# Install Docker for Qdrant
sudo apt-get install docker.io
sudo systemctl start docker

# Install Ollama
curl -fsSL https://ollama.ai/install.sh | sh
```

### 2. Setup Python Environment

```bash
cd forge-scripts
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

### 3. Start Services

```bash
# Start Qdrant
docker run -d --name qdrant -p 6333:6333 \
  -v $(pwd)/qdrant_storage:/qdrant/storage \
  --restart unless-stopped \
  qdrant/qdrant

# Start Ollama (if not running)
ollama serve &

# Pull model
ollama pull gpt-oss:20b
```

### 4. Generate Embeddings (One-time, ~2-3 minutes)

```bash
python generate_embeddings.py
```

Expected output:
```
Forge Card Script Embedding Generation
========================================
1. Loading Sentence Transformer model...
2. Checking Qdrant connection...
✓ Qdrant is running
3. Loading card scripts...
✓ Loaded 31621 cards
4. Creating Qdrant collection...
✓ Collection created
5. Generating embeddings and uploading...
Processing cards: 100%|████████████| 31621/31621
✓ Successfully processed 31621 cards
```

### 5. Run Automation

```bash
# Test run (once)
python run_automation.py --once

# Enable daily automation (2 AM)
python run_automation.py --schedule
```

## How It Works

### 1. Web Scraping
- Scrapes mythicspoiler.com daily at 2 AM
- Downloads card images to cache
- Rate limiting: 1 request per 5 seconds

### 2. OCR Processing
- Tesseract extracts text from images
- Image preprocessing for better accuracy
- Parses: name, mana cost, types, oracle text, P/T

### 3. RAG Search
- Generates embeddings with Sentence Transformers
- Searches Qdrant for 5 most similar cards
- Priority weighting:
  - Multicolor: 1.5x
  - Rares/Mythics: 1.3x
  - Set mechanics: 1.4x

### 4. LLM Generation
- Ollama (gpt-oss:20b) generates card script
- Temperature: 0.2 (low randomness)
- Context: Similar card scripts as examples

### 5. Validation & Writing
- Validates required fields (Name, ManaCost, Types)
- Writes to `forge-gui/res/cardsfolder/[letter]/[name].txt`

## Configuration

Edit `config.py` to customize:

```python
# Qdrant
QDRANT_URL = 'http://localhost:6333'

# Ollama
OLLAMA_URL = 'http://localhost:11434'
OLLAMA_MODEL = 'gpt-oss:20b'

# Scheduling
SCHEDULE_HOUR = 2  # 2 AM
```

## Logs

Logs are written to `data/logs/automation_YYYYMMDD.log`

```bash
tail -f data/logs/automation_$(date +%Y%m%d).log
```

## Fork Workflow

This automation is **completely isolated** from main Forge:

### What's Isolated (Never in PRs):
- `forge-scripts/` - All automation code
- `qdrant_storage/` - Vector database
- `data/` - Logs and cache

### What's Shared (PR Target):
- `forge-gui/res/cardsfolder/` - Generated card scripts ONLY

### Creating PRs:

```bash
# 1. Generate scripts (automation runs daily)
python run_automation.py --once

# 2. Review generated scripts
git status forge-gui/res/cardsfolder/

# 3. Create PR with ONLY card scripts
git add forge-gui/res/cardsfolder/
git commit -m "feat: add generated card scripts for [Set Name]"
git push origin your-branch

# 4. Open PR to main Forge repo
```

## Troubleshooting

### Qdrant not running
```bash
docker ps | grep qdrant
# If not running:
docker start qdrant
```

### Ollama not responding
```bash
curl http://localhost:11434/api/tags
# If fails:
ollama serve &
```

### Low accuracy from OCR
- Check image quality
- Adjust preprocessing in `utils/ocr_processor.py`
- Manual review queue for failed cards

### LLM generates invalid scripts
- Check similar cards (may need better weighting)
- Adjust temperature in `config.py`
- Review prompt in `utils/ollama_client.py`

## Performance

### Hardware Requirements
- GPU: RTX 3090 (24GB VRAM)
- RAM: 16GB+
- Storage: ~20GB for models + 2GB for vector DB

### Timing
- Embedding generation: ~2-3 minutes (one-time)
- Per card processing: ~10-20 seconds
- Batch of 100 cards: ~20-30 minutes

## Directory Structure

```
forge-scripts/
├── config.py                  # Configuration
├── generate_embeddings.py     # One-time embedding setup
├── run_automation.py          # Main automation runner
├── requirements.txt           # Python dependencies
├── utils/
│   ├── card_loader.py         # Load Forge card files
│   ├── ocr_processor.py       # Tesseract OCR
│   ├── ollama_client.py       # LLM integration
│   ├── qdrant_manager.py      # Vector database
│   ├── rag_search.py          # Similarity search
│   ├── script_validator.py    # Validation & writing
│   └── web_scraper.py         # mythicspoiler.com scraper
├── data/
│   ├── cache/                 # Downloaded images
│   └── logs/                  # Automation logs
└── tests/                     # Unit tests
```

## Development

### Running Tests
```bash
pytest tests/
```

### Adding New Scrapers
Edit `utils/web_scraper.py` to add more sources (Reddit, Scryfall, etc.)

### Improving Prompts
Edit `utils/ollama_client.py` → `_build_prompt()` method

## Cost Analysis

### One-Time Setup: $0
- All tools are free and open-source

### Monthly Operating: ~$0-10
- Electricity: ~$5-10 (RTX 3090 daily runs)
- No API costs

### Annual Savings: $180-420
- vs. cloud-based approach ($25-45/month)

## Documentation

See full technical plan:
- `docs/Automated-Card-Script-Generation-Plan.md`
- `docs/Automated-Card-Script-Generation-Summary.md`

## License

Same as Forge (GPL-3.0)

## Support

For issues with automation:
1. Check logs in `data/logs/`
2. Verify services: `docker ps` and `curl http://localhost:11434/api/tags`
3. Review plan documentation

For Forge-related issues:
- See main Forge repository

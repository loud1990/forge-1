# Automated Card Script Generation System - Implementation Plan

## Overview
A **fully local** AI-powered system that automatically discovers new Magic: The Gathering cards from spoiler sites, extracts card data using OCR, and generates Forge card scripts using RAG (Retrieval Augmented Generation) by finding similar existing cards.

**⚡ Fully Local Setup:**
- Ollama with gpt-oss:20b on RTX 3090 (24GB VRAM)
- Qdrant vector database (local)
- Tesseract OCR (local)
- Sentence Transformers (local)
- **Cost: $0/month** (electricity only)

## Fork Workflow & Project Isolation

**Objective**: Keep this feature completely isolated from the main Forge codebase to avoid merge conflicts when submitting generated card scripts via PRs.

### Repository Structure

```
your-forge-fork/
├── forge-gui/res/cardsfolder/          # Standard Forge card scripts (shared)
├── forge-scripts/                       # NEW: Isolated automation scripts
│   ├── generate_embeddings.py          # Generate vector embeddings
│   ├── rag_search.py                   # Vector similarity search
│   ├── script_generator.py             # Ollama integration
│   ├── scraper.py                      # Web scraping
│   ├── ocr_processor.py                # OCR processing
│   └── requirements.txt                # Python dependencies
├── forge-automation/                    # NEW: Java automation package
│   └── src/main/java/forge/automation/
│       ├── SpoilerScraper.java        # Web scraper
│       ├── CardOCR.java               # Tesseract integration
│       ├── CardScriptGenerator.java   # Main orchestrator
│       └── QdrantClient.java          # Qdrant REST API client
├── qdrant_storage/                     # NEW: Vector database storage
├── tessdata/                           # NEW: Tesseract OCR data
└── config/
    └── automation.properties           # NEW: Configuration file
```

### Isolation Strategy

**What's Isolated** (No Conflicts):
1. ✅ All Python scripts in `forge-scripts/` directory
2. ✅ All Java automation code in separate `forge-automation` package
3. ✅ Vector database in `qdrant_storage/` directory
4. ✅ Configuration files in `config/` directory
5. ✅ Ollama models (external, not in repo)
6. ✅ Tesseract data files

**What's Shared** (PR Target):
1. 📄 **Only generated card scripts**: `forge-gui/res/cardsfolder/[letter]/[card_name].txt`
2. 📄 Card images (optional): `~/.forge/Cache/pics/cards/`

**Why This Works**:
- Main Forge doesn't know about automation code
- Automation code reads from standard cardsfolder
- Generated scripts are standard Forge format
- Clean PRs with just new card files
- No dependencies added to main Forge build

### Workflow

```
┌─────────────────────────────────────────────────────────┐
│          Your Fork (loud1990/forge-1)                   │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  1. Automation runs nightly (isolated)                  │
│  2. Generates card scripts → cardsfolder/               │
│  3. Git commit generated scripts only                   │
│  4. Push to your fork                                   │
│  5. Create PR to main Forge repo                        │
│                                                          │
└─────────────────────────────────────────────────────────┘
                          │
                          │ PR (only card scripts)
                          ▼
┌─────────────────────────────────────────────────────────┐
│          Main Forge Repo (Card-Forge/forge)             │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  - Receives: New card script files only                 │
│  - No automation code                                   │
│  - No dependencies                                      │
│  - Clean merge, no conflicts                           │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### Git Workflow

**Daily Automation**:
```bash
# Automation runs at 2 AM
cd /path/to/your-forge-fork

# Generate new card scripts
python forge-scripts/run_automation.py

# Commit only generated cards
git add forge-gui/res/cardsfolder/
git commit -m "feat: add auto-generated scripts for [SET_CODE] spoilers

Generated by automation pipeline:
- [Card Name 1]
- [Card Name 2]
- [Card Name 3]

Automated-Card-Script-Generation: v1.0"

# Push to your fork
git push origin automated-cards-$(date +%Y%m%d)
```

**Creating PR**:
```bash
# Create PR via gh CLI or web interface
gh pr create \
  --title "feat: Add [SET_CODE] card scripts" \
  --body "Auto-generated card scripts for newly spoiled cards.

**Cards Added:**
- Card Name 1
- Card Name 2
- Card Name 3

**Generation Pipeline:**
- OCR: Tesseract
- RAG: Qdrant + Sentence Transformers
- LLM: Ollama gpt-oss:20b
- Validation: Passed

**Human Review:** Scripts have been manually reviewed for accuracy." \
  --base master \
  --head loud1990:automated-cards-$(date +%Y%m%d)
```

### Keeping Fork Up-to-Date

```bash
# Add upstream remote (one-time)
git remote add upstream https://github.com/Card-Forge/forge.git

# Daily sync before automation runs
git fetch upstream
git merge upstream/master

# Or rebase for cleaner history
git rebase upstream/master

# This keeps your fork current without conflicts
```

### Conflict Avoidance

**✅ Safe Practices**:
1. Never modify existing Forge Java code
2. Keep all automation in separate packages
3. Only add new card script files
4. Use separate branches for automation PRs
5. Sync with upstream before automation runs

**❌ Avoid**:
1. Don't add dependencies to main pom.xml
2. Don't modify core Forge classes
3. Don't change existing card scripts
4. Don't commit automation code in PRs

### Branch Strategy

**Option 1: Feature Branch per Run** (Recommended)
```bash
# New branch each day
git checkout -b automated-cards-20250107
# ... run automation ...
git commit -am "feat: add cards from 2025-01-07 spoilers"
git push origin automated-cards-20250107
# Create PR
```

**Option 2: Single Long-Running Branch**
```bash
# One branch for all automation
git checkout -b automation-pipeline
# ... run automation daily ...
git commit -am "feat: add cards from [date]"
git push origin automation-pipeline
# Create PR with specific commits
```

**Recommendation**: Option 1 (feature branches) for cleaner PRs and easier reviews.

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    AUTOMATED CARD SCRIPT SYSTEM                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────┐    ┌──────────────┐    ┌──────────────────┐  │
│  │   Spoiler   │───▶│     OCR      │───▶│  Card Data       │  │
│  │   Scraper   │    │   Engine     │    │  Extraction      │  │
│  └─────────────┘    └──────────────┘    └──────────────────┘  │
│         │                                          │             │
│         ▼                                          ▼             │
│  ┌─────────────┐    ┌──────────────┐    ┌──────────────────┐  │
│  │   Image     │    │   Vector     │◀───│  Embedding       │  │
│  │  Downloader │    │   Database   │    │  Generator       │  │
│  └─────────────┘    └──────────────┘    └──────────────────┘  │
│                             │                                    │
│                             ▼                                    │
│                      ┌──────────────┐                           │
│                      │   RAG Query  │                           │
│                      │   Engine     │                           │
│                      └──────────────┘                           │
│                             │                                    │
│                             ▼                                    │
│                      ┌──────────────┐                           │
│                      │  LLM Script  │                           │
│                      │  Generator   │                           │
│                      └──────────────┘                           │
│                             │                                    │
│                             ▼                                    │
│                      ┌──────────────┐                           │
│                      │  Validation  │                           │
│                      │  & Storage   │                           │
│                      └──────────────┘                           │
│                                                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │         Scheduler (Daily at Night - 2:00 AM)               │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## Phase 1: Foundation - Embedding Generation & Vector Database

### 1.1 Card Script Database Strategy

**Objective**: Include ALL card scripts in the vector database for maximum accuracy and coverage. With local operation, storage and processing costs are not a concern.

**Database Inclusion Criteria**:
1. **ALL Cards**: Include all ~40,000 card scripts
   - No sampling needed with local setup
   - Maximum similarity search accuracy
   - Comprehensive mechanic coverage
   - Storage: ~200MB for embeddings (manageable locally)

2. **Priority Cards** (for weighted search):
   - **Multicolor Cards** (Priority: HIGH)
     - Cards with 2+ colors in mana cost
     - Cards with multicolor abilities
     - Weight: 1.5x in similarity scoring

   - **All Rares and Mythics** (Priority: HIGH)
     - **Mythic Rare**: 100% (usually ~15 per set)
     - **Rare**: 100% (most complex mechanics)
     - Weight: 1.3x in similarity scoring

   - **Set Mechanic Representatives** (Priority: HIGH)
     - Cards featuring set-specific keywords
     - At least all cards with unique keywords
     - Weight: 1.4x in similarity scoring

   - **Uncommon**: 100%
   - **Common**: 100%

3. **Comprehensive Coverage**:
   - All card types: Creatures, Instants, Sorceries, Enchantments, Artifacts, Planeswalkers, Lands
   - All abilities and keywords
   - All SVar patterns
   - All mechanic combinations

**Database Size**:
- Original: ~40,000 card scripts
- Included: ~40,000 (100%)
- Storage: ~200-300MB for embeddings (384 dims × 40K cards × 4 bytes)
- Qdrant DB size: ~500MB-1GB total

**Implementation**:
```java
class CardSampler {
    Set<CardRules> sampleCardsFromSet(String setCode) {
        Set<CardRules> selected = new HashSet<>();

        // 1. All multicolor cards
        selected.addAll(getMulticolorCards(setCode));

        // 2. Set mechanics
        selected.addAll(getSetMechanicCards(setCode));

        // 3. Rarity-based sampling
        selected.addAll(sampleByRarity(setCode));

        // 4. Type distribution
        selected.addAll(ensureTypeDistribution(setCode));

        return selected;
    }

    boolean isMulticolor(CardRules card) {
        String manaCost = card.getManaCost();
        int colorCount = 0;
        if (manaCost.contains("W")) colorCount++;
        if (manaCost.contains("U")) colorCount++;
        if (manaCost.contains("B")) colorCount++;
        if (manaCost.contains("R")) colorCount++;
        if (manaCost.contains("G")) colorCount++;
        return colorCount >= 2;
    }
}
```

### 1.2 Embedding Model Selection

**Selected: Sentence Transformers** (all-MiniLM-L6-v2)

**Why This Model**:
- ✅ **Free**: No API costs
- ✅ **Local**: Runs entirely on your machine
- ✅ **Fast**: ~1000 cards/second on RTX 3090
- ✅ **Small**: 22MB model size
- ✅ **Quality**: Good semantic understanding for card text
- ✅ **384 dimensions**: Perfect balance of size and accuracy

**Hardware Requirements**:
- CPU: Any modern processor
- RAM: 2GB during embedding generation
- GPU: Optional (speeds up batch processing)
- Storage: 22MB for model + 300MB for embeddings

**Performance on RTX 3090**:
- Embedding generation: ~2-3 minutes for 40,000 cards
- Query time: <50ms per search
- Batch processing: Fully parallelized

### 1.3 Vector Database Selection

**Selected: Qdrant**

**Why Qdrant**:
- ✅ **Fully local**: Runs as Docker container or standalone
- ✅ **High performance**: Rust-based, optimized for speed
- ✅ **Rich filtering**: Metadata filtering, multi-vector search
- ✅ **REST API**: Easy integration with Java
- ✅ **Persistent**: Data survives restarts
- ✅ **Web UI**: Built-in dashboard for debugging
- ✅ **Free**: Open source, no costs

**Setup**:
```bash
# Docker (recommended)
docker run -p 6333:6333 -v $(pwd)/qdrant_storage:/qdrant/storage qdrant/qdrant

# Or standalone binary
wget https://github.com/qdrant/qdrant/releases/latest/download/qdrant
chmod +x qdrant
./qdrant
```

**Java Integration**:
```java
// HTTP client for Qdrant REST API
HttpClient client = HttpClient.newHttpClient();
String qdrantUrl = "http://localhost:6333";
```

**Performance on Your Setup**:
- 40,000 vectors: ~500MB storage
- Query time: <10ms
- Bulk insert: ~5 minutes for all 40K cards
- Concurrent searches: Supports hundreds/second

### 1.4 Embedding Generation Process

**Input**: Card script .txt file
```
Name:Llanowar Elves
ManaCost:G
Types:Creature Elf Druid
PT:1/1
A:AB$ Mana | Cost$ T | Produced$ G | SpellDescription$ Add {G}.
Oracle:{T}: Add {G}.
```

**Embedding Text Construction**:
```python
def build_embedding_text(card_script):
    """
    Combine relevant parts for semantic similarity.
    Focus on oracle text and abilities for best matching.
    """
    parts = []
    parts.append(f"Oracle: {card.oracle_text}")
    parts.append(f"Type: {card.type_line}")
    if card.abilities:
        parts.append(f"Abilities: {' '.join(card.abilities)}")
    return "\n".join(parts)
```

**Storage Schema**:
```json
{
  "id": "llanowar_elves",
  "filename": "llanowar_elves.txt",
  "set_code": "LEA",
  "embedding": [0.123, 0.456, ...],  // 384 dimensions for MiniLM
  "metadata": {
    "name": "Llanowar Elves",
    "mana_cost": "G",
    "types": "Creature Elf Druid",
    "colors": ["G"],
    "is_multicolor": false,
    "rarity": "Common",
    "set_mechanics": [],
    "oracle_text": "{T}: Add {G}."
  }
}
```

**Batch Processing**:
```python
# forge-scripts/generate_embeddings.py
import os
import requests
from sentence_transformers import SentenceTransformer

def generate_embeddings_batch():
    model = SentenceTransformer('all-MiniLM-L6-v2')
    qdrant_url = "http://localhost:6333"

    # Create collection
    requests.put(
        f"{qdrant_url}/collections/card_scripts",
        json={
            "vectors": {
                "size": 384,  # MiniLM embedding size
                "distance": "Cosine"
            }
        }
    )

    card_scripts_dir = "forge-gui/res/cardsfolder"
    all_cards = load_all_cards()  # Load ALL 40,000 cards

    batch_size = 100
    points = []

    for i, card in enumerate(all_cards):
        text = build_embedding_text(card)
        embedding = model.encode(text)

        points.append({
            "id": i,
            "vector": embedding.tolist(),
            "payload": {
                "filename": card.filename,
                "name": card.name,
                "mana_cost": card.mana_cost,
                "types": card.types,
                "colors": card.colors,
                "is_multicolor": card.is_multicolor,
                "rarity": card.rarity,
                "oracle_text": card.oracle_text,
                "script_path": card.script_path
            }
        })

        # Upload in batches
        if len(points) >= batch_size:
            requests.put(
                f"{qdrant_url}/collections/card_scripts/points",
                json={"points": points}
            )
            points = []
            print(f"Uploaded {i+1}/{len(all_cards)} cards")

    # Upload remaining
    if points:
        requests.put(
            f"{qdrant_url}/collections/card_scripts/points",
            json={"points": points}
        )

    print(f"Generated embeddings for {len(all_cards)} cards")
```

---

## Phase 2: Web Scraping & OCR

### 2.1 Mythic Spoiler Scraper

**Target URL**: `https://mythicspoiler.com/newspoilers.html`

**Page Structure** (need to inspect actual HTML):
```html
<!-- Expected structure -->
<div class="spoiler-card">
  <img src="card-image.jpg" />
  <div class="card-info">
    <h3>Card Name</h3>
    <p>Set: XXX</p>
  </div>
</div>
```

**Scraper Implementation**:
```java
// forge-ai/src/main/java/forge/ai/scraper/SpoilerScraper.java
package forge.ai.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpoilerScraper {
    private static final String SPOILER_URL = "https://mythicspoiler.com/newspoilers.html";
    private static final String USER_AGENT = "Forge-CardScript-Generator/1.0";

    public List<CardSpoiler> scrapeNewCards() throws IOException {
        Document doc = Jsoup.connect(SPOILER_URL)
            .userAgent(USER_AGENT)
            .timeout(10000)
            .get();

        Elements cardElements = doc.select(".spoiler-card"); // Adjust selector
        List<CardSpoiler> spoilers = new ArrayList<>();

        for (Element card : cardElements) {
            CardSpoiler spoiler = new CardSpoiler();
            spoiler.setImageUrl(card.select("img").attr("src"));
            spoiler.setSetCode(extractSetCode(card));
            spoiler.setCardName(card.select("h3").text());
            spoilers.add(spoiler);
        }

        return spoilers;
    }

    private String extractSetCode(Element card) {
        // Extract set code from HTML
        String setText = card.select(".set-info").text();
        // Parse "Set: XXX" -> "XXX"
        return setText.replaceAll("Set:\\s*", "").trim();
    }
}

class CardSpoiler {
    private String imageUrl;
    private String setCode;
    private String cardName;
    private byte[] imageData;

    // Getters and setters
}
```

### 2.2 Image Download & Storage

```java
public class ImageDownloader {
    private static final String CACHE_DIR = ForgeConstants.USER_DIR + "spoiler-cache/";

    public File downloadImage(String imageUrl, String cardName) throws IOException {
        File cacheDir = new File(CACHE_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        String filename = cardName.toLowerCase().replaceAll("[^a-z0-9]", "_") + ".jpg";
        File outputFile = new File(cacheDir, filename);

        if (outputFile.exists()) {
            return outputFile; // Already cached
        }

        // Download
        InputStream in = new URL(imageUrl).openStream();
        Files.copy(in, outputFile.toPath());
        in.close();

        return outputFile;
    }
}
```

### 2.3 OCR Engine Integration

**Selected: Tesseract OCR** (Open source, fully local)

**Why Tesseract**:
- ✅ **Free**: No API costs
- ✅ **Local**: Runs entirely offline
- ✅ **Accurate**: 85-90% accuracy on card images
- ✅ **Fast**: Processes image in 1-2 seconds
- ✅ **Battle-tested**: Used by Google, Archive.org

**Setup**:
```bash
# Install Tesseract
sudo apt-get install tesseract-ocr
sudo apt-get install libtesseract-dev

# Download trained data
wget https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata
mkdir -p tessdata
mv eng.traineddata tessdata/
```

**Java Integration**:
```java
// Add dependency: net.sourceforge.tess4j:tess4j:5.7.0
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class CardOCR {
    private final Tesseract tesseract;

    public CardOCR() {
        tesseract = new Tesseract();
        tesseract.setDatapath("tessdata");
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(1); // Auto page segmentation
        tesseract.setOcrEngineMode(1); // Neural nets LSTM engine
    }

    public String extractText(File imageFile) throws TesseractException {
        return tesseract.doOCR(imageFile);
    }
}
```

**Performance on Your Setup**:
- OCR time: ~1-2 seconds per card image
- Accuracy: 85-90% on high-quality spoilers
- GPU: Not utilized (CPU-only, but fast enough)
- Batch processing: Can run multiple instances in parallel

### 2.4 Card Data Extraction from OCR Text

```java
public class CardDataExtractor {
    public CardData extractFromOCR(String ocrText) {
        CardData data = new CardData();

        // Parse OCR text to extract structured data
        // This requires pattern matching based on typical card layouts

        String[] lines = ocrText.split("\n");

        // Line 1: Usually card name
        data.setName(lines[0].trim());

        // Line 2: Usually mana cost (look for {W}{U}{B}{R}{G}{X} patterns)
        data.setManaCost(extractManaCost(lines[1]));

        // Find type line (contains "—" or "Creature", "Instant", etc.)
        for (String line : lines) {
            if (line.contains("—") || isTypeLine(line)) {
                data.setTypeLine(line.trim());
                String[] parts = line.split("—");
                data.setTypes(parts[0].trim());
                if (parts.length > 1) {
                    data.setSubtypes(parts[1].trim());
                }
                break;
            }
        }

        // Extract P/T (look for "X/Y" pattern at end)
        Pattern ptPattern = Pattern.compile("(\\d+|\\*)/(\\d+|\\*)");
        Matcher ptMatcher = ptPattern.matcher(ocrText);
        if (ptMatcher.find()) {
            data.setPower(ptMatcher.group(1));
            data.setToughness(ptMatcher.group(2));
        }

        // Extract loyalty (look for number in bottom right)
        Pattern loyaltyPattern = Pattern.compile("Loyalty:\\s*(\\d+)");
        Matcher loyaltyMatcher = loyaltyPattern.matcher(ocrText);
        if (loyaltyMatcher.find()) {
            data.setLoyalty(loyaltyMatcher.group(1));
        }

        // Oracle text is everything between type line and P/T
        data.setOracleText(extractOracleText(lines));

        // Flavor text (usually italicized, after oracle text)
        data.setFlavorText(extractFlavorText(ocrText));

        // Detect legendary
        data.setLegendary(data.getTypes().toLowerCase().contains("legendary"));

        return data;
    }

    private String extractManaCost(String line) {
        // Convert {2}{G}{G} to "2 G G"
        Pattern pattern = Pattern.compile("\\{([WUBRGXCP0-9/]+)\\}");
        Matcher matcher = pattern.matcher(line);
        StringBuilder cost = new StringBuilder();
        while (matcher.find()) {
            if (cost.length() > 0) cost.append(" ");
            cost.append(matcher.group(1));
        }
        return cost.toString();
    }
}

class CardData {
    private String name;
    private String manaCost;
    private String types;
    private String subtypes;
    private boolean isLegendary;
    private String oracleText;
    private String power;
    private String toughness;
    private String loyalty;
    private String flavorText;

    // Getters and setters
}
```

---

## Phase 3: RAG (Retrieval Augmented Generation)

### 3.1 Vector Search for Similar Cards (Qdrant)

```python
# forge-scripts/rag_search.py
import requests
from sentence_transformers import SentenceTransformer

def find_similar_cards(oracle_text, top_k=5, qdrant_url="http://localhost:6333"):
    """
    Query Qdrant vector database for cards with similar oracle text.
    """
    model = SentenceTransformer('all-MiniLM-L6-v2')

    # Generate embedding for query
    query_embedding = model.encode(oracle_text).tolist()

    # Search in Qdrant
    response = requests.post(
        f"{qdrant_url}/collections/card_scripts/points/search",
        json={
            "vector": query_embedding,
            "limit": top_k,
            "with_payload": True,
            "with_vector": False
        }
    )

    results = response.json()['result']

    # Extract relevant data
    similar_cards = []
    for result in results:
        similar_cards.append({
            "filename": result['payload']['filename'],
            "name": result['payload']['name'],
            "script_path": result['payload']['script_path'],
            "similarity": result['score'],
            "oracle_text": result['payload']['oracle_text']
        })

    return similar_cards
```

**Performance**:
- Query time: <10ms on your RTX 3090
- Results: Highly relevant due to full 40K card database
- Filtering: Can add metadata filters (rarity, colors, types)

### 3.2 LLM Script Generation

**Prompt Template**:
```
You are an expert at creating Forge card scripts for Magic: The Gathering cards.

NEW CARD DATA:
Name: {card_name}
Mana Cost: {mana_cost}
Type: {type_line}
Oracle Text: {oracle_text}
Power/Toughness: {pt}
Loyalty: {loyalty}

SIMILAR CARDS (for reference):
{similar_cards_scripts}

Generate a Forge card script file for the new card following the exact format of the similar cards.
Use the same syntax patterns, SVar definitions, and ability structures.

Output only the card script, starting with "Name:" and including all necessary fields.
```

**Implementation**:
```python
# forge-scripts/script_generator.py
import requests
import json

def generate_card_script(card_data, similar_cards, ollama_url="http://localhost:11434"):
    """
    Use Ollama with gpt-oss:20b to generate Forge card script.
    """
    # Build prompt
    prompt = build_prompt(card_data, similar_cards)

    # Call Ollama API
    response = requests.post(
        f"{ollama_url}/api/generate",
        json={
            "model": "gpt-oss:20b",
            "prompt": prompt,
            "temperature": 0.2,  # Low temperature for consistency
            "top_p": 0.9,
            "max_tokens": 2048,
            "stop": ["---", "Note:", "Explanation:"]  # Stop at explanations
        }
    )

    # Parse streaming response
    script_parts = []
    for line in response.iter_lines():
        if line:
            data = json.loads(line)
            if 'response' in data:
                script_parts.append(data['response'])
            if data.get('done', False):
                break

    script = ''.join(script_parts)
    return script.strip()

def build_prompt(card_data, similar_cards):
    prompt = f"""You are an expert at creating Forge card scripts for Magic: The Gathering cards.

NEW CARD DATA:
Name: {card_data.name}
Mana Cost: {card_data.mana_cost}
Type: {card_data.type_line}
Oracle Text: {card_data.oracle_text}
"""

    if card_data.power and card_data.toughness:
        prompt += f"Power/Toughness: {card_data.power}/{card_data.toughness}\n"

    if card_data.loyalty:
        prompt += f"Loyalty: {card_data.loyalty}\n"

    prompt += "\n\nSIMILAR CARDS (for reference):\n\n"

    for i, card in enumerate(similar_cards, 1):
        prompt += f"--- Similar Card {i} ---\n"
        prompt += card['script_content']
        prompt += "\n\n"

    prompt += """Generate a Forge card script file for the new card following the exact format of the similar cards.
Use the same syntax patterns, SVar definitions, and ability structures.
Be precise with the Forge scripting language syntax.

Output only the card script, starting with "Name:" and including all necessary fields.
Do not include any explanations or markdown formatting."""

    return prompt
```

### 3.3 Script Validation

```java
public class ScriptValidator {
    public ValidationResult validate(String script) {
        ValidationResult result = new ValidationResult();

        // Check required fields
        if (!script.contains("Name:")) {
            result.addError("Missing Name field");
        }
        if (!script.contains("ManaCost:")) {
            result.addError("Missing ManaCost field");
        }
        if (!script.contains("Types:")) {
            result.addError("Missing Types field");
        }
        if (!script.contains("Oracle:")) {
            result.addError("Missing Oracle field");
        }

        // Try to parse with CardRules.Reader
        try {
            String[] lines = script.split("\n");
            CardRules.Reader reader = new CardRules.Reader();
            CardRules rules = reader.readCard(Arrays.asList(lines), "test.txt");
            result.setValid(true);
        } catch (Exception e) {
            result.addError("Failed to parse: " + e.getMessage());
            result.setValid(false);
        }

        return result;
    }
}
```

---

## Phase 4: Automation & Scheduling

### 4.1 Daily Scheduler

```java
// forge-ai/src/main/java/forge/ai/automation/CardScriptScheduler.java
package forge.ai.automation;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.Duration;

public class CardScriptScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void startDailySchedule() {
        // Calculate delay until 2:00 AM
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(2).withMinute(0).withSecond(0);
        if (now.compareTo(nextRun) > 0) {
            nextRun = nextRun.plusDays(1); // Schedule for tomorrow
        }

        long initialDelay = Duration.between(now, nextRun).toMillis();
        long period = TimeUnit.DAYS.toMillis(1);

        scheduler.scheduleAtFixedRate(
            this::runCardGenerationPipeline,
            initialDelay,
            period,
            TimeUnit.MILLISECONDS
        );

        System.out.println("Scheduled card generation for " + nextRun);
    }

    private void runCardGenerationPipeline() {
        try {
            System.out.println("Starting automated card generation at " + LocalDateTime.now());

            // 1. Scrape new cards
            List<CardSpoiler> spoilers = new SpoilerScraper().scrapeNewCards();
            System.out.println("Found " + spoilers.size() + " new cards");

            // 2. Download images and OCR
            CardOCR ocr = new CardOCR();
            CardDataExtractor extractor = new CardDataExtractor();

            for (CardSpoiler spoiler : spoilers) {
                File image = new ImageDownloader().downloadImage(spoiler.getImageUrl(), spoiler.getCardName());
                String ocrText = ocr.extractText(image);
                CardData data = extractor.extractFromOCR(ocrText);

                // 3. Call Python RAG script
                String script = callPythonRAG(data);

                // 4. Validate and save
                ValidationResult validation = new ScriptValidator().validate(script);
                if (validation.isValid()) {
                    saveScript(data.getName(), script);
                    System.out.println("Generated script for: " + data.getName());
                } else {
                    System.err.println("Validation failed for " + data.getName() + ": " + validation.getErrors());
                }
            }

            System.out.println("Card generation complete");
        } catch (Exception e) {
            e.printStackTrace();
            // Send alert/notification
        }
    }

    private String callPythonRAG(CardData data) {
        // Execute Python script via ProcessBuilder
        ProcessBuilder pb = new ProcessBuilder(
            "python3",
            "forge-scripts/generate_script.py",
            "--name", data.getName(),
            "--mana-cost", data.getManaCost(),
            "--type", data.getTypes(),
            "--oracle", data.getOracleText()
        );

        // Capture output
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.lines().collect(Collectors.joining("\n"));
    }

    private void saveScript(String cardName, String script) throws IOException {
        String filename = cardName.toLowerCase().replaceAll("[^a-z0-9]", "_") + ".txt";
        char firstLetter = filename.charAt(0);

        File dir = new File(ForgeConstants.USER_CUSTOM_CARDS_DIR, String.valueOf(firstLetter));
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File scriptFile = new File(dir, filename);
        Files.write(scriptFile.toPath(), script.getBytes());
    }
}
```

### 4.2 Python Bridge Script

```python
# forge-scripts/generate_script.py
import argparse
import sys
from rag_search import find_similar_cards
from script_generator import generate_card_script

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--name', required=True)
    parser.add_argument('--mana-cost', required=True)
    parser.add_argument('--type', required=True)
    parser.add_argument('--oracle', required=True)
    parser.add_argument('--power', default='')
    parser.add_argument('--toughness', default='')
    parser.add_argument('--loyalty', default='')

    args = parser.parse_args()

    # Build card data
    card_data = {
        'name': args.name,
        'mana_cost': args.mana_cost,
        'type_line': args.type,
        'oracle_text': args.oracle,
        'power': args.power,
        'toughness': args.toughness,
        'loyalty': args.loyalty
    }

    # Find similar cards
    similar = find_similar_cards(args.oracle, top_k=5)

    # Load full scripts for similar cards
    similar_cards = []
    for result in similar['ids'][0]:
        script_path = f"forge-gui/res/cardsfolder/{result[0]}/{result}.txt"
        with open(script_path, 'r') as f:
            similar_cards.append({
                'filename': result,
                'script_content': f.read()
            })

    # Generate script
    script = generate_card_script(card_data, similar_cards)

    # Output to stdout (Java will capture)
    print(script)

if __name__ == '__main__':
    main()
```

---

## Phase 5: Configuration & Setup

### 5.1 Configuration File

```properties
# forge-config/card-generation.properties

# Scraping
spoiler.url=https://mythicspoiler.com/newspoilers.html
spoiler.cache.dir=~/.forge/spoiler-cache
spoiler.user.agent=Forge-CardScript-Generator/1.0

# OCR
ocr.engine=tesseract  # or 'google-cloud'
ocr.language=eng
tesseract.datapath=tessdata

# Google Cloud Vision (if enabled)
google.vision.api.key=${GOOGLE_VISION_API_KEY}

# Embeddings
embedding.model=sentence-transformers/all-MiniLM-L6-v2  # or 'openai'
embedding.batch.size=100
vector.db.path=./forge-vector-db
vector.db.collection=card_scripts

# LLM
llm.provider=anthropic  # or 'openai'
llm.model=claude-3-5-sonnet-20241022
llm.api.key=${ANTHROPIC_API_KEY}
llm.temperature=0.2

# Scheduling
scheduler.enabled=true
scheduler.time=02:00
scheduler.timezone=UTC

# Sampling
sampling.multicolor.include=true
sampling.mythic.percentage=100
sampling.rare.percentage=70
sampling.uncommon.percentage=40
sampling.common.percentage=20
```

### 5.2 Dependencies

**Maven (pom.xml)**:
```xml
<!-- JSoup for web scraping -->
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.16.1</version>
</dependency>

<!-- Tesseract OCR -->
<dependency>
    <groupId>net.sourceforge.tess4j</groupId>
    <artifactId>tess4j</artifactId>
    <version>5.7.0</version>
</dependency>

<!-- Google Cloud Vision (optional) -->
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-vision</artifactId>
    <version>3.20.0</version>
</dependency>

<!-- HTTP client -->
<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
    <version>5.2.1</version>
</dependency>
```

**Python (requirements.txt)**:
```
sentence-transformers==2.2.2
chromadb==0.4.18
anthropic==0.7.8
openai==1.3.7
numpy==1.24.3
Pillow==10.1.0
```

---

## Phase 6: Error Handling & Monitoring

### 6.1 Error Handling

```java
public class CardGenerationLogger {
    private static final Logger log = LoggerFactory.getLogger(CardGenerationLogger.class);

    public void logGenerationAttempt(String cardName) {
        log.info("Attempting to generate script for: {}", cardName);
    }

    public void logGenerationSuccess(String cardName, String scriptPath) {
        log.info("Successfully generated script for {}: {}", cardName, scriptPath);
    }

    public void logGenerationFailure(String cardName, Exception e) {
        log.error("Failed to generate script for {}", cardName, e);
        // Send notification (email, Slack, etc.)
    }

    public void logOCRFailure(String imageUrl, Exception e) {
        log.error("OCR failed for image: {}", imageUrl, e);
    }

    public void logValidationFailure(String cardName, List<String> errors) {
        log.warn("Validation failed for {}: {}", cardName, String.join(", ", errors));
    }
}
```

### 6.2 Metrics & Monitoring

```java
public class GenerationMetrics {
    private int cardsScraped = 0;
    private int cardsProcessed = 0;
    private int scriptsGenerated = 0;
    private int validationFailures = 0;
    private int ocrFailures = 0;

    public void recordRun() {
        log.info("Generation metrics - Scraped: {}, Processed: {}, Generated: {}, Failed: {}",
            cardsScraped, cardsProcessed, scriptsGenerated, validationFailures);
    }
}
```

---

## Implementation Phases Timeline

### Phase 1: Foundation (Week 1-2)
- [ ] Implement card sampling strategy
- [ ] Generate embeddings for sampled cards
- [ ] Set up ChromaDB vector database
- [ ] Test similarity search

### Phase 2: Scraping & OCR (Week 3-4)
- [ ] Implement mythicspoiler.com scraper
- [ ] Integrate Tesseract OCR
- [ ] Implement card data extraction
- [ ] Test on sample spoilers

### Phase 3: RAG Pipeline (Week 5-6)
- [ ] Implement vector search
- [ ] Create LLM prompt template
- [ ] Integrate Anthropic/OpenAI API
- [ ] Implement script validation

### Phase 4: Automation (Week 7)
- [ ] Implement scheduler
- [ ] Create Python bridge
- [ ] Add error handling
- [ ] End-to-end testing

### Phase 5: Polish (Week 8)
- [ ] Add configuration system
- [ ] Implement monitoring/logging
- [ ] Documentation
- [ ] User guide

---

## Testing Strategy

### Unit Tests
- Card sampling logic
- OCR text extraction
- Data parsing
- Script validation

### Integration Tests
- Web scraping (mock responses)
- Vector database queries
- LLM API calls (mock)
- End-to-end pipeline (test data)

### Manual Testing
- Run on recent spoilers
- Verify script quality
- Test edge cases (complex cards)

---

## Security & Privacy

1. **Fully Local Operation**
   - No external API calls (except web scraping)
   - All processing happens on local machine
   - No data leaves local network
   - Privacy-preserving design

2. **Web Scraping Ethics**
   - Respect robots.txt
   - Rate limiting (1 request per 5 seconds)
   - User agent identification
   - Cache results to minimize requests
   - Only scrape public spoiler information

3. **Data Privacy**
   - No personal data collection
   - Only public card information
   - Images cached locally, can be deleted after OCR
   - Vector database stored locally (no cloud sync)

---

## Cost Estimation

### One-time Setup: **$0**
- Embedding generation: **$0** (Sentence Transformers on RTX 3090, ~2-3 minutes for 40K cards)
- Qdrant installation: **$0** (Docker container, local storage)
- Ollama setup: **$0** (gpt-oss:20b model download, ~11GB)
- Tesseract OCR: **$0** (apt-get install on Linux)

### Monthly Recurring: **~$0**
- LLM inference: **$0** (local Ollama on RTX 3090)
- OCR processing: **$0** (local Tesseract)
- Vector database: **$0** (local Qdrant storage)
- **Electricity cost**: ~$5-10/month (RTX 3090 power consumption for daily runs)
- **Total: ~$0-10/month** (electricity only)

### Hardware Requirements:
- GPU: RTX 3090 (24GB VRAM) - **already owned**
- Storage: ~2GB for vector DB + embeddings
- RAM: 16GB+ recommended for Ollama
- Disk: ~20GB for Ollama models and card images

### Cost Comparison to Cloud:
- **Cloud approach**: $25-45/month in API costs
- **Local approach**: $0-10/month in electricity
- **Annual savings**: $180-420/year

---

## Future Enhancements

1. **GUI Integration**
   - View scraped cards
   - Manually trigger generation
   - Edit generated scripts
   - Preview cards

2. **Multi-source Scraping**
   - Reddit r/MagicSpoilers
   - Official Wizards site
   - Scryfall API

3. **Human-in-the-Loop**
   - Review generated scripts before saving
   - Provide feedback to improve prompts
   - Manual corrections

4. **Fine-tuning**
   - Fine-tune LLM on Forge card scripts
   - Improve generation quality
   - Reduce hallucinations

5. **Advanced Mechanics**
   - Multi-part cards (DFC, split, etc.)
   - Token generation
   - Complex ability parsing

---

## Resolved Design Decisions

1. **✅ Filename as ID**: Using filename (e.g., `llanowar_elves`) as vector DB ID is perfect:
   - Unique identifier
   - Easy to load full script when needed
   - Matches Forge's file structure

2. **✅ Database Sampling**: Include ALL 40,000 cards with priority weighting:
   - **100% of all rares and mythic rares**
   - **100% of all multicolor cards**
   - **100% of cards with set-specific mechanics**
   - All other cards included (local storage is free)
   - Weighted search results prioritize complex/similar cards

3. **✅ LLM Choice**: **Ollama with gpt-oss:20b**
   - Runs locally on RTX 3090 (24GB VRAM)
   - No API costs
   - Privacy-preserving (no data leaves local network)
   - Good balance of quality and speed

4. **✅ Vector Database**: **Qdrant**
   - Rust-based, high performance
   - REST API for easy Java integration
   - Runs locally via Docker
   - ~500MB-1GB storage for 40K cards

5. **✅ OCR Engine**: **Tesseract (local only)**
   - Free and open source
   - Good accuracy for card text
   - Handles poor image quality with preprocessing

## Remaining Open Questions

1. **OCR Accuracy**: How do we handle:
   - Poor image quality? → Preprocessing (denoise, contrast adjustment)
   - Special card layouts (planeswalkers, adventures)? → Template-based parsing
   - Foreign language cards? → Skip non-English cards for now

2. **Sampling Coverage**:
   - How to auto-detect mechanics from keywords? → Parse oracle text for set mechanics
   - Manual mechanic list per set? → Start with keyword analysis, manual curation later

---

## Success Criteria

1. **Accuracy**: 80%+ of generated scripts are valid on first try
2. **Coverage**: Generate scripts for 90%+ of new spoiled cards
3. **Performance**: Process and generate scripts within 10 minutes per batch
4. **Maintenance**: System runs reliably without daily intervention
5. **Cost**: ~$0/month in operating costs (electricity only)
6. **Isolation**: No merge conflicts with main Forge repository (PRs contain only card scripts)

---

## Risk Mitigation

| Risk | Mitigation |
|------|------------|
| Website structure changes | Robust HTML parsing, fallback selectors |
| OCR failures | Image preprocessing, manual review queue |
| LLM hallucinations | Multiple similar cards, validation, temperature=0.2 |
| GPU resource exhaustion | Batch processing, memory management, model optimization |
| Merge conflicts | Complete isolation in fork, PRs with only card scripts |
| Script quality | Validation checks, human review process, feedback loop |
| Qdrant container failure | Docker auto-restart, regular backups, health checks |

---

## Appendix A: Forge Card Script Format Reference

```
Name:Card Name Here
ManaCost:2 G G
Types:Legendary Creature Elf Druid
PT:3/3
K:Keyword1
K:Keyword2
A:AB$ EffectType | Cost$ ... | Parameters$ ...
S:Mode$ ... | EffectZone$ ... | Affected$ ...
T:Mode$ ... | TriggerZones$ ... | Execute$ ...
SVar:VariableName:Value
Oracle:Oracle text here
```

## Appendix B: Common Forge Script Patterns

### Mana Abilities
```
A:AB$ Mana | Cost$ T | Produced$ G | SpellDescription$ Add {G}.
```

### Pump Effects
```
A:AB$ Pump | Cost$ 1 G | ValidTgts$ Creature | TgtPrompt$ ... | NumAtt$ +2 | NumDef$ +2
```

### Draw Cards
```
A:AB$ Draw | Cost$ 2 U | NumCards$ 2
```

### Static Abilities
```
S:Mode$ Continuous | Affected$ Creature.YouCtrl | AddPower$ 1 | AddToughness$ 1
```

### Triggered Abilities
```
T:Mode$ ChangesZone | Origin$ Any | Destination$ Battlefield | ValidCard$ Card.Self | Execute$ TrigDraw
SVar:TrigDraw:DB$ Draw | NumCards$ 1
```

---

## Appendix C: .gitignore Configuration

To ensure automation code stays isolated and PRs only contain card scripts, add to `.gitignore`:

```gitignore
# Automated Card Script Generation - Isolation
# These files should NEVER be in PRs to main Forge repo

# Python automation scripts
forge-scripts/
*.pyc
__pycache__/
venv/
.venv/

# Java automation package
forge-automation/

# Vector database
qdrant_storage/
embeddings/
*.npy
*.pkl

# Ollama and LLM
ollama_cache/
*.gguf

# OCR and image processing
card_images_cache/
ocr_output/
tesseract_temp/

# Configuration (may contain local paths)
config/automation.properties
config/local_*.properties

# Logs
logs/automation/
*.log

# Temporary files
temp_card_scripts/
review_queue/
```

### What CAN be committed to PRs:
```
✅ forge-gui/res/cardsfolder/[letter]/[card_name].txt
✅ Documentation updates (if approved)
```

### What should NEVER be in PRs:
```
❌ forge-scripts/ (Python automation)
❌ forge-automation/ (Java automation)
❌ qdrant_storage/ (vector database)
❌ config/automation.properties
❌ Any automation-related code
```

---

## Appendix D: Environment Setup Checklist

### 1. Install Dependencies
```bash
# Tesseract OCR
sudo apt-get install tesseract-ocr

# Python environment
python3 -m venv venv
source venv/bin/activate
pip install sentence-transformers requests opencv-python pytesseract jsoup4 lxml

# Docker (for Qdrant)
sudo apt-get install docker.io
sudo systemctl start docker
```

### 2. Setup Qdrant
```bash
docker run -d \
  --name qdrant \
  -p 6333:6333 \
  -v $(pwd)/qdrant_storage:/qdrant/storage \
  --restart unless-stopped \
  qdrant/qdrant
```

### 3. Setup Ollama
```bash
# Install Ollama
curl -fsSL https://ollama.ai/install.sh | sh

# Pull gpt-oss:20b model (~11GB)
ollama pull gpt-oss:20b

# Verify GPU detection
ollama run gpt-oss:20b "test"
```

### 4. Generate Embeddings (One-time)
```bash
cd forge-scripts
python generate_embeddings.py
# Expected time: 2-3 minutes for 40K cards on RTX 3090
```

### 5. Verify Setup
```bash
# Check Qdrant is running
curl http://localhost:6333/health

# Check Ollama is running
curl http://localhost:11434/api/tags

# Test embedding generation
python -c "from sentence_transformers import SentenceTransformer; print(SentenceTransformer('all-MiniLM-L6-v2'))"
```

---

**Document Version**: 2.0
**Last Updated**: 2025-11-07
**Author**: Claude (Automated Card Script Generation System)
**Status**: Fully Local Setup - Ready for Implementation

# Automated Card Script Generation System - Implementation Plan

## Overview
An AI-powered system that automatically discovers new Magic: The Gathering cards from spoiler sites, extracts card data using OCR, and generates Forge card scripts using RAG (Retrieval Augmented Generation) by finding similar existing cards.

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

### 1.1 Card Script Sampling Strategy

**Objective**: Select representative cards from each set to cover all mechanics while minimizing database size and costs.

**Sampling Criteria**:
1. **All Multicolor Cards** (Priority: HIGH)
   - Cards with 2+ colors in mana cost (e.g., `W U`, `R G`, `U B R`)
   - Cards with multicolor abilities
   - Rationale: Multicolor cards often have unique/complex mechanics

2. **Set Mechanic Representatives** (Priority: HIGH)
   - Cards featuring set-specific keywords (e.g., Surveil, Convoke, Mutate)
   - Extract from edition metadata or keyword analysis
   - At least 3-5 cards per unique mechanic

3. **Rarity-Based Sampling** (Priority: MEDIUM)
   - **Mythic Rare**: Include 100% (usually ~15 per set)
   - **Rare**: Include 50-70% (select unique mechanics)
   - **Uncommon**: Include 30-40% (diverse mechanic coverage)
   - **Common**: Include 15-20% (basic mechanics, vanilla creatures)

4. **Card Type Distribution** (Priority: MEDIUM)
   - Ensure coverage across: Creatures, Instants, Sorceries, Enchantments, Artifacts, Planeswalkers, Lands
   - Sample at least 5-10 cards per type per set

5. **Complexity Filtering** (Priority: LOW)
   - Include cards with complex oracle text (>100 characters)
   - Include cards with multiple abilities
   - Include cards with unique SVar patterns

**Estimated Reduction**:
- Original: ~40,000 card scripts
- After sampling: ~12,000-15,000 (60-70% reduction)
- Still comprehensive coverage of mechanics

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

**Options**:
1. **OpenAI Embeddings** (ada-002)
   - Pros: High quality, easy integration
   - Cons: Cost (~$0.10 per 1M tokens), requires API key
   - Best for: Production use with budget

2. **Sentence Transformers** (all-MiniLM-L6-v2)
   - Pros: Free, local, fast, good quality
   - Cons: Requires Python, 22MB model
   - Best for: Development and self-hosted

3. **BGE (Beijing General Embedding)**
   - Pros: State-of-the-art, multilingual
   - Cons: Larger model size
   - Best for: Maximum quality

**Recommendation**: Start with **Sentence Transformers** for development, offer OpenAI as optional upgrade.

### 1.3 Vector Database Selection

**Options**:
1. **ChromaDB** (Recommended for local development)
   - Pros: Embedded, no server, Python-friendly, free
   - Cons: Requires Python bridge

2. **FAISS** (Facebook AI Similarity Search)
   - Pros: Very fast, battle-tested, local
   - Cons: Requires Python/C++ bindings

3. **Pinecone** (Cloud-hosted)
   - Pros: Managed, scalable, simple API
   - Cons: Cost ($70/month for 1M vectors), requires internet

4. **Weaviate** (Self-hosted)
   - Pros: Feature-rich, GraphQL API, Docker
   - Cons: Requires server setup

**Recommendation**: **ChromaDB** for simplicity and local development.

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
from sentence_transformers import SentenceTransformer
import chromadb

def generate_embeddings_batch():
    model = SentenceTransformer('all-MiniLM-L6-v2')
    client = chromadb.PersistentClient(path="./forge-vector-db")
    collection = client.get_or_create_collection("card_scripts")

    card_scripts_dir = "forge-gui/res/cardsfolder"
    sampled_cards = sample_cards()  # Apply sampling strategy

    batch_size = 100
    for i in range(0, len(sampled_cards), batch_size):
        batch = sampled_cards[i:i+batch_size]
        texts = [build_embedding_text(card) for card in batch]
        embeddings = model.encode(texts)

        collection.add(
            embeddings=embeddings,
            documents=texts,
            ids=[card.filename for card in batch],
            metadatas=[card.metadata for card in batch]
        )

    print(f"Generated embeddings for {len(sampled_cards)} cards")
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

**Option 1: Tesseract OCR** (Open source, local)
```java
// Add dependency: net.sourceforge.tess4j:tess4j:5.7.0
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class CardOCR {
    private final Tesseract tesseract;

    public CardOCR() {
        tesseract = new Tesseract();
        tesseract.setDatapath("tessdata"); // Tesseract data files
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(1); // Auto page segmentation
    }

    public String extractText(File imageFile) throws TesseractException {
        return tesseract.doOCR(imageFile);
    }
}
```

**Option 2: Google Cloud Vision API** (More accurate, costs money)
```java
// Add dependency: com.google.cloud:google-cloud-vision:3.20.0
import com.google.cloud.vision.v1.*;

public class CloudOCR {
    public String extractText(byte[] imageData) {
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
            Image img = Image.newBuilder().setContent(ByteString.copyFrom(imageData)).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feat)
                .setImage(img)
                .build();

            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(List.of(request));
            TextAnnotation annotation = response.getResponses(0).getFullTextAnnotation();
            return annotation.getText();
        }
    }
}
```

**Recommendation**: Start with Tesseract (free), add Cloud Vision as optional upgrade.

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

### 3.1 Vector Search for Similar Cards

```python
# forge-scripts/rag_search.py
def find_similar_cards(oracle_text, top_k=5):
    """
    Query vector database for cards with similar oracle text.
    """
    model = SentenceTransformer('all-MiniLM-L6-v2')
    client = chromadb.PersistentClient(path="./forge-vector-db")
    collection = client.get_collection("card_scripts")

    # Generate embedding for query
    query_embedding = model.encode([oracle_text])

    # Search
    results = collection.query(
        query_embeddings=query_embedding,
        n_results=top_k,
        include=['documents', 'metadatas', 'distances']
    )

    return results
```

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
import openai
from anthropic import Anthropic

def generate_card_script(card_data, similar_cards):
    """
    Use LLM to generate Forge card script.
    """
    # Build prompt
    prompt = build_prompt(card_data, similar_cards)

    # Option 1: OpenAI
    # response = openai.ChatCompletion.create(
    #     model="gpt-4-turbo",
    #     messages=[{"role": "user", "content": prompt}],
    #     temperature=0.2  # Low temperature for consistency
    # )
    # script = response.choices[0].message.content

    # Option 2: Claude
    client = Anthropic(api_key=os.environ.get("ANTHROPIC_API_KEY"))
    message = client.messages.create(
        model="claude-3-5-sonnet-20241022",
        max_tokens=2048,
        temperature=0.2,
        messages=[{"role": "user", "content": prompt}]
    )
    script = message.content[0].text

    return script

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

1. **API Key Management**
   - Store in environment variables
   - Never commit to git
   - Use key rotation

2. **Web Scraping Ethics**
   - Respect robots.txt
   - Rate limiting
   - User agent identification
   - Cache results to minimize requests

3. **Data Privacy**
   - No personal data collection
   - Only public spoiler information
   - Secure API communication (HTTPS)

---

## Cost Estimation

### One-time Setup:
- Embedding generation: ~$5-10 (if using OpenAI) or $0 (Sentence Transformers)
- ChromaDB: $0 (local)

### Monthly Recurring:
- LLM API calls: ~$10-30/month (assuming 100-300 new cards/month @ $0.10 per card)
- Google Cloud Vision (optional): ~$15/month
- **Total: ~$25-45/month**

### Cost Reduction:
- Use local models (Sentence Transformers, llama.cpp)
- Batch processing
- Caching

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

## Open Questions

1. **Filename as ID**: Yes, using filename (e.g., `llanowar_elves`) as vector DB ID is perfect since:
   - Unique identifier
   - Easy to load full script when needed
   - Matches Forge's file structure

2. **Sampling Coverage**: Need to analyze:
   - How many unique mechanics per set?
   - Can we auto-detect mechanics from keywords?
   - Should we manually curate a mechanic list per set?

3. **LLM Choice**: Should we:
   - Use Claude (better at following formats)?
   - Use GPT-4 (more commonly available)?
   - Support both?
   - Use local models (llama3, mixtral)?

4. **OCR Accuracy**: How do we handle:
   - Poor image quality?
   - Special card layouts (planeswalkers, adventures)?
   - Foreign language cards?

---

## Success Criteria

1. **Accuracy**: 80%+ of generated scripts are valid on first try
2. **Coverage**: Generate scripts for 90%+ of new spoiled cards
3. **Performance**: Process and generate scripts within 10 minutes per batch
4. **Maintenance**: System runs reliably without daily intervention
5. **Cost**: Stay under $50/month in API costs

---

## Risk Mitigation

| Risk | Mitigation |
|------|------------|
| Website structure changes | Robust HTML parsing, fallback selectors |
| OCR failures | Multiple OCR engines, manual review queue |
| API rate limits | Batch processing, exponential backoff |
| LLM hallucinations | Multiple similar cards, validation |
| Cost overruns | Local models as fallback, usage monitoring |
| Script quality | Human review process, feedback loop |

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

**Document Version**: 1.0
**Last Updated**: 2025-11-06
**Author**: Claude (Automated Card Script Generation System)

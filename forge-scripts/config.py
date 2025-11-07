"""Configuration for automation system."""
import os

# Paths
CARDSFOLDER_PATH = '../forge-gui/res/cardsfolder'
CACHE_DIR = 'data/cache'
LOGS_DIR = 'data/logs'

# Qdrant
QDRANT_URL = os.getenv('QDRANT_URL', 'http://localhost:6333')
QDRANT_COLLECTION = 'card_scripts'

# Ollama
OLLAMA_URL = os.getenv('OLLAMA_URL', 'http://localhost:11434')
OLLAMA_MODEL = os.getenv('OLLAMA_MODEL', 'gpt-oss:20b')

# Embedding Model
EMBEDDING_MODEL = 'all-MiniLM-L6-v2'

# Web Scraping
MYTHICSPOILER_URL = 'https://www.mythicspoiler.com/newspoilers.html'
RATE_LIMIT_SECONDS = 5

# RAG Settings
SIMILAR_CARDS_LIMIT = 5
SEARCH_SCORE_THRESHOLD = 0.5

# LLM Settings
LLM_TEMPERATURE = 0.2
LLM_MAX_TOKENS = 2048
LLM_TIMEOUT = 120

# Batch Processing
BATCH_SIZE = 100

# Scheduling
SCHEDULE_HOUR = 2  # 2 AM
SCHEDULE_MINUTE = 0

# Create directories
os.makedirs(CACHE_DIR, exist_ok=True)
os.makedirs(LOGS_DIR, exist_ok=True)

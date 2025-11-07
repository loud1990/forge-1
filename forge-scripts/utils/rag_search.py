"""RAG search for finding similar cards."""
from sentence_transformers import SentenceTransformer
from typing import List, Dict, Any
from .qdrant_manager import QdrantManager

class RAGSearch:
    def __init__(self, model_name: str = 'all-MiniLM-L6-v2'):
        self.model = SentenceTransformer(model_name)
        self.qdrant = QdrantManager()

    def find_similar_cards(self, card_data: Dict[str, str], limit: int = 5) -> List[Dict[str, Any]]:
        """Find similar cards using RAG."""
        # Create query text
        query_parts = []

        if card_data.get('types'):
            query_parts.append(f"Types: {card_data['types']}")

        if card_data.get('oracle_text'):
            query_parts.append(f"Text: {card_data['oracle_text']}")

        if card_data.get('pt'):
            query_parts.append(f"P/T: {card_data['pt']}")

        query_text = ' | '.join(query_parts)

        # Generate embedding
        query_vector = self.model.encode(query_text, convert_to_tensor=False).tolist()

        # Search
        results = self.qdrant.search(query_vector, limit=limit * 2, score_threshold=0.5)

        # Apply priority weighting
        weighted_results = []
        for result in results:
            score = result['score']
            priority = result['payload'].get('priority_weight', 1.0)
            weighted_score = score * priority

            weighted_results.append({
                'score': score,
                'weighted_score': weighted_score,
                'card_id': result['payload']['card_id'],
                'name': result['payload']['name'],
                'full_script': result['payload']['full_script'],
                'oracle': result['payload']['oracle']
            })

        # Sort by weighted score
        weighted_results.sort(key=lambda x: x['weighted_score'], reverse=True)

        return weighted_results[:limit]

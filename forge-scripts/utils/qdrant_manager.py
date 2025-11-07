"""Qdrant vector database manager."""
import requests
from typing import List, Dict, Any

class QdrantManager:
    def __init__(self, url: str = "http://localhost:6333", collection: str = "card_scripts"):
        self.url = url
        self.collection = collection

    def create_collection(self, vector_size: int = 384):
        """Create or recreate collection."""
        # Delete if exists
        requests.delete(f"{self.url}/collections/{self.collection}")

        # Create collection
        response = requests.put(
            f"{self.url}/collections/{self.collection}",
            json={
                "vectors": {
                    "size": vector_size,
                    "distance": "Cosine"
                }
            }
        )
        return response.json()

    def upload_batch(self, points: List[Dict[str, Any]]):
        """Upload batch of points to Qdrant."""
        response = requests.put(
            f"{self.url}/collections/{self.collection}/points",
            json={"points": points}
        )
        return response.json()

    def search(self, query_vector: List[float], limit: int = 5, score_threshold: float = 0.7) -> List[Dict]:
        """Search for similar cards."""
        response = requests.post(
            f"{self.url}/collections/{self.collection}/points/search",
            json={
                "vector": query_vector,
                "limit": limit,
                "score_threshold": score_threshold,
                "with_payload": True
            }
        )
        return response.json().get('result', [])

    def health_check(self) -> bool:
        """Check if Qdrant is running."""
        try:
            response = requests.get(f"{self.url}/health")
            return response.status_code == 200
        except:
            return False

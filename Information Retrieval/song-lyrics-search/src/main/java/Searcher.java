import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.nio.file.Path;


/*
 * This class executes a search on an index for a given user generated query.
 * It has the ability to search multiple fields in a document,
 * and also keeps track of the search history and provides alternate queries
 */

class SearchResult implements Comparable<SearchResult> {
    private String songId;
    private float similarity;

    public SearchResult(String songId, float similarity) {
        this.songId = songId;
        this.similarity = similarity;
    }

    public String getSongId() {
        return songId;
    }

    public float getSimilarity() {
        return similarity;
    }

    @Override
    public int compareTo(SearchResult other) {
        return Float.compare(other.similarity, this.similarity);
    }
}

public class Searcher {
	private static final String INDEX_DIR = "C:\\Users\\akisg\\OneDrive\\Documents\\Work\\school\\anaktisi Project\\song-lyrics-search\\index";
    private VectorLoader vectorLoader;
    private static final Logger logger = LogManager.getLogger(VectorLoader.class);

    
    @SuppressWarnings("unused")
	private Map<String, double[]> word2vecModel;

    public Searcher(VectorLoader vectorLoader, Map<String, double[]> word2vecModel) {
        this.vectorLoader = vectorLoader;
        this.word2vecModel = word2vecModel;
    }
    
    // normalize vectors
    public static double[] normalizeVector(double[] vector) {
        double magnitude = Math.sqrt(Arrays.stream(vector).map(val -> val * val).sum());
        return Arrays.stream(vector).map(val -> val / magnitude).toArray();
    }

    public List<SearchResult> search(String queryString) throws IOException {
        double[] queryVector = vectorLoader.convertQueryStringToVector(queryString);
        if (queryVector == null) {
            logger.error("Query vector could not be converted. Query: " + queryString);
            return Collections.emptyList();
        }
        float maxSimilarity = -1;

        List<SearchResult> results = new ArrayList<>();

        Path path = Paths.get(INDEX_DIR);
        
        // Print the query string
        System.out.println("Query String: " + queryString);
        
        try (Directory indexDir = FSDirectory.open(path);
             IndexReader indexReader = DirectoryReader.open(indexDir)) {

            for (int i = 0; i < indexReader.maxDoc(); i++) {
                String songId = indexReader.document(i).get("songId");
                System.out.println("Song ID: " + songId);  // Print the songId
                double[] songVector = vectorLoader.getVector(songId);
                
                if (songVector != null) {
                    songVector = normalizeVector(songVector);
                    queryVector = normalizeVector(queryVector);
                    float similarity = (float) vectorLoader.computeCosineSimilarity(queryVector, songVector);
                    if(similarity > maxSimilarity) {
                        results.add(new SearchResult(songId, similarity));
                    }
                }

            }
        }
        Collections.sort(results);
        return results;
    }
}
    /*public float search(String queryString) throws IOException {
        double[] queryVector = vectorLoader.convertQueryStringToVector(queryString);

        Path path = Paths.get(INDEX_DIR);
        try (Directory indexDir = FSDirectory.open(path);
             IndexReader indexReader = DirectoryReader.open(indexDir)) {

            IndexSearcher searcher = new IndexSearcher(indexReader);
            float maxSimilarity = -1;
            String bestMatch = "";

            for (int i = 0; i < indexReader.maxDoc(); i++) {
                String songId = indexReader.document(i).get("songId");
                double[] songVector = vectorLoader.getVector(songId);

                if (songVector != null) {
                    float similarity = vectorLoader.computeCosineSimilarity(queryVector, songVector);
                    if (similarity > maxSimilarity) {
                        maxSimilarity = similarity;
                        bestMatch = songId;
                    }
                }
            }

            System.out.println("Best match is song with id " + bestMatch + " with similarity score " + maxSimilarity);
            return maxSimilarity;
        }
    }*/
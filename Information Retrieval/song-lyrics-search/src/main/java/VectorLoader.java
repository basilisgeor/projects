import java.util.Arrays;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import com.google.gson.reflect.TypeToken;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.google.gson.*;
import java.io.Reader;
import java.io.StringReader;

/**
 * VectorLoader is a class that loads song vectors from a file and provides methods to manipulate and analyze these vectors.
 */
public class VectorLoader {
    private static final Logger logger = LogManager.getLogger(VectorLoader.class);

    private Map<String, double[]> songVectors;
    private Map<String, double[]> word2vecModel;
    private Analyzer analyzer;

    /**
     * Constructor for VectorLoader. It initializes the songVectors map by reading from a JSON file.
     * @param filePath The path to the JSON file containing song vectors.
     * @param word2vecModel The Word2Vec model used for semantic analysis.
     * @param analyzer The Analyzer used for tokenizing text.
     */
    public VectorLoader(String filePath, Map<String, double[]> word2vecModel, Analyzer analyzer) {
        this.word2vecModel = word2vecModel;
        this.analyzer = analyzer;

        try {
            // Create a Gson object
            Gson gson = new Gson();

            // Create a Reader object to read the JSON file
            Reader reader = Files.newBufferedReader(Paths.get(filePath));

            // Use Gson to parse the JSON file into a Map<String, double[]>
            this.songVectors = gson.fromJson(reader, new TypeToken<Map<String, double[]>>(){}.getType());

            // Close the reader
            reader.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    /**
     * Prints information about the song vectors loaded into the songVectors map.
     */
    public void printSongVectorsInfo() {
        System.out.println("Number of song vectors: " + songVectors.size());
        System.out.println("First 5 song vectors:");
        int count = 0;
        for (Map.Entry<String, double[]> entry : songVectors.entrySet()) {
            System.out.println("Song ID: " + entry.getKey());
            System.out.println("Vector: " + Arrays.toString(entry.getValue()));
            count++;
            if (count >= 5) {
                break;
            }
        }
    }

    /**
     * Retrieves the vector for a given song ID.
     * @param songId The ID of the song.
     * @return The vector for the song.
     */
    public double[] getVector(String songId) {
        double[] vector = songVectors.get(songId);
        logger.info("Retrieved vector for songId " + songId + ": " + Arrays.toString(vector));
        return vector;
    }

    /**
     * Converts a query string into a vector using the Word2Vec model.
     * @param queryString The query string to convert.
     * @return The vector representation of the query string.
     * @throws IOException If an error occurs during tokenization.
     */
    public double[] convertQueryStringToVector(String queryString) throws IOException {
        logger.info("Converting query string to vector: {}", queryString);
        double[] queryVector = new double[100];
        TokenStream stream = analyzer.tokenStream(null, new StringReader(queryString));
        CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
        stream.reset();

        int count = 0;
        while (stream.incrementToken()) {
            String token = cattr.toString();
            double[]wordVector = word2vecModel.get(token);
            if (wordVector != null) {
                for (int i = 0; i < wordVector.length; i++) {
                    queryVector[i] += wordVector[i];
                }
                count++;
            }
        }

        stream.end();
        stream.close();

        if (count > 0) {
            for (int i = 0; i < queryVector.length; i++) {
                queryVector[i] /= count;
            }
        }

        return queryVector;
    }

    /**
     * Computes the cosine similarity between two vectors. This is a measure of how similar the vectors are.
     * @param vec1 The first vector.
     * @param vec2 The second vector.
     * @return The cosine similarity between the two vectors.
     */
    public float computeCosineSimilarity(double[] vec1, double[] vec2) {
        logger.info("Computing cosine similarity between two vectors");

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            normA += Math.pow(vec1[i], 2);
            normB += Math.pow(vec2[i], 2);
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator != 0 ? (float) (dotProduct / denominator) : 0f;
    }
}

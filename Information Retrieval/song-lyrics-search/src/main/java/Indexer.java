import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
import java.sql.SQLException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/*
 * This class creates an index using Apache Lucene, 
 * connects to a database, retrieves data, and indexes it using the Lucene API.
 */
public class Indexer {
    private static final String INDEX_DIR = "C:\\\\Users\\\\akisg\\\\OneDrive\\\\Documents\\\\Work\\\\school\\\\anaktisi Project\\\\song-lyrics-search\\\\index";
    private static final String SYNONYM_FILE = "C:\\\\Users\\\\akisg\\\\OneDrive\\\\Documents\\\\Work\\\\school\\\\anaktisi Project\\\\song-lyrics-search\\\\resources\\\\synonyms.txt";
    private static final String EMBEDDINGS_FILE = "C:\\Users\\akisg\\OneDrive\\Documents\\Work\\school\\anaktisi Project\\song-lyrics-search\\resources\\song_vectors.json";

    
    
    private static class IndexTask implements Runnable {
        private List<Song> songs;
        private IndexWriter indexWriter;
        private FieldType type;
        private VectorLoader vectorLoader;


        public IndexTask(List<Song> songs, IndexWriter indexWriter, FieldType type, VectorLoader vectorLoader) {
            this.songs = songs;
            this.songs = songs;
            this.indexWriter = indexWriter;
            this.type = type;
            this.vectorLoader = vectorLoader;
        }

        @Override
        public void run() {
            try {
                for (Song song : songs) {
                	double[] embedding = vectorLoader.getVector(String.valueOf(song.getId()));
                    if (embedding == null) {
                        // Skip this song or handle it appropriately.
                        System.out.println("No vector found for song: " + song.getId());
                        continue;
                    }
                    Document doc = new Document();
                    doc.add(new Field("id", String.valueOf(song.getId()), type));
                    doc.add(new Field("title", song.getTitle(), type));
                    doc.add(new Field("artist", song.getArtist(), type));
                    doc.add(new SortedDocValuesField("artist", new BytesRef(song.getArtist())));
                    doc.add(new Field("album", song.getAlbum(), type));
                    doc.add(new Field("year", String.valueOf(song.getReleaseDate()), type));
                    doc.add(new Field("lyrics", song.getLyrics(), type));

                    indexWriter.addDocument(doc);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static Map<String, double[]> loadEmbeddings() {
        Map<String, double[]> embeddingsMap = new HashMap<>();
        try {
            FileReader reader = new FileReader(EMBEDDINGS_FILE);
            try (CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {
				for (CSVRecord csvRecord : csvParser) {
				    String id = csvRecord.get("id");
				    double[] embedding = new double[100]; 
				    for (int i = 0; i < embedding.length; i++) {
				        embedding[i] = Float.parseFloat(csvRecord.get("dim_" + i));
				    }
				    embeddingsMap.put(id, embedding);
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } catch (IOException e) {
            e.printStackTrace();
        }
        return embeddingsMap;
    }
    
    public static void createIndex() {
    	
        System.out.println("Starting index creation..."); 
        try (Directory indexDir = FSDirectory.open(Paths.get(INDEX_DIR))) {
            
            // Use CustomAnalyzer
            Analyzer analyzer = new CustomAnalyzer(SYNONYM_FILE);
            
            // Load the word2vecModel
            Map<String, double[]> word2vecModel = loadEmbeddings();
            
            // Now create the VectorLoader instance
            VectorLoader vectorLoader = new VectorLoader(EMBEDDINGS_FILE, word2vecModel, analyzer);
            

            DatabaseManager dbManager = new DatabaseManager(); 
            dbManager.connect("C:\\Users\\akisg\\OneDrive\\Documents\\Work\\school\\anaktisi Project\\song-lyrics-search\\song_data.db");
            
            // Check if connection is established
            if (dbManager.isConnected()) {
                System.out.println("Database connection established successfully.");
            } else {
                System.out.println("Failed to establish database connection.");
            }

            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            try (IndexWriter indexWriter = new IndexWriter(indexDir, config)) {
                
                // Retrieve song data from the SQLite database
                List<Song> songs = dbManager.getAllSongs();

                // Create FieldType that stores positional data
                FieldType type = new FieldType();
                type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
                type.setStored(true);
                type.setTokenized(true);
                type.setStoreTermVectors(true);
                type.setStoreTermVectorPositions(true);
                
                // Create an ExecutorService
                ExecutorService executor = Executors.newFixedThreadPool(4);  // adjust the number of threads

                // Set the batch size
                int batchSize = 100;

                // Loop over the batches
                for (int i = 0; i < songs.size(); i += batchSize) {
                    // Get the current batch of songs
                    List<Song> batch = songs.subList(i, Math.min(i + batchSize, songs.size()));

                    // Index the batch in a new thread
                    executor.submit(new IndexTask(batch, indexWriter, type, vectorLoader));
                }

                // Shut down the executor service
                executor.shutdown();
                // Wait until all tasks are finished
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Index creation completed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
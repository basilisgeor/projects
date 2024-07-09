import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Map;

public class VectorIndexer {
    public void createIndex(Map<String, double[]> songVectors, String indexDirPath) {
    	
        try {
            FSDirectory dir = FSDirectory.open(Paths.get(indexDirPath));
            StandardAnalyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(dir, config);
            
            // Print the index directory path
            System.out.println("Index Directory Path: " + indexDirPath);
           

            for (Map.Entry<String, double[]> entry : songVectors.entrySet()) {
            	
            	// Print the song and its vector
                System.out.println("Song: " + entry.getKey());
                System.out.println("Vector: " + entry.getValue());
                
                Document doc = new Document();
                doc.add(new StringField("song", entry.getKey(), Field.Store.YES));

                StringBuilder vectorString = new StringBuilder();
                for (double val : entry.getValue()) {
                    vectorString.append(val).append(" ");
                }
                doc.add(new StringField("vector", vectorString.toString(), Field.Store.YES));

                writer.addDocument(doc);
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.search.TotalHits;


public class NormalSearcher {
	private static final String INDEX_DIR = "C:\\Users\\akisg\\OneDrive\\Documents\\Work\\school\\anaktisi Project\\song-lyrics-search\\index";
    private static final String SYNONYM_FILE = "C:\\\\Users\\\\akisg\\\\OneDrive\\\\Documents\\\\Work\\\\school\\\\anaktisi Project\\\\song-lyrics-search\\\\resources\\\\synonyms.txt";
    private static final int RESULTS_PER_PAGE = 10; 
	private static CustomAnalyzer analyzer;
    private IndexSearcher searcher;


	public NormalSearcher(String indexDir, CustomAnalyzer analyzer) {
		NormalSearcher.analyzer = analyzer;
        // Initialize the IndexSearcher
		try {
	        Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
	        IndexReader indexReader = DirectoryReader.open(dir);
	        searcher = new IndexSearcher(indexReader);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
    }
	public void close() {
	    try {
	        searcher.getIndexReader().close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	private static List<String> searchHistory = new ArrayList<>();
    private static Map<String, List<String>> alternateQueries = new HashMap<>();

    
    public TopDocs search(String queryString, String[] fields, int pageNumber) {
        System.out.println("Query String: " + queryString);
        System.out.println("Fields: " + Arrays.toString(fields));
        System.out.println("Page Number: " + pageNumber); // Print the page number

        try (Analyzer analyzer = new CustomAnalyzer(SYNONYM_FILE)) {

            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, NormalSearcher.analyzer);
            Query query = parser.parse(queryString);
            System.out.println("Executing search for query: " + queryString);
            TopDocs results;
            if (pageNumber == 1) {
                // If it's the first page, use the normal search method
                results = searcher.search(query, RESULTS_PER_PAGE);
            } else {
            	int numResultsToSkip = RESULTS_PER_PAGE * (pageNumber - 1) - 1;
            	TopDocs topDocs = searcher.search(query, numResultsToSkip);
            	if (topDocs.scoreDocs.length <= numResultsToSkip) {
                    if (topDocs.scoreDocs.length > 0) {
                        // return new TopDocs with the last ScoreDoc
                        ScoreDoc[] scoreDocs = {topDocs.scoreDocs[topDocs.scoreDocs.length - 1]};
                        return new TopDocs(new TotalHits(topDocs.totalHits.value, topDocs.totalHits.relation), scoreDocs);
                    } else {
                        return null;
                    }
                } else {
                    ScoreDoc lastScoreDocOfPreviousPage = topDocs.scoreDocs[numResultsToSkip];
                    results = searcher.searchAfter(lastScoreDocOfPreviousPage, query, RESULTS_PER_PAGE);
                }
            }
            // Add the query to the history
            searchHistory.add(queryString);

            // If there are alternate queries for this query, suggest them
            if (alternateQueries.containsKey(queryString)) {
                System.out.println("You might also be interested in: ");
                for (String alternateQuery : alternateQueries.get(queryString)) {
                    System.out.println(alternateQuery);
                }
            }
            System.out.println("Number of results found: " + results.totalHits);

            // Print the results
            for (ScoreDoc scoreDoc : results.scoreDocs) {
                Document document = searcher.doc(scoreDoc.doc);
                System.out.println("Document ID: " + scoreDoc.doc + ", Score: " + scoreDoc.score);
                System.out.println("Document content: " + document.toString()); // Use the document object
            }

            return results;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to add alternate queries for a given query
    public static void addAlternateQueries(String query, List<String> alternates) {
        alternateQueries.put(query, alternates);
    }

    
    
	/*public static TopDocs search(String queryString, String[] fields, int maxResults) {
		
		
		
		// Print the query string, fields, and max results
	    System.out.println("Query String: " + queryString);
	    System.out.println("Fields: " + Arrays.toString(fields));
	    System.out.println("Max Results: " + maxResults);
		
		Path path = Paths.get(INDEX_DIR);
		if (!Files.exists(path) || !Files.isReadable(path)) {
			System.err.println("Index directory '" + INDEX_DIR + "' does not exist or is not readable. Please check the path.");
			return null;
		}
		try (Directory indexDir = FSDirectory.open(Paths.get(INDEX_DIR));
		     IndexReader indexReader = DirectoryReader.open(indexDir);
		     Analyzer analyzer = new CustomAnalyzer(SYNONYM_FILE)) {

			IndexSearcher searcher = new IndexSearcher(indexReader);
			MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, NormalSearcher.analyzer);
			Query query = parser.parse(queryString);
			System.out.println("Executing search for query: " + queryString);
			TopDocs results = searcher.search(query, maxResults);

			// Add the query to the history
			searchHistory.add(queryString);

			// If there are alternate queries for this query, suggest them
			if (alternateQueries.containsKey(queryString)) {
				System.out.println("You might also be interested in: ");
				for (String alternateQuery : alternateQueries.get(queryString)) {
					System.out.println(alternateQuery);
				}
			}
			System.out.println("Number of results found: " + results.totalHits);
			
			// Print the results
		    System.out.println("Results: " + results);

			return results;

		} catch (IOException | ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	// Method to add alternate queries for a given query
	public static void addAlternateQueries(String query, List<String> alternates) {
		alternateQueries.put(query, alternates);
	}*/
}
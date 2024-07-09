import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.JFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.store.FSDirectory;
import java.util.List;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.grouping.GroupingSearch;




/*
 * This class helps us create a song lyrics search application with a GUI using Apache Lucene 
 * It creates an index, performs searches with different query types, 
 * and displays the search results
 */

public class SongSearchApp {
    private static JTextArea resultArea;
    private static int currentPage = 1; 
	private static final int PAGE_SIZE = 10;
    private static final String INDEX_DIR = "C:\\Users\\akisg\\OneDrive\\Documents\\Work\\school\\anaktisi Project\\song-lyrics-search\\index";
    private static final String SYNONYM_FILE = "C:\\\\Users\\\\akisg\\\\OneDrive\\\\Documents\\\\Work\\\\school\\\\anaktisi Project\\\\song-lyrics-search\\\\resources\\\\synonyms.txt";
    private static final String EMBEDDINGS_FILE = "C:\\Users\\akisg\\OneDrive\\Documents\\Work\\school\\anaktisi Project\\song-lyrics-search\\resources\\song_vectors.json";

    private static Searcher semanticSearcher;
    private static NormalSearcher normalSearcher;
    private static QueryHistory queryHistory = new QueryHistory();
    private static Map<String, List<String>> alternateQueriesMap = new HashMap<>();
    
    public static Map<String, List<String>> synonymsMap = new HashMap<>();
    
    public static void clearDirectory(String dirPath) {
        try {
            Files.walk(Paths.get(dirPath))
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(File::delete);
            System.out.println("Index directory cleared.");
        } catch (IOException e) {
            System.err.println("Failed to clear index directory: " + e.getMessage());
        }
    }

    
   	public static void main(String[] args) throws IOException {
        clearDirectory(INDEX_DIR);

	    Indexer.createIndex();
	    
	    List<String> lines;
	    try {
	        lines = Files.readAllLines(Paths.get(SYNONYM_FILE));
	    } catch (IOException e) {
	        e.printStackTrace();
	        return; // or handle the exception in an appropriate way
	    }
	    
	    for (String line : lines) {
	        List<String> synonyms = Arrays.asList(line.split(","));
	        for (String word : synonyms) {
	            // trim is used to remove leading and trailing spaces
	            synonymsMap.put(word.trim(), new ArrayList<>(synonyms));
	        }
	    }
	    
	    // Initialize the normalSearcher
	    normalSearcher = new NormalSearcher(INDEX_DIR, new CustomAnalyzer(SYNONYM_FILE));
	    
	    
	    
	    // Create a basic GUI
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Song Lyrics Search");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            // Create a panel to hold all other components
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
         
            // Create sub-panels
            JPanel searchPanel = new JPanel(new FlowLayout());
            JPanel checkboxPanel = new JPanel(new FlowLayout());
            JPanel resultPanel = new JPanel(new BorderLayout());

            // Add input field
            JTextField searchField = new JTextField(20);
            searchPanel.add(searchField);
            
            // Add result display area
            resultArea = new JTextArea(15, 30);
            resultArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(resultArea);
            resultPanel.add(scrollPane, BorderLayout.CENTER);
            
            // Fields checkboxes
            JCheckBox titleCheckBox = new JCheckBox("Title");
            JCheckBox artistCheckBox = new JCheckBox("Artist");
            JCheckBox albumCheckBox = new JCheckBox("Album");
            JCheckBox lyricsCheckBox = new JCheckBox("Lyrics");
            
            JEditorPane editorPane = new JEditorPane();
            editorPane.setContentType("text/html");
            editorPane.setEditable(false);

            editorPane.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        // Open the hyperlink in new window
                        String lyrics = getLyricsFromSongID(e.getDescription());
                        JOptionPane.showMessageDialog(null, new JScrollPane(new JTextArea(lyrics)));
                    }
                }
            });

            
            // All fields checkbox
            JCheckBox allFieldsCheckBox = new JCheckBox("All Fields");
            allFieldsCheckBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (allFieldsCheckBox.isSelected()) {
                        titleCheckBox.setSelected(false);
                        artistCheckBox.setSelected(false);
                        albumCheckBox.setSelected(false);
                        lyricsCheckBox.setSelected(false);
                    }
                    if (titleCheckBox.isSelected() | artistCheckBox.isSelected() | albumCheckBox.isSelected() | lyricsCheckBox.isSelected() ) {
                    	allFieldsCheckBox.setSelected(false);
                	}
                }
            });
            
            
            
            // Add semantic search checkbox
            JCheckBox semanticSearchCheckBox = new JCheckBox("Semantic Search");
            checkboxPanel.add(semanticSearchCheckBox);
            
            

            // Add checkboxes to panel
            checkboxPanel.add(titleCheckBox);
            checkboxPanel.add(artistCheckBox);
            checkboxPanel.add(albumCheckBox);
            checkboxPanel.add(lyricsCheckBox);
            checkboxPanel.add(allFieldsCheckBox);
            
            // Add 'Next Page' button
            JButton nextPageButton = new JButton("Next Page");
            searchPanel.add(nextPageButton);
            nextPageButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Increase the page number and perform the search again
                    currentPage++;
                    performSearch(searchField, titleCheckBox, artistCheckBox, albumCheckBox, lyricsCheckBox, allFieldsCheckBox, semanticSearchCheckBox);
                }
            });
            
            

            

            // Add button
            JButton searchButton = new JButton("Search");
            searchPanel.add(searchButton);
            // Modify the search button's ActionListener
            searchButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Reset the page number and perform the search
                    currentPage = 1;
                    performSearch(searchField, titleCheckBox, artistCheckBox, albumCheckBox, lyricsCheckBox, allFieldsCheckBox, semanticSearchCheckBox);
                }
            });

            panel.add(searchPanel);
            panel.add(checkboxPanel);
            panel.add(resultPanel);

            frame.getContentPane().add(panel, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
   	
   	private static List<String> generateAlternateQueries(String query, QueryHistory queryHistory, Map<String, List<String>> synonymsMap) {
   	    List<String> alternateQueries = new ArrayList<>();
   	    String[] words = query.split("\\s+");
   	    for (String word : words) {
   	        if (synonymsMap.containsKey(word)) {
   	            for (String synonym : synonymsMap.get(word)) {
   	                String alternateQuery = query.replace(word, synonym);
   	                alternateQueries.add(alternateQuery);
   	            }
   	        }
   	    }
   	    return alternateQueries;
   	}
   	
   	private static void performSearch(JTextField searchField, JCheckBox titleCheckBox, JCheckBox artistCheckBox, JCheckBox albumCheckBox, JCheckBox lyricsCheckBox, JCheckBox allFieldsCheckBox, JCheckBox semanticSearchCheckBox) {
        // Clear previous results
        resultArea.setText("");

        // Check which fields are selected
        List<String> fields = new ArrayList<>();
        if (titleCheckBox.isSelected()) fields.add("title");
        if (artistCheckBox.isSelected()) fields.add("artist");
        if (albumCheckBox.isSelected()) fields.add("album");
        if (lyricsCheckBox.isSelected()) fields.add("lyrics");
        if (allFieldsCheckBox.isSelected()) fields = Arrays.asList("title", "artist", "album", "lyrics");

        
        // Perform the search
        String query = searchField.getText();
        queryHistory.addQuery(query);
        
        final List<String> alternateQueries = generateAlternateQueries(query, queryHistory, synonymsMap);
        if (!alternateQueries.isEmpty()) {
            alternateQueriesMap.put(query, alternateQueries);
        }

        if (queryHistory.isRepeatQuery(query) && alternateQueriesMap.containsKey(query)) {
            resultArea.append("Alternate queries:\n");
            for (String alternateQuery : alternateQueriesMap.get(query)) {
                resultArea.append(alternateQuery + "\n");
            }
            resultArea.append("\n");
        }
        
        try {
        	JEditorPane editorPane = new JEditorPane(); // Create a new JEditorPane instance
            List<Document> results = searchSongs(query, fields, currentPage, semanticSearchCheckBox.isSelected(), editorPane);

            // Display the results or a 'no results' message
            if (results.isEmpty()) {
                resultArea.append("No results found for '" + query + "'. Please try again.");
            } else {
                for (Document doc : results) {
                    resultArea.append(doc.get("title") + " by " + doc.get("artist") + "\n");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


	
   	private static List<Document> searchSongs(String query,List<String> fields, int pageNumber, boolean isSemanticSearch, JEditorPane editorPane) throws IOException {
		Path path = Paths.get(INDEX_DIR);
        if (!Files.exists(path) || !Files.isReadable(path)) {
            System.err.println("Index directory '" + INDEX_DIR + "' does not exist or is not readable. Please check the path.");
            return new ArrayList<>();
        }
        String[] searchFields = fields.toArray(new String[0]);

        resultArea.setText("");

	    
        // Combine queries using BooleanQuery
        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
        for (String field : fields) {
            // Create term query
            Query termQuery = new TermQuery(new Term(field, query));

            // Create acronym expanded query
            String expandedQuery = query + " OR \"" + AcronymExpander.expand(query) + "\""; // Expand the acronym
            Query expandedAcronymQuery;
            try {
                expandedAcronymQuery = new QueryParser(field, new CustomAnalyzer(SYNONYM_FILE)).parse(expandedQuery);
            } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                e.printStackTrace();
                return new ArrayList<>(); // return empty list
            }

            // Split the query into words
            String[] words = query.split("\\s+");

            // Create a PhraseQuery
            PhraseQuery.Builder phraseQueryBuilder = new PhraseQuery.Builder();
            for (int i = 0; i < words.length; i++) {
                phraseQueryBuilder.add(new Term(field, words[i]), i);
            }
            Query phraseQuery = phraseQueryBuilder.build();

            queryBuilder.add(termQuery, BooleanClause.Occur.SHOULD);
            queryBuilder.add(expandedAcronymQuery, BooleanClause.Occur.SHOULD);
            queryBuilder.add(phraseQuery, BooleanClause.Occur.SHOULD);
        }
	    
	    // check if we are doing scemantic search
	    if (isSemanticSearch) {
		    Map<String, double[]> word2vecModel = Indexer.loadEmbeddings();
		    VectorLoader vectorLoader = new VectorLoader(EMBEDDINGS_FILE, word2vecModel, new CustomAnalyzer(SYNONYM_FILE));

	        Searcher semanticSearcher = new Searcher(vectorLoader, word2vecModel);
	        List<SearchResult> semanticResults = semanticSearcher.search(query);
	        for (SearchResult result : semanticResults) {
	            Query semanticQuery = new TermQuery(new Term("songId", result.getSongId()));
	            queryBuilder.add(semanticQuery, BooleanClause.Occur.SHOULD);

	            // Add it to the list of alternate queries.
	           // alternateQueries.add(result.getAlternateQuery());
	        }
	    }
	    // Store the alternate queries in the global map.
	    //alternateQueriesMap.put(query, alternateQueries);
	    Query finalQuery = queryBuilder.build();

	    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIR)));
	    IndexSearcher searcher = new IndexSearcher(reader);
	    
	    
	    // Create GroupingSearch to group by artist
        GroupingSearch groupingSearch = new GroupingSearch("artist");
        groupingSearch.setAllGroups(true);
        groupingSearch.setGroupDocsLimit(PAGE_SIZE);
        groupingSearch.setGroupDocsOffset(PAGE_SIZE * (pageNumber - 1));
        

        TopGroups<String> groups = groupingSearch.search(searcher, finalQuery, 0, PAGE_SIZE);
        
        // Print the query, fields, pageNumber, and isSemanticSearch
        System.out.println("Query: " + query);
        System.out.println("Fields: " + fields);
        System.out.println("Page Number: " + pageNumber);
        System.out.println("Is Semantic Search: " + isSemanticSearch);

        // Print the final query
        System.out.println("Final Query: " + finalQuery);
        
        for (GroupDocs<String> groupDocs : groups.groups) {
            for (ScoreDoc scoreDoc : groupDocs.scoreDocs) {
                Document document = searcher.doc(scoreDoc.doc);
                Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(), new QueryScorer(finalQuery));
                // Initialize your CustomAnalyzer within try-with-resources statement
                try (CustomAnalyzer customAnalyzer = new CustomAnalyzer(SYNONYM_FILE)) {
                	// Highlight title
                	String title = document.get("title");
                	TokenStream titleTokenStream = customAnalyzer.tokenStream("title", new StringReader(title));
                	String highlightedTitle = highlighter.getBestFragment(titleTokenStream, title);
                	if (highlightedTitle == null) {
                	    highlightedTitle = title;
                	}

                    // Highlight artist
                    String artist = document.get("artist");
                    TokenStream artistTokenStream = customAnalyzer.tokenStream("artist", new StringReader(artist));
                    String highlightedArtist = highlighter.getBestFragment(artistTokenStream, artist);
                    if (highlightedArtist == null) {
                    	highlightedArtist = artist;
                	}


                    // Highlight album
                    String album = document.get("album");
                    TokenStream albumTokenStream = customAnalyzer.tokenStream("album", new StringReader(album));
                    String highlightedAlbum = highlighter.getBestFragment(albumTokenStream, album);
                    if (highlightedAlbum == null) {
                    	highlightedAlbum = album;
                	}
                    // Highlight lyrics
                    String lyrics = document.get("lyrics");
                    TokenStream lyricsTokenStream = customAnalyzer.tokenStream("lyrics", new StringReader(lyrics));
                    String highlightedLyrics = highlighter.getBestFragment(lyricsTokenStream, lyrics);
                    if (highlightedLyrics == null) {
                    	highlightedLyrics = lyrics;
                	}
                    resultArea.append("Title: " + highlightedTitle + "\n");
                    resultArea.append("Artist: " + highlightedArtist + "\n");
                    resultArea.append("Album: " + highlightedAlbum + "\n");
                    resultArea.append("Highlighted Lyrics: " + highlightedLyrics + "\n\n");     
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                } catch (InvalidTokenOffsetsException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
        


        List<Document> documents = new ArrayList<>();
        if (isSemanticSearch) {
            // Perform scemantic search
            List<SearchResult> results = semanticSearcher.search(query);
            for (SearchResult result : results) {
            	// Get the songId as a string
                String songId = result.getSongId();
                // Use the songId to get the document from the index
                Document document = getDocumentFromIndex(songId, searcher);
                if (document != null) {
                    documents.add(document);
                }
            }
        } else {
        	// Perform normal search
        	String[] fieldsArray = fields.toArray(new String[0]);
        	TopDocs topDocs = normalSearcher.search(query, fieldsArray, PAGE_SIZE);

        	// Insert the null check here
            if (topDocs == null) {
                System.out.println("No documents matched the query.");
                return Collections.emptyList(); // Return an empty list
            }
        	
        	List<SearchResult> results = new ArrayList<>();
        	for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
        	    // Convert docId to string before creating a SearchResult object
        	    String docId = String.valueOf(scoreDoc.doc);
        	    float score = scoreDoc.score;
        	    results.add(new SearchResult(docId, score));
        	}

        	for (SearchResult result : results) {
        		// Get the songId as a string
        	    String songId = result.getSongId();
        	    // Use the songId to get the document from the index
        	    Document document = getDocumentFromIndex(songId, searcher);
        	    if (document != null) {
        	        documents.add(document);
        	    }
        	}
        }
        // Print the documents
        for (Document document : documents) {
            System.out.println("Document: " + document);
        }
        
        reader.close();
        return documents;
	}
   	
   	private static String getLyricsFromSongID(String songId) {
   	    try {
   	        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIR)));
   	        IndexSearcher searcher = new IndexSearcher(reader);
   	        Query query = new TermQuery(new Term("songId", songId));
   	        TopDocs topDocs = searcher.search(query, 1);
   	        if (topDocs.totalHits.value > 0) {
   	            Document document = searcher.doc(topDocs.scoreDocs[0].doc);
   	            return document.get("lyrics");
   	        }
   	    } catch (IOException e) {
   	        e.printStackTrace();
   	    }
   	    return "Lyrics not found!";
   	}
   	
   	private static Document getDocumentFromIndex(String songId, IndexSearcher searcher) {
   		try {
   	        // Create a query that matches documents with the given songId
   	        Query query = new TermQuery(new Term("songId", songId));
   	        // Search for the document
   	        TopDocs topDocs = searcher.search(query, 1);
   	        if (topDocs.totalHits.value > 0) {
   	            // If we found a document, return it
   	            return searcher.doc(topDocs.scoreDocs[0].doc);
   	        }
   	    } catch (IOException e) {
   	        e.printStackTrace();
   	    }
   	    // If we didn't find a document or an error occurred, return null
   	    return null;
   	}
}
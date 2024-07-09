import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.lucene.analysis.TokenStream;

/*
 * This class create a customized analyzer for processing text data,
 * utilizes a Synonym file we have created ,
 * containing 100 basic words commonly used for music purposes and their synonyms.
 * Its purpose is to help us build an index or query parsing in Lucene search.
 */
public class CustomAnalyzer extends Analyzer {
    private final SynonymMap synonymMap;

    /*
     *  constructor, reads a synonym file from a path and uses it to build a SynonymMap
     */
    public CustomAnalyzer(String synonymFilePath) {
    	// Convert the string path to a Path object
        Path synonymFile = Paths.get(synonymFilePath);
        // Check if the file exists and is readable
        if (!Files.exists(synonymFile) || !Files.isReadable(synonymFile)) {
            throw new RuntimeException("Failed to read synonyms file at " + synonymFile);
        }
        try {
            SynonymMap.Builder builder = new SynonymMap.Builder(true);
            BufferedReader reader = Files.newBufferedReader(synonymFile);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split(",");
                for (int i = 1; i < words.length; i++) {
                    builder.add(new CharsRef(words[0]), new CharsRef(words[i]), true);
                }
            }
            this.synonymMap = builder.build();
        } catch (IOException e) {
            System.out.println("Absolute path to file: " + synonymFile.toAbsolutePath());
            throw new RuntimeException("Failed to read synonyms file", e);
        }
    }

    /*
     * creates TokenStreamComponents object
     * is a combination of StandardTokenizer and
     *  TokenFilters(LowercaseFilter, StopFilter, SynonymGraphFilter
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new StandardTokenizer();
        TokenStream filter = new LowerCaseFilter(tokenizer);
        filter = new StopFilter(filter, EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
        filter = new SynonymGraphFilter(filter, synonymMap, true);
        return new TokenStreamComponents(tokenizer, filter);
    }
}

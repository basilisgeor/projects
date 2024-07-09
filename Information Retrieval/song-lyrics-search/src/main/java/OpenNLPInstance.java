import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import java.io.IOException;
import java.io.InputStream;

public class OpenNLPInstance {

    private Tokenizer tokenizer;

    public OpenNLPInstance() {
        try {
        	InputStream modelIn = getClass().getResourceAsStream("/resources/en-token.bin");
            TokenizerModel model = new TokenizerModel(modelIn);
            tokenizer = new TokenizerME(model);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] tokenize(String text) {
        return tokenizer.tokenize(text);
    }

}
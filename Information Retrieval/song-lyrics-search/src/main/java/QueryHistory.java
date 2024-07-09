import java.util.List;
import java.util.Map;
import java.util.ArrayList;


public class QueryHistory {
    private List<String> history;

    public QueryHistory() {
        this.history = new ArrayList<>();
    }

    public void addQuery(String query) {
        this.history.add(query);
    }

    public List<String> getHistory() {
        return this.history;
    }

    public boolean isRepeatQuery(String query) {
        return this.history.contains(query);
    }
    
    public List<String> getSimilarQueries(String query, Map<String, List<String>> synonymsMap) {
        List<String> similarQueries = new ArrayList<>();
        String[] words = query.split("\\s+");
        for (String word : words) {
            if (synonymsMap.containsKey(word)) {
                for (String previousQuery : history) {
                    if (previousQuery.contains(word)) {
                        similarQueries.add(previousQuery);
                    }
                }
            }
        }
        return similarQueries;
    }
}

package Models;

import app.TumblrFeed.supervisor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class SearchChecker implements supervisor {

    private ArrayList<String> words;

    public SearchChecker() {
        words = new ArrayList<>();

    }

    public void start() {
        words = getWords();
    }

    public void disconnect() {
        words = null;
    }

    private ArrayList<String> getWords() {
        ArrayList<String> ret = new ArrayList<>();

        ArrayList<Map<String, String>> rawWords = supervisor.getSql().select("SELECT words FROM t_illegal");

        for (Map<String, String> rawWord : rawWords) {
            ret.add(rawWord.get("word"));
        }

        return ret;
    }

    public boolean isIllegal(String toSearch, String searchName) {
        boolean isIllegal = false;

        String[] toSearchWords = toSearch.split(" ");
        String[] searchNameWords = searchName.split(" ");

        ArrayList<String> searchWords = new ArrayList<>();

        Collections.addAll(searchWords, toSearchWords);
        Collections.addAll(searchWords, searchNameWords);

        for (String searchWord : searchWords) {
            for (String word : words) {
                if (searchWord.equalsIgnoreCase(word)) {
                    isIllegal = true;
                    break;
                }
            }

            if (isIllegal) {
                break;
            }
        }

        return isIllegal;
    }


}

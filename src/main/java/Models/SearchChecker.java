/*
    Discord bot allowing the creation of Tumblr feeds
    Copyright (C) 2024  Lucielle Voeffray

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

    Contact:
        pro@lucielle.ch

*/

package Models;

import app.TumblrFeed.supervisor;
import org.jetbrains.annotations.NotNull;

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

    private @NotNull ArrayList<String> getWords() {
        ArrayList<String> ret = new ArrayList<>();

        ArrayList<Map<String, String>> rawWords = supervisor.getSql().select("SELECT words FROM t_illegal");

        for (Map<String, String> rawWord : rawWords) {
            ret.add(rawWord.get("word"));
        }

        return ret;
    }

    public boolean isIllegal(@NotNull String toSearch, @NotNull String searchName) {
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

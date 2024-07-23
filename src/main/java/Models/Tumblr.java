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
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Post;
import org.jetbrains.annotations.NotNull;
import secrets.secrets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

public class Tumblr implements supervisor {

    private final String consumerKey;
    private final String consumerSecret;
    private JumblrClient client;

    public Tumblr() {
        this.consumerKey = secrets.OAUTH_CONSUMER_KEY;
        this.consumerSecret = secrets.OAUTH_CONSUMER_SECRET;
    }

    public void connect() {
        client = new JumblrClient(consumerKey, consumerSecret);
    }

    public void start() {

        Sql sql = supervisor.getSql();

        while (true) {

            ArrayList<Map<String, String>> results = sql.select("SELECT search.hashtagName, channel.id, user.hashedName FROM t_search AS search JOIN t_channel as channel ON channel.pk_channel = search.fk_channel JOIN t_user AS user ON user.pk_user = search.fk_user WHERE search.paused = false");
            Discord discord = supervisor.getDiscord();
            List<Post> search;

            for (Map<String, String> row : results) {
                String channelID = row.get("id");
                String hashedName = row.get("hashedName");
                String hashtagName = row.get("hashtagName");

                search = search(hashtagName);
                search = getNewPosts(search, hashedName, hashtagName);

                discord.sendPosts(search, channelID, hashtagName);
            }


            try {
                sleep(3600000L);
            } catch (InterruptedException ignore) {
            }
        }
    }

    public @NotNull List<Post> search(@NotNull String toSearch) {
        return client.tagged(toSearch);
    }

    private @NotNull List<Post> getNewPosts(@NotNull List<Post> posts, String hashedUser, String searchName) {
        List<Post> newPosts = new ArrayList<>();
        String lastPost = null;

        lastPost = supervisor.getSql().select(String.format("SELECT lastSharedPost FROM t_search WHERE searchName = %s AND fk_user = (SELECT pk_user FROM t_user = %s)", searchName, hashedUser)).get(0).get("lastSharedPost");

        for (Post post : posts) {
            if (!post.getId().toString().equals(lastPost)) {
                newPosts.add(post);
            } else {
                break;
            }
        }

        return newPosts;
    }

    public Post getNewestPost(String toSearch) {
        return search(toSearch).get(0);
    }
}

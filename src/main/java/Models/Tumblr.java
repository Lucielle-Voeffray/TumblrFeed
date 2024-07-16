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
import secrets.secrets;

import java.util.List;

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
        while (true) {
            System.out.println("UwU");
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }

    public List<Post> search(String toSearch) {
        return client.tagged(toSearch);
    }

    private boolean checkLastPostSent() {
        boolean success = false;


        return success;
    }
}

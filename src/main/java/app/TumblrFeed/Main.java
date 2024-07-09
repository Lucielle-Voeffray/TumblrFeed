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

package app.TumblrFeed;

import services.Cypher;

public class Main {
    public static void main(String[] args) {

        /*// Create objects needed
        Tumblr tumblr = new Tumblr();
        Discord discord = new Discord();
        Sql sql = new Sql();

        // Set the different references to each object
        discord.setTumblr(tumblr);
        discord.setSql(sql);

        tumblr.setDiscord(discord);
        tumblr.setSql(sql);

        sql.setDiscord(discord);
        sql.setTumblr(tumblr);

        // Connect the clients
        if (!sql.connect()) {
            try {
                sleep(500);
            } catch (InterruptedException ignored) {
            }
            sql.connect();
        }
        discord.connect();

        // Create the commands
        discord.build();

        // Start the app
        discord.start();
        tumblr.start();*/

        System.out.println(Cypher.encrypt("Hello"));

    }
}
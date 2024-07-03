package app.TumblrFeed;

import Models.Discord;
import Models.Sql;
import Models.Tumblr;

import static java.lang.Thread.sleep;

public class Main {
    public static void main(String[] args) {

        // Create objects needed
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
        tumblr.start();


    }
}
package Models;

import secrets.secrets;

import static java.lang.Thread.sleep;

public class Tumblr {

    private final String key;
    private final String secret;
    private Discord discord;
    private Sql sql;

    public Tumblr() {
        this.key = secrets.TUMBLR_KEY;
        this.secret = secrets.TUMBLR_SECRET;
        this.discord = null;
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

    public Discord getDiscord() {
        return discord;
    }

    public void setDiscord(Discord discord) {
        this.discord = discord;
    }

    public Sql getSql() {
        return sql;
    }

    public void setSql(Sql sql) {
        this.sql = sql;
    }
}

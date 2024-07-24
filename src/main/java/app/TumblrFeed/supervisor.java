package app.TumblrFeed;

import Models.Discord;
import Models.Sql;
import Models.Tumblr;
import services.Error;
import services.LogType;

import java.util.HashMap;
import java.util.Map;

public interface supervisor {
    Map<String, supervisor> services = new HashMap<>();

    static void setService(String key, supervisor object) {
        services.put(key, object);
    }

    static Sql getSql() {
        return (Sql) services.get("SQL");
    }

    static Discord getDiscord() {
        return (Discord) services.get("Discord");
    }

    static Tumblr getTumblr() {
        return (Tumblr) services.get("Tumblr");
    }

    static boolean launchApp() {
        boolean success = true;
        int attemps = 0;
        boolean isSqlConnected = getSql().connect();

        while (!isSqlConnected && attemps < 3) {
            isSqlConnected = getSql().connect();
            attemps++;
        }

        if (!isSqlConnected) {
            success = false;
        }

        if (!getDiscord().connect()) {
            success = false;
        }

        getTumblr().connect();

        if (success) {
            getSql().start();
            getDiscord().start();
            getTumblr().start();
        } else {
            Error.report(LogType.ERROR, "supervisor.java", "launchApp", 0, "Something went wrong");
        }

        return success;
    }

    default void start() {
    }

}

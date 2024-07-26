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

import Models.Discord;
import Models.SearchChecker;
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

    static SearchChecker getSearchChecker() {
        return (SearchChecker) services.get("SearchChecker");
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
            getSearchChecker().start();
            Error.report(LogType.SUCCESS, "supervisor.java", "launchApp()", 0, "App launched successfully");
        } else {
            Error.report(LogType.ERROR, "supervisor.java", "launchApp", 1, "Something went wrong");
        }

        return success;
    }

    static void shutApp() {
        getSql().disconnect();
        getDiscord().disconnect();
        getTumblr().disconnect();
        getSearchChecker().disconnect();
        Error.report(LogType.INFO, "supervisor.java", "shutApp()", 0, "Shutting down gracefully");
    }

    default void start() {
    }

    default void disconnect() {
    }

}

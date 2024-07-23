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
import org.postgresql.ds.PGSimpleDataSource;
import secrets.secrets;
import services.Error;
import services.Hasher;
import services.LogType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Sql implements supervisor {

    private final String fqdn;
    private final String user;
    private final String password;
    private Connection connection;
    private DataSource datasource;

    public Sql() {
        fqdn = secrets.SQL_FQDN;
        user = secrets.SQL_USER;
        password = secrets.SQL_PASSWORD;
        datasource = null;
        connection = null;
    }

    /**
     * @description: Connects to the Database.
     * @Needs: The secrets.secrets.java file to be created and filled according to the documentation
     */
    public boolean connect() {
        boolean success = false;

        try {
            Class.forName("org.postgresql.Driver");
            createDataSource();
            connection = datasource.getConnection();
            success = true;

            Exception e = new Exception("Connected and Nicely");
            Error.report(LogType.SUCCESS, "Sql.java", "select()", 0, e);

        } catch (Exception e) {
            Error.report(LogType.ERROR, "Sql.java", "connect()", 0, e);
        }

        return success;
    }

    /**
     * @return true if success, false if failure
     * @Description: Creates the DataSource and assigns it to dataSource
     */
    private void createDataSource() {

        final String url = fqdn + "?user=" + user + "&password=" + password;
        PGSimpleDataSource source = new PGSimpleDataSource();
        source.setUrl(url);
        datasource = source;

    }

    /**
     * @param query: full SELECT query
     * @return Object ResultSet
     */
    public ArrayList<Map<String, String>> select(@NotNull String query) {
        ArrayList<Map<String, String>> ret;

        ResultSet result = null;
        int columnCount = 0;

        PreparedStatement stmt = null;

        try {
            stmt = connection.prepareStatement(query);
        } catch (SQLException e) {
            Error.report(LogType.ERROR, "Sql.java", "select()", 0, e);
        }

        if (stmt != null) {
            try {
                result = stmt.executeQuery();
                columnCount = result.getMetaData().getColumnCount();
            } catch (SQLException e) {
                Error.report(LogType.ERROR, "Sql.java", "select()", 1, e);
                String code = e.getSQLState();

                if (code.equals("08000")) {
                    boolean connected = connect();
                    if (connected) {
                        try {
                            stmt = connection.prepareStatement(query);
                            result = stmt.executeQuery();
                        } catch (SQLException r) {
                            Error.report(LogType.ERROR, "Sql.java", "connect()", 2, r);
                        }
                    }
                } else {
                    Error.report(LogType.ERROR, "Sql.java", "connect()", 3, e);
                }
            }
        }

        ret = new ArrayList<>(columnCount);
        try {
            while (result.next()) {
                Map<String, String> map = new HashMap<>();
                for (int i = 0; i < columnCount; i++) {
                    String key = result.getMetaData().getColumnName(i);
                    String value = result.getString(i);
                    map.put(key, value);
                }
                ret.add(map);
            }
        } catch (SQLException e) {
            Error.report(LogType.ERROR, "Sql.java", "connect()", 4, e);
        }

        return ret;
    }

    /**
     * @param query full INSERT/UPDATE/DELETE query
     * @return int representing the number of INSERT/UPDATE/DELETE done
     */
    public int update(@NotNull String query) {
        int success = 0;

        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(query);
        } catch (Exception e) {
            Error.report(LogType.ERROR, "Sql.java", "connect()", 0, e);
        }

        if (stmt != null) {
            try {
                success = stmt.executeUpdate();
            } catch (SQLException e) {
                Error.report(LogType.ERROR, "Sql.java", "connect()", 1, e);
                String code = e.getSQLState();
                if (code.equals("08000")) {
                    if (connect()) {
                        try {
                            stmt = connection.prepareStatement(query);
                        } catch (SQLException r) {
                            Error.report(LogType.ERROR, "Sql.java", "connect()", 2, r);
                        }
                    }
                } else {
                    Error.report(LogType.ERROR, "Sql.java", "connect()", 3, e);
                }
            }
        }
        return success;
    }

    public boolean createSearch(String search, String searchName, String userID, String server, String channel) {
        boolean success = false;

        if (!userAccountExists(userID)) {
            createUserAccount(userID);
        }

        if (isSearchNameAvailable(userID, searchName) && !isUserPaused(userID)) {
            String fkChannel = String.format("SELECT pk_channel FROM t_channel WHERE id = %s", channel);
            String fkUser = String.format("SELECT pk_user FROM t_user WHERE hashedName = %s", Hasher.hash(userID));
            String lastPost = supervisor.getTumblr().getNewestPost(search).getId().toString();
            update(String.format("INSERT INTO t_search (searchName, fk_channel, fk_user, paused, hashtagName, lastSharedPost) VALUES (%s, %s, %s, false, %s, %s)", searchName, fkChannel, fkUser, search, lastPost));
        }

        return success;
    }

    private boolean isUserPaused(String userID) {
        return select(String.format("SELECT paused FROM t_user WHERE hashedName = %s", Hasher.hash(userID))).get(0).get("disabled").equalsIgnoreCase("true");
    }

    private boolean createUserAccount(String userID) {
        return update(String.format("INSERT INTO t_user (hashedName, disabled, app_admin) VALUES (%s, false, false)", Hasher.hash(userID))) == 1;
    }

    private boolean pauseUser(String userToPause, String userWhoAsked) {
        boolean success = false;

        if (isAppAdmin(userWhoAsked)) {
            int upD = update(String.format("UPDATE t_user SET disabled = true WHERE hashedName = %s", Hasher.hash(userToPause)));

            String pkUser = select(String.format("SELECT paused FROM t_search WHERE hashedName = %s", Hasher.hash(userToPause))).get(0).get("pk_user");

            int numberOfSearches = select(String.format("SELECT paused, pk_search FROM t_search WHERE fk_user = %s", pkUser)).size();

            upD += update(String.format("UPDATE t_search SET paused = true WHERE fk_user = %s", pkUser));

            if (upD == 1 + numberOfSearches) {
                success = true;
            }
        }

        return success;
    }

    private boolean isAppAdmin(String userID) {
        return select(String.format("SELECT app_admin FROM t_user WHERE hashedName = %s", Hasher.hash(userID))).get(0).get("app_admin").equalsIgnoreCase("true");
    }

    private boolean userAccountExists(String userID) {
        return Objects.equals(select(String.format("SELECT id FROM t_user WHERE id = %s", Hasher.hash(userID))).get(0).get("id"), Hasher.hash(userID));
    }

    private boolean isSearchNameAvailable(String userID, String searchName) {
        boolean isAvailable = false;
        ArrayList<Map<String, String>> result = select(String.format("SELECT search.searchName FROM t_search AS search JOIN t_user AS user ON user.pk_user = search.fk_search WHERE user.hashedName = %s AND search.searchName = %s", Hasher.hash(userID), searchName));

        int numberOfRows = result.size();

        if (numberOfRows == 0) {
            isAvailable = true;
        }

        return isAvailable;
    }
}

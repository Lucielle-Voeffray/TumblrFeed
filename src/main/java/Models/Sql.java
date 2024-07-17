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
    public ArrayList<ArrayList> select(@NotNull String query) {
        ArrayList<ArrayList> ret;

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
                int i = 1;
                while (i <= columnCount) {
                    result.getArray(i++);
                }
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

        if (isSearchNameAvailable(userID, searchName)) {
            String fkChannel = String.format("SELECT pk_channel FROM t_channel WHERE id = %s", channel);
            String fkUser = String.format("SELECT pk_user FROM t_user WHERE hashedName = %s", Hasher.hash(userID));
            String lastPost = supervisor.getTumblr().getNewestPost(search).getId().toString();
            update(String.format("INSERT INTO t_search (searchName, fk_channel, fk_user, paused, hashtagName, lastSharedPost) VALUES (%s, %s, %s, false, %s, %s)", searchName, fkChannel, fkUser, search, lastPost));
        }

        return success;
    }

    private boolean isSearchNameAvailable(String userID, String searchName) {
        boolean isAvailable = false;
        int numberOfRows = -1;
        ResultSet result = select(String.format("SELECT search.searchName FROM t_search AS search JOIN t_user AS user ON user.pk_user = search.fk_search WHERE user.hashedName = %s AND search.searchName = %s", Hasher.hash(userID), searchName));

        try {
            result.last();
            numberOfRows = result.getRow();
            result.first();
        } catch (SQLException e) {
            Error.report(LogType.ERROR, "Sql.java", "isSearchNameUsed()", 1, e);
        }

        if (numberOfRows == 0) {
            isAvailable = true;
        }

        return isAvailable;
    }
}

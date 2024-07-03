package Models;

import org.jetbrains.annotations.NotNull;
import org.postgresql.ds.PGSimpleDataSource;
import secrets.secrets;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Sql {

    private final String fqdn;
    private final String user;
    private final String password;
    private Connection connection;
    private DataSource datasource;
    private Discord discord;
    private Tumblr tumblr;

    public Sql() {
        fqdn = secrets.SQL_FQDN;
        user = secrets.SQL_USER;
        password = secrets.SQL_PASSWORD;
        datasource = null;
        connection = null;
        discord = null;
        tumblr = null;
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

            // TODO add success log
            System.out.println("success");

        } catch (Exception e) {
            // TODO add error log
            System.out.println(e);
        }

        return success;
    }

    /**
     * @return true if success, false if failure
     * @description: Creates the DataSource and assigns it to dataSource
     */
    private boolean createDataSource() {
        boolean success = false;

        try {
            final String url = fqdn + "?user=" + user + "&password=" + password;
            PGSimpleDataSource source = new PGSimpleDataSource();
            source.setUrl(url);
            datasource = source;
            success = true;
            // TODO ADD log success

        } catch (Exception e) {
            // TODO add log error
            System.out.println(e);
        }

        return success;
    }

    /**
     * @param query: full SELECT query
     * @return Object ResultSet
     */
    public @NotNull ResultSet select(String query) {
        ResultSet result = null;

        PreparedStatement stmt = null;

        try {
            stmt = connection.prepareStatement(query);
            // TODO add success log
        } catch (SQLException e) {
            // TODO add error log
            System.out.println(e);
        }

        if (stmt != null) {
            try {
                result = stmt.executeQuery();
                // TODO add success log
            } catch (SQLException e) {
                // TODO add error log
                String code = e.getSQLState();
                switch (code) {
                    case "08000":
                        boolean connected = connect();
                        if (connected) {
                            try {
                                stmt = connection.prepareStatement(query);
                            } catch (SQLException r) {
                                // TODO add error log
                            }
                        }
                        break;
                    case "42601":
                        // TODO add error log
                        System.out.println(e);
                    default:
                        // TODO add error log
                        System.out.println("SQL failed with error code: " + code);
                }
            }
        }
        return result;
    }

    /**
     * @param query full INSERT/UPDATE/DELETE query
     * @return int representing the number of INSERT/UPDATE/DELETE done
     */
    public int update(String query) {
        int success = 0;

        if (query != null) {
            PreparedStatement stmt = null;
            try {
                stmt = connection.prepareStatement(query);
                // TODO add success log
            } catch (Exception e) {
                // TODO add error log
            }

            if (stmt != null) {
                try {
                    success = stmt.executeUpdate();
                    // TODO add success log
                } catch (SQLException e) {
                    // TODO add error log
                    String code = e.getSQLState();
                    switch (code) {
                        case "08000":
                            boolean connected = connect();
                            if (connected) {
                                try {
                                    stmt = connection.prepareStatement(query);
                                } catch (SQLException r) {
                                    // TODO add error log
                                }
                            }
                            break;
                        case "42601":
                            // TODO add error log
                            System.out.println(e);
                        default:
                            // TODO add error log
                            System.out.println("SQL failed with error code: " + code);
                    }
                }
            }
        }
        return success;
    }

    public Discord getDiscord() {
        return discord;
    }

    public void setDiscord(Discord discord) {
        this.discord = discord;
    }

    public Tumblr getTumblr() {
        return tumblr;
    }

    public void setTumblr(Tumblr tumblr) {
        this.tumblr = tumblr;
    }
}

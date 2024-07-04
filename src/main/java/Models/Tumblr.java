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

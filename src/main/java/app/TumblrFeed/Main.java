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
import Models.Sql;
import Models.Tumblr;

public class Main {
    public static void main(String[] args) {

        Discord discord = new Discord();
        Tumblr tumblr = new Tumblr();
        Sql sql = new Sql();

        supervisor.setService("Discord", discord);
        supervisor.setService("Tumblr", tumblr);
        supervisor.setService("SQL", sql);

        supervisor.launchApp();

    }
}
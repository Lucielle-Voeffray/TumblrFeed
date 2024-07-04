
--    Discord bot allowing the creation of Tumblr feeds
--    Copyright (C) 2024  Lucielle Voeffray
--
--    This program is free software: you can redistribute it and/or modify
--    it under the terms of the GNU General Public License as published by
--    the Free Software Foundation, either version 3 of the License, or
--    any later version.
--
--    This program is distributed in the hope that it will be useful,
--    but WITHOUT ANY WARRANTY; without even the implied warranty of
--    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--    GNU General Public License for more details.
--
--    You should have received a copy of the GNU General Public License
--    along with this program.  If not, see <https://www.gnu.org/licenses/>.
--
--    Contact:
--        pro@lucielle.ch


CREATE DATABASE db_TumblrFeed;

CREATE TABLE IF NOT EXISTS t_server (
    pk_server serial,
    serverName varchar(100) NOT NULL,
    id varchar(30) NOT NULL,

    PRIMARY KEY (pk_server)
);

CREATE TABLE IF NOT EXISTS t_user (
    pk_user serial,
    hashedName varchar(100) NOT NULL,
    disabled boolean NOT NULL,
    app_admin boolean NOT NULL,

    PRIMARY KEY (pk_user)
);

CREATE TABLE IF NOT EXISTS t_channel (
    pk_channel serial,
    id varchar(30) NOT NULL,
    channelName varchar(100) NOT NULL,
    fk_server integer NOT NULL,

    PRIMARY KEY (pk_channel),

    FOREIGN KEY (fk_server) REFERENCES t_server (pk_server)
);

CREATE TABLE IF NOT EXISTS t_search (
    pk_search serial,
    searchName varchar(20) NOT NULL,
    fk_channel integer NOT NULL,
    fk_user integer NOT NULL,
    creation date,
    paused boolean NOT NULL,
        CHECK (paused IN (true, false)),
    hashtagName varchar(180) NOT NULL,
    lastSharedPost varchar(300) NOT NULL,

    PRIMARY KEY (pk_search),

    FOREIGN KEY (fk_channel) REFERENCES t_channel (pk_channel),
    FOREIGN KEY (fk_user) REFERENCES t_user (pk_user)
);

CREATE TABLE IF NOT EXISTS t_log (
    pk_log serial,
    creation date NOT NULL,
    username varchar(20) NOT NULL,
    logDescription varchar(500) NOT NULL,

    PRIMARY KEY (pk_log)
);

CREATE ROLE "tumblrfeed" WITH
	LOGIN
	NOSUPERUSER
	NOCREATEDB
	NOCREATEROLE
	INHERIT
	NOREPLICATION
	NOBYPASSRLS
	CONNECTION LIMIT -1
	PASSWORD 'CHANGE THIS WHEN CREATING THE DATABASE';

GRANT SELECT, INSERT, UPDATE, DELETE ON t_channel, t_search, t_server, t_user TO tumblrfeed;

CREATE OR REPLACE FUNCTION addLog() RETURNS TRIGGER AS $t_log$
    BEGIN
        INSERT INTO t_log (creation, username, logDescription) VALUES (now(), get_app_user(), concat(TG_OP, ' on ', TG_TABLE_NAME));
        RETURN NULL;
    END
$t_log$ LANGUAGE plpgsql;


CREATE TRIGGER t_log
    AFTER INSERT OR UPDATE OR DELETE ON t_server
    FOR EACH ROW
    EXECUTE FUNCTION addLog();

CREATE TRIGGER t_log
    AFTER INSERT OR UPDATE OR DELETE ON t_channel
    FOR EACH ROW
    EXECUTE FUNCTION addLog();

CREATE TRIGGER t_log
    AFTER INSERT OR UPDATE OR DELETE ON t_search
    FOR EACH ROW
    EXECUTE FUNCTION addLog();

CREATE TRIGGER t_log
    AFTER INSERT OR UPDATE OR DELETE ON t_user
    FOR EACH ROW
    EXECUTE FUNCTION addLog();

CREATE OR REPLACE FUNCTION addDate() RETURNS TRIGGER AS $AddDate$
    BEGIN
        NEW.creation = now();
        RETURN NEW.creation;
    END;
$AddDate$ LANGUAGE plpgsql;

CREATE TRIGGER AddDate
    BEFORE INSERT OR UPDATE ON t_search
    FOR EACH ROW
    EXECUTE FUNCTION addDate();

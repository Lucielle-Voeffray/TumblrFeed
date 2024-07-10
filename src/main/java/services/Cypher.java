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

package services;


import org.jetbrains.annotations.NotNull;
import secrets.secrets;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Cypher {

    public static boolean importKey() {
        String pubKey = secrets.GPG_PUBLIC_KEY_FILE;
        boolean succes = false;
        String[] toImport = {"gpg", "--import", pubKey};
        try {
            if (Runtime.getRuntime().exec(toImport) != null) {
                succes = true;
            }
        } catch (java.io.IOException e) {
            System.out.printf("%s [ERROR] SELECT FAILURE Cypher.java method: importKey: 1rst catch | Error Message: %s%n", java.time.LocalDateTime.now(), e);
        }
        return succes;
    }


    public static @NotNull String encrypt(String unsafeText) {
        String safeText = "";

        String mail = secrets.MAIL_USER;
        String[] cmdCypher = {
                "/bin/sh",
                "-c",
                "echo " + unsafeText + " | gpg --encrypt --armor --recipient " + mail
                // String.format("echo %s | gpg --encrypt --armor --recipient %s", unsafeText, mail)
        };
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmdCypher);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s;
            boolean prout = false;
            while (!prout) {
                if ((reader.readLine()).equals("-----BEGIN PGP MESSAGE-----")) {
                    safeText = String.format("-----BEGIN PGP MESSAGE-----%n");
                    prout = true;
                }
            }
            while ((s = reader.readLine()) != null) {
                safeText += String.format("%s%n", s);
            }
        } catch (java.io.IOException e) {
            System.out.printf("%s [ERROR] FAILURE Cypher.java method: encrypt: 1rst catch | Error Message: %s%n", java.time.LocalDateTime.now(), e);
        }
        return safeText.toString();
    }
}

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

import app.TumblrFeed.supervisor;
import org.jetbrains.annotations.NotNull;
import secrets.secrets;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;


public class Mail {

    private static final String USER_MAIL = secrets.MAIL_USER;
    private static final String USER_PASSWORD = secrets.MAIL_PASSWORD;
    private static final String DESTINATION_MAIL = secrets.DESTINATION_EMAIL;
    private static final String HOST = secrets.SMTP_SERVER;

    public static boolean sendMail(String discordUsername, String userID, String searchName, String search, String serverID, String channelID) {
        boolean success = false;

        Properties properties = System.getProperties();

        properties.setProperty("mail.smtp.host", HOST);

        Session session = Session.getDefaultInstance(properties);

        String safeContent = getContent(discordUsername, userID, searchName, search, serverID, channelID);

        String subject = supervisor.getSql().select("SELECT english FROM t_text WHERE pk = 8").get(0).get("english");

        try {
            properties.setProperty("mail.user", USER_MAIL);
            properties.setProperty("mail.password", USER_PASSWORD);

            MimeMessage message = new MimeMessage(session);

            message.setFrom(USER_MAIL);

            message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(DESTINATION_MAIL));

            message.setSubject(subject);

            message.setText(safeContent);

            Transport.send(message);
            success = true;

        } catch (MessagingException e) {
            System.out.printf("%s [ERROR] FAILURE Mail.java method: sendMail | Error Message: %s%n", java.time.LocalDateTime.now(), e);
        }

        return success;
    }

    public static @NotNull String getContent(String discordUsername, String userID, String searchName, String search, String serverID, String channelID) {
        String hash = Hasher.hash(discordUsername);
        String mail = supervisor.getSql().select("SELECT english FROM t_text WHERE pk = 7").get(0).get("english");
        String content = String.format(mail, discordUsername, discordUsername, hash, userID, serverID, channelID, searchName, search, userID);

        String safeContent = Cypher.encrypt(content);
        System.out.println(safeContent);

        return safeContent;
    }
}
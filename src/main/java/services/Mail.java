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

import secrets.secrets;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;


public class Mail {

    private static final String USER_MAIL = secrets.MAIL_USER;
    private static final String DESTINATION_MAIL = secrets.DESTINATION_EMAIL;
    private static final String HOST = "localhost";

    public static boolean sendMail(String discordUsername, String userID, String searchName, String search, String serverName, String serverID, String channelName, String channelID) {
        boolean success = true;

        Properties properties = System.getProperties();

        properties.setProperty("mail.smtp.host", HOST);

        Session session = Session.getDefaultInstance(properties);

        String content = String.format("%s tried to create a search with an illegal word ! %n", discordUsername) +
                String.format("Discord Username / Hash / id : %s / %s / %s%n", discordUsername, Hasher.hash(discordUsername), userID) +
                String.format("Discord Server / Server ID : %s / %s%n", serverName, serverID) +
                String.format("Discord Channel / Channel ID : %s / %s%n", channelName, channelID) +
                String.format("Tumblr Search Name : %s", searchName) +
                String.format("Tumblr Search content : %s", search);

        String safeContent = Cypher.encrypt(content);

        try {
            MimeMessage message = new MimeMessage(session);

            message.setFrom(USER_MAIL);

            message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(DESTINATION_MAIL));

            message.setSubject("[EMERGENCY] use of an illegal term in their search!");

            message.setText(safeContent);

            Transport.send(message);

        } catch (MessagingException e) {
            System.out.printf("%s [ERROR] FAILURE Mail.java method: sendMail | Error Message: %s%n", java.time.LocalDateTime.now(), e);
        }

        return success;
    }
}
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

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.apache.commons.codec.binary.Base64;
import secrets.secrets;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;


public class Mail {

    private static final String USER_MAIL = secrets.MAIL_USER;
    private static final String PASSWORD = secrets.MAIL_PASSWORD;
    private static final String DESTINATION_MAIL = secrets.DESTINATION_EMAIL;
    private static final String API_KEY = secrets.GOOGLE_API_KEY;

    public static boolean sendMail(String discordUsername, String userID, String searchName, String search, String serverName, String serverID, String channelName, String channelID) {
        boolean success = true;
        boolean[] trySuccess = new boolean[3];

        String bodyText = generateContent(discordUsername, userID, searchName, search, serverName, serverID, channelName, channelID);
        MimeMessage mailContent = null;
        Message mail = null;

        try {
            mailContent = createEmail(bodyText);
            trySuccess[0] = true;
        } catch (MessagingException e) {
            System.out.printf("%s [ERROR] FAILURE Mail.java method: sendMail: 1rst catch | Error Message: %s%n", java.time.LocalDateTime.now(), e);
        }

        try {
            mail = createMessageWithEmail(mailContent);
            trySuccess[1] = true;
        } catch (IOException | MessagingException e) {
            System.out.printf("%s [ERROR] FAILURE Mail.java method: sendMail: 2nd catch | Error Message: %s%n", java.time.LocalDateTime.now(), e);
        }

        /*try {
            sendEmail(USER_MAIL, DESTINATION_MAIL);
            trySuccess[2] = true;
        } catch (javax.mail.MessagingException | java.io.IOException e) {
            System.out.printf("%s [ERROR] FAILURE Mail.java method: sendMail: 3rd catch | Error Message: %s%n", java.time.LocalDateTime.now(), e);
        }*/



        return success;
    }

    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param bodyText body text of the email
     * @return the MimeMessage to be used to send email
     * @throws MessagingException - if a wrongly formatted address is encountered.
     */
    private static MimeMessage createEmail(String bodyText) throws MessagingException {
        String toEmailAddress = secrets.DESTINATION_EMAIL;
        String fromEmailAddress = secrets.MAIL_USER;
        String subject = "[URGENT] Illegal search";
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(fromEmailAddress));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(toEmailAddress));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    private static String generateContent(String discordUsername, String userID, String searchName, String search, String serverName, String serverID, String channelName, String channelID) {
        StringBuilder content = new StringBuilder();
        String hashedUsername = Hasher.hash(discordUsername);

        content.append(String.format("%s has broken the search rules by looking up illegal terms ! %n", discordUsername));
        content.append(String.format("Hashed username: %s%n", hashedUsername));
        content.append(String.format("Discord user name / ID: %s / %s%n", discordUsername, userID));
        content.append(String.format("Discord server name / ID : %s / %s%n", serverName, serverID));
        content.append(String.format("Discord channel name / ID : %s / %s%n", channelName, channelID));
        content.append(String.format("Discord search name / search : %s / %s%n", searchName, search));

        return content.toString();
    }

    public static Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    /**
     * Send an email from the user's mailbox to its recipient.
     *
     * @param fromEmailAddress - Email address to appear in the from: header
     * @param toEmailAddress   - Email address of the recipient
     * @return the sent message, {@code null} otherwise.
     * @throws MessagingException - if a wrongly formatted address is encountered.
     * @throws IOException        - if service account credentials file not found.
     */
    public static Message sendEmail(String fromEmailAddress, String toEmailAddress) throws MessagingException, IOException {

        /* Load pre-authorized user credentials from the environment.
           TODO(developer) - See https://developers.google.com/identity for
            guides on implementing OAuth2 for your application.*/

        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(GmailScopes.GMAIL_SEND);
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        // Create the gmail API client
        Gmail service = new Gmail.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName("Gmail samples")
                .build();

        // Create the email content
        String messageSubject = "Test message";
        String bodyText = "lorem ipsum.";

        // Encode as MIME message
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(fromEmailAddress));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(toEmailAddress));
        email.setSubject(messageSubject);
        email.setText(bodyText);

        // Encode and wrap the MIME message into a gmail message
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
        Message message = new Message();
        message.setRaw(encodedEmail);

        try {
            // Create send message
            message = service.users().messages().send("me", message).execute();
            return message;
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 403) {
                System.out.printf("%s [ERROR] FAILURE Mail.java method: sendEmail: 1rst catch | Error Message: %s%n", java.time.LocalDateTime.now(), e);
                ;
            } else {
                System.out.printf("%s [ERROR] FAILURE Mail.java method: sendEmail: 2nd catch | Error Message: %s%n", java.time.LocalDateTime.now(), e);
                ;
            }
        }
        return null;
    }























}

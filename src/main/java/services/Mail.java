package services;

import secrets.secrets;

public class Mail {

    public static final String PUB_KEY = secrets.GPG_PUB_KEY;
    public static final String USER = secrets.MAIL_USER;
    public static final String PASSWORD = secrets.MAIL_PASSWORD;
    public static final String DESTINATION = secrets.DESTINATION_EMAIL;

    public static boolean sendMail(String content, String object) {
        boolean success = false;

        return success;
    }

}

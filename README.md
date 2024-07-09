# TumblrFeed

## Description

This is a Discord bot that will perform searches on Tumblr for you and create a personalized feed in a Discord channel.
In order to keep all the informations needed, I decided to set a postgres database up.

## Privacy of information

In order to keep your informations private, the database will not store your discord handle, but a hash of your handle.
That allows the bot to still find your searches on the database but denies identification of any sort to anyone who
might access the database with ill intent.

## Report bugs

To report bug, you can simply add an [issue](https://github.com/Lucielle-Voeffray/TumblrFeed/issues).

Please, remember to add a maximum of details you might have in order to facilitate my work and check if the report for
that bug already exists.

## Report security issues

You can report any security issues to me via <a href="mailto:pro@lucielle.ch">Email</a>

If you know me you can also send me a message over Discord or [Matrix]

Please, remember to add a maximum of details you might have in order to facilitate my work.

## Contribute

At the moment I don't take contributions other than bug/security reports, I want to do the entire project on my own.

## How to use it

To use it you'll need to clone this repository and create a file here: ./TumblrFeed/src/main/java/secrets/secrets.java

Inside ot this secrets.java file you'll need to add these constants:

```java
    // Discord secrets
public static final String DISCORD = "YOUR APPLICATION TOKEN";
public static final long ADMIN_SERVER = Your_server_ID;

// Tumblr secrets
public static final String TUMBLR_SECRET = "YOUR TUMBLR SECRET";
public static final String TUMBLR_KEY = "YOUR TUMBLR KEY";

// SQL secrets
public static final String SQL_USER = "TrumblrFeed";
public static final String SQL_PASSWORD = "PASSWORD FOR TUMBLRFEED (SET IN /TumblrFeed/src/main/sql/tumblrFeed.sql)";
public static final String SQL_FQDN = "jdbc:postgresql://[URL TO YOUR DATABASE]:[THE PORT YOUR DATABASE LISTENS TO]/db_TumblrFeed";

// E-Mails alerts
public static final String GPG_PUBLIC_KEY_FILE = "FILEPATH TO YOUR PUBLIC GPG KEY FOR YOUR ALERT EMAIL"; // You can put your key file in src/main/java/secrets
public static final String MAIL_USER = "USERNAME FOR YOUR ALERTS EMAIL";
public static final String MAIL_PASSWORD = "PASSWORD FOR YOUR ALERTS EMAIL";
public static final String DESTINATION_EMAIL = "TO WHAT ADDRESS THE EMAIL SHOULD BE SENT"; // If, like me, you are lazy, just put the same as MAIL_USER, it'll work fine
```

## Many thanks to:

- [EcklerOChokola](https://github.com/EcklerOChokola), who read my code and gave me advice
- [Lawhan](), who suggested this project to me

package org.example.utils;

import jakarta.mail.*;
import jakarta.mail.search.FromStringTerm;
import jakarta.mail.search.SearchTerm;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import static org.example.utils.ConfigReader.getGmailAppMail;
import static org.example.utils.ConfigReader.getGmailAppPassword;

@Log
public class MailReader {

    public static Store establishConnection() throws MessagingException {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");

        Session session = Session.getDefaultInstance(props, null);

        Store store = session.getStore("imaps");
        store.connect("imap.googlemail.com", getGmailAppMail(), getGmailAppPassword());
        return store;
    }

    // https://www.baeldung.com/java-access-gmail-imap

    public static void readEmails(Store store) throws MessagingException, IOException {
        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_ONLY);
        Message[] messages = inbox.getMessages();
        if (messages.length > 0) {
            Message message = messages[0];
            log.info("Subject: " + message.getSubject());
            log.info("From: " + Arrays.toString(message.getFrom()));
            log.info("Text: " + message.getContent());
        }
        inbox.close(true);
    }

    public static void searchEmails(Store store, String from) throws MessagingException {
        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_ONLY);
        SearchTerm senderTerm = new FromStringTerm(from);
        Message[] messages = inbox.search(senderTerm);
        Message[] getFirstFiveEmails = Arrays.copyOfRange(messages, 0, 5);
        for (Message message : getFirstFiveEmails) {
            log.info("Subject: " + message.getSubject());
            log.info("From: " + Arrays.toString(message.getFrom()));
        }
        inbox.close(true);
    }

}

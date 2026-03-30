package org.example.utils;

import jakarta.mail.*;
import jakarta.mail.search.FromStringTerm;
import jakarta.mail.search.SearchTerm;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static Message[] readEmails(Store store) throws MessagingException, IOException {
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
        return messages;
    }

    public static void searchEmails(Store store, String from) throws MessagingException {
        searchEmails(store, from, 5);
    }

    public static Message[] searchEmails(Store store, String from, int messageLimit) throws MessagingException {
        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_ONLY);
        SearchTerm senderTerm = new FromStringTerm(from);
        Message[] messages = inbox.search(senderTerm);
        Message[] getFirstNEmails = Arrays.copyOfRange(messages, 0, messageLimit);
        for (Message message : getFirstNEmails) {
            log.info("Subject: " + message.getSubject());
            log.info("From: " + Arrays.toString(message.getFrom()));
        }
        inbox.close(true);
        return getFirstNEmails;
    }

    /**
     *
     * @param store - your session
     * @param from - from email, search parameter
     * @return the last Message object found
     * @throws MessagingException
     */
    public Message findLastMessage(Store store, String from) throws MessagingException {
        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_ONLY);
        SearchTerm senderTerm = new FromStringTerm(from);
        Message[] messages = inbox.search(senderTerm);
        Message lastMessage = messages[messages.length - 1];
        return lastMessage;
    }

    public String parseEmailGetCode(Message message) throws MessagingException, IOException {
        Object html = message.getContent();

        Pattern pattern = Pattern.compile("<b>(\\d{6})</b>");
        Matcher matcher = pattern.matcher(html.toString());

        String code = null;
        if (matcher.find()) {
            code = matcher.group(1);
            log.info("Verification code: " + code); // Output: 123456
        } else if (StringUtils.isEmpty(code)) {
            // TODO may be thrwo may be observer event. or retry
        }
        return code;
    }


    public int getMessageAmount(Store store) {
        try {
            return store.getFolder("Inbox").getMessageCount();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void waitTillNewMailReceived(Store store, int initialMessageCount) {
        // Retry mechanism
        int maxAttempts = 40; // how many times to check
        int waitMillis = 5000; // how long to wait between checks (5 seconds)
        int updatedMailAmount = initialMessageCount;
        int attempt = 0;

        while (attempt < maxAttempts) {
            updatedMailAmount = getMessageAmount(store);
            if (updatedMailAmount > initialMessageCount) {
                System.out.println("New mail received, blyat! Total: " + updatedMailAmount);
                break;
            }
            attempt++;
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (updatedMailAmount == initialMessageCount) {
            throw new RuntimeException("No new mail received after waiting, pizdeц!");
        }
    }

}

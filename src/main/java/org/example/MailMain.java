package org.example;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Store;
import lombok.extern.java.Log;
import org.example.utils.MailReader;

@Log
public class MailMain {
    public static void main(String[] args) {
//verification code via email
        MailReader mailReader = new MailReader();
        try {
            Store store = mailReader.establishConnection();
            log.info("If it works here you will see the amount of emails " + store.getFolder("Inbox").getMessageCount());
//                mailReader.readEmails(store);
            Message messageWithCode = mailReader.findLastMessage(store, "noreply@info.luxmed.pl");
            String code = mailReader.parseEmailGetCode(messageWithCode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

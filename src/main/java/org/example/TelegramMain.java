package org.example;

import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.java.Log;
import org.example.utils.Telegram;

@Log
public class TelegramMain {
    public static void main(String[] args) {

        Telegram telegram = new Telegram();
        try {
            log.info("sending test message");
            SendResponse response = telegram.sendMessage("test first message");
            try {
                log.info(response.toString());
            } catch (Exception e) {
                log.severe("Telegram response failed. " + e.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

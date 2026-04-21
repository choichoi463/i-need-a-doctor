package org.example;

import lombok.extern.java.Log;
import org.example.utils.Telegram;

@Log
public class TelegramMain {
    public static void main(String[] args) {

        Telegram telegram = new Telegram();
        try {
           telegram.sendMessage("test first message");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

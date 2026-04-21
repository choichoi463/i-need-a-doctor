package org.example.utils;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Telegram {

    /**
     * Send message using telegram bot https://rieckpil.de/howto-send-telegram-bot-notifications-with-java/
     *
     * @param message
     */
    public static void sendMessageTelegramBot(String message) {
        // Create your bot passing the token received from @BotFather
        String token = "set yours auth key!"; // set yours auth key

        TelegramBot bot = new TelegramBot(token);

        // Register for updates
        bot.setUpdatesListener(updates -> {
            // ... process updates
            // return id of last processed update or confirm them all
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        // Send messages
        long chatId = 342962942; //set yours chat id
        SendResponse response = bot.execute(new SendMessage(chatId, message));
    }

    /**
     * Get time now for the message
     *
     * @return string time
     */
    public static String getDateTimeNow() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    public static void sendMessage(String message) {
        sendMessageTelegramBot(message + " " + getDateTimeNow());
    }
}


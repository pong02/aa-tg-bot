package aa.entity;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;

@Slf4j
public class Bot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "aa-tg-bot";
    }

    @Override
    public String getBotToken() {
        return "x";
    }

    @Override
    public void onUpdateReceived(Update update) {
        var msg = update.getMessage();
        var user = msg.getFrom();
        var id = user.getId();
        long chatId = update.getMessage().getChatId();


        System.out.println(user.getFirstName() + " wrote " + msg.getText());
        log.info(user.getFirstName() + " wrote " + msg.getText());

        if(msg.getText().equals("/help")) {
            sendText(id, "https://pong02.github.io/bot-help/");
        } else if (msg.getText().equals("/ping")) {
            try {
                long startTime = System.currentTimeMillis();
                SendMessage response = new SendMessage();
                response.setChatId(String.valueOf(chatId));
                response.setText("ok");
                execute(response);

                // Record the end time after the message is sent
                long endTime = System.currentTimeMillis();

                long latency = endTime - startTime;

                // Send the latency time back
                SendMessage latencyMessage = new SendMessage();
                latencyMessage.setChatId(String.valueOf(chatId));
                latencyMessage.setText(latency + "ms");
                execute(latencyMessage);
            } catch (TelegramApiException e) {
                log.info(Arrays.toString(e.getStackTrace()));
            }
        }
    }

    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what).build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }
}
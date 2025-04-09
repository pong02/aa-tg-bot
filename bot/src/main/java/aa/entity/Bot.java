package aa.entity;

import aa.helper.HelpPaginator;
import aa.helper.MessageBodyHelper;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.vdurmont.emoji.EmojiParser;

import java.util.Arrays;
import java.util.List;

import static java.lang.Math.toIntExact;

@Slf4j
public class Bot implements LongPollingSingleThreadUpdateConsumer {

    List<String> helpItems;

    Integer helpPage;

    private final TelegramClient telegramClient;

    public Bot(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public void consume(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            String name = update.getMessage().getFrom().getFirstName();
            Long id = update.getMessage().getFrom().getId();
            String txt = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            String reply = "ok buddy "+EmojiParser.parseToUnicode(":nerd:");
            String errorEmoji = EmojiParser.parseToUnicode(":no_entry_sign:");
            InlineKeyboardMarkup buttons = null;

            log.info(name + " said "+ update.getMessage());
            System.out.println(name + " said "+ txt);

            if (txt.startsWith("/")){
                String command = Arrays.stream(txt.split(" ")).findFirst().orElseThrow();
                switch (command){
                    case "/menu":
                        System.out.println("Showing command menu");
                        buttons = MessageBodyHelper.menuBody;
                        break;
                    case "/help":
                        System.out.println("Showing help wiki");
                        helpItems = HelpPaginator.loadHelpItems();
                        helpPage = 1;
                        reply = HelpPaginator.getHelpPage(helpItems, helpPage);
                        buttons = MessageBodyHelper.helpBody;
                        break;
                    case "/ping":
                        System.out.println("Pinging bot");
                        break;
                    case "/ocr":
                        System.out.println("OCR FLOW begin");
                        break;
                    case "/env":
                        System.out.println("Envelope inventory");
                        break;
                    default:
                        reply = errorEmoji+" Command not recognised, please try /help";
                        break;
                }
            }

            SendMessage message = SendMessage // Create a message object
                    .builder()
                    .chatId(chat_id)
                    .text(reply)
                    .replyMarkup(buttons)
                    .build();
            try {
                telegramClient.execute(message); // Sending our message object to user
            } catch (TelegramApiException e) {
                log.info(Arrays.toString(e.getStackTrace()));
            }
        }
        else if (update.hasCallbackQuery()) {
            // Set variables
            String call_data = update.getCallbackQuery().getData();
            long message_id = update.getCallbackQuery().getMessage().getMessageId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();

            switch (call_data){
                case ("help-next") :
                    if (HelpPaginator.getTotalPages(helpItems) > helpPage) {
                        helpPage++;
                        String helpText = HelpPaginator.getHelpPage(helpItems, helpPage);

                        EditMessageText help_next = EditMessageText.builder()
                                .chatId(chat_id)
                                .messageId(toIntExact(message_id))
                                .text(helpText)
                                .replyMarkup(MessageBodyHelper.helpBody)
                                .build();
                        try {
                            telegramClient.execute(help_next);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case ("help-prev") :
                    String helpTextP;
                    if (helpPage > 1) {
                        helpPage --;
                        helpTextP = HelpPaginator.getHelpPage(helpItems, helpPage);
                        EditMessageText help_prev = EditMessageText.builder()
                                .chatId(chat_id)
                                .messageId(toIntExact(message_id))
                                .text(helpTextP)
                                .replyMarkup(MessageBodyHelper.helpBody)
                                .build();
                        try {
                            telegramClient.execute(help_prev);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }
}
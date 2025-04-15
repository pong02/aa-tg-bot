package aa.entity;

import aa.exception.ParseError;
import aa.helper.ExcelProcessor;
import aa.helper.HelpPaginator;
import aa.helper.MessageBodyHelper;
import aa.model.Label;
import aa.repository.LabelDao;
import aa.repository.LabelDaoImpl;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.vdurmont.emoji.EmojiParser;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.toIntExact;

@Slf4j
public class Bot implements LongPollingSingleThreadUpdateConsumer {

    private AtomicBoolean expectingFile = new AtomicBoolean(false);

    private final EntityManager em;
    private final LabelDao labelDao;


    List<String> helpItems;

    Integer helpPage;

    private final TelegramClient telegramClient;

    public Bot(String botToken, EntityManager em) {
        telegramClient = new OkHttpTelegramClient(botToken);
        this.em = em;
        this.labelDao = new LabelDaoImpl(em);
    }

    @Override
    public void consume(Update update) {
        if (expectingFile.get() && update.getMessage().hasDocument()) {
            long chat_id = update.getMessage().getChatId();
            Document document = update.getMessage().getDocument();
            String fileName = document.getFileName();
            SendMessage message;
            //check file type, and handle accordingly

            if (!fileName.toLowerCase().endsWith(".xlsx")) {
                message = sendMessage(chat_id,"Only .xlsx files are supported. Please upload a valid Excel file.",null);
            }
            else {
                List<Label> labels;
                try {
                    String fileId = document.getFileId();

                    // Step 1: Resolve the actual file path from Telegram
                    GetFile getFile = new GetFile(fileId);
                    File telegramFile = telegramClient.execute(getFile);

                    // Step 2: Download file as InputStream
                    try (InputStream inputStream = telegramClient.downloadFileAsStream(telegramFile)) {
                        labels = ExcelProcessor.read(inputStream, labelDao);
                        log.info("{} lines parsed and saved to DB as pending",labels.size());
                        message = sendMessage(chat_id, "File read successfully", null);
                        expectingFile.set(false);
                    }

                } catch (ParseError e) {
                    log.error("Invalid file headers", e);
                    message = sendMessage(chat_id, "Invalid Excel file uploaded. Please try again", null);
                    expectingFile.set(true);
                } catch (Exception e) {
                    log.error("Error downloading or processing file", e);
                    message = sendMessage(chat_id, "Error processing the uploaded Excel file. Please try again", null);
                    expectingFile.set(true);
                }
            }
            try {
                telegramClient.execute(message); // Sending our message object to user
            } catch (TelegramApiException e) {
                log.info(Arrays.toString(e.getStackTrace()));
            }
        }
        // We check if the update has a message and the message has text
        else if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            String name = update.getMessage().getFrom().getFirstName();
//            Long id = update.getMessage().getFrom().getId();
            String txt = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            String reply = "ok buddy "+EmojiParser.parseToUnicode(":nerd:");
            String errorEmoji = EmojiParser.parseToUnicode(":no_entry_sign:");
            InlineKeyboardMarkup buttons = null;

            log.info("{} said {}", name, update.getMessage());
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
                    case "/dispatch":
                        expectingFile.set(true);
                        reply = "Dispatch upload prompted. Please upload an .xlsx file.";
                        break;
                    case "/confirm":
                        List<Label> confirmedOrders = labelDao.confirmPending();
                        reply = "Confirmed "+ confirmedOrders.size() +" orders";
                        break;
                    case "/cancel":
                        List<Label> deletedOrders = labelDao.deletePending();
                        reply = "Moved "+ deletedOrders.size() +" orders to the Bin";
                        break;
                    case "/ping":
                        long startTime = System.currentTimeMillis();
                        try {
                            telegramClient.execute(new GetMe()); // lightweight API call
                            long latency = System.currentTimeMillis() - startTime;

                            reply = "Bot API latency: " + latency + " ms";
                        } catch (Exception e) {
                            log.error("Ping failed", e);

                            reply = "Ping failed. Bot may be experiencing network issues.";
                        }
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
            SendMessage message = sendMessage(chat_id,reply,buttons);
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
                            log.error(Arrays.toString(e.getStackTrace()));
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
                            log.error(Arrays.toString(e.getStackTrace()));
                        }
                    }
                    break;
            }
        }
    }
    private SendMessage sendMessage(long chat_id, String reply, InlineKeyboardMarkup buttons){
        return SendMessage // Create a message object
                .builder()
                .chatId(chat_id)
                .text(reply)
                .replyMarkup(buttons)
                .build();
    }

}
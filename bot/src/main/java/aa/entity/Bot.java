package aa.entity;

import aa.dto.CallbackPayload;
import aa.exception.ParseError;
import aa.helper.*;
import aa.model.*;
import aa.repository.*;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
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
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Math.toIntExact;

@Slf4j
public class Bot implements LongPollingSingleThreadUpdateConsumer {

    private AtomicBoolean expectingFile = new AtomicBoolean(false);

    private final LabelDao labelDao;

    private final EnvelopeDao envelopeDao;

    private final StampDao stampDao;

    private final StampConfigurationDao stampConfigurationDao;

    private Map<Stamp,Integer> stampConfiguration = new HashMap<>();

    private UUID cachedId;

    private List<String> helpItems;

    private Integer helpPage;

    private final TelegramClient telegramClient;

    public Bot(TelegramClient client, EntityManager em) {
        this.telegramClient = client;
        this.labelDao = new LabelDaoImpl(em);
        this.envelopeDao = new EnvelopeDaoImpl(em);
        this.stampDao = new StampDaoImpl(em);
        this.stampConfigurationDao = new StampConfigurationDaoImpl(em);
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
            String txt = update.getMessage().getText().replace("@aa_waltuh_bot ","");
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
                    case "/envelope":
                        List<Envelope> envelopeList = envelopeDao.getAllEnvelopes();
                        reply = ListMessageFormatter.format(envelopeList);
                        buttons = MessageBodyHelper.envelopeBodyC;
                        break;
                    case "/create-envelope":
                        String text = txt.substring("/create-envelope".length()).trim();
                        String[] parts = text.split(",", 4);
                        if (parts.length == 4) {
                            String ename = parts[0].trim();
                            String description = parts[1].trim();
                            int quantity;
                            BigDecimal price;

                            try {
                                quantity = Integer.parseInt(parts[2].trim());
                                price = BigDecimal.valueOf(Double.parseDouble(parts[3].trim()));

                                Envelope env = Envelope.builder().name(ename).description(description).quantity(quantity).price(price).build();

                                envelopeDao.save(env);

                                System.out.println("Creating envelope "+env.getId());

                                reply = "Created Envelope successfully: \n"+env;
                                buttons = MessageBodyHelper.envelopeBodyC2(env.getId());

                            } catch (NumberFormatException e) {
                                reply = "❌ Quantity and price must be valid numbers.";
                            }

                        } else {
                            reply = "❌ Invalid format. Use: /submit Name, Description, Quantity, Price";
                        }
                        break;
                    case "/update-envelope":
                        String textUp = txt.substring("/update-envelope".length()).trim();
                        String[] partsUp = textUp.split(",", 5);
                        if (partsUp.length == 5) {
                            UUID id = UUID.fromString(partsUp[0].trim());
                            String ename = partsUp[1].trim();
                            String description = partsUp[2].trim();
                            int quantity;
                            BigDecimal price;

                            try {
                                quantity = Integer.parseInt(partsUp[3].trim());
                                price = BigDecimal.valueOf(Double.parseDouble(partsUp[4].trim()));

                                Envelope env = envelopeDao.findById(id).orElseThrow();
                                env.setName(ename);
                                env.setDescription(description);
                                env.setPrice(price);
                                env.setQuantity(quantity);

                                envelopeDao.save(env);

                                System.out.println("Updating envelope "+env.getId());

                                reply = "Updated Envelope successfully: \n"+env;
                                buttons = MessageBodyHelper.envelopeBodyU2(env.getId());

                            } catch (NumberFormatException e) {
                                reply = "❌ Quantity and price must be valid numbers.";
                            }

                        } else {
                            reply = "❌ Invalid format. Use: /submit Name, Description, Quantity, Price";
                        }
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
            CallbackPayload callback = CallbackPayloadUtil.fromJson(call_data);
            String callback_action = callback.getAction();
            switch (callback_action){
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
                case ("estamp") :
                    UUID env_id = callback.getId();
                    System.out.println("Stamp config for envelope: "+env_id);
                    List<Stamp> stamps = stampDao.findAllStamps();
                    //generate buttons row for each stamp found, with a + - button, each editing the message to show how many of each stamp we adding
                    stampConfiguration = stamps.stream()
                            .collect(Collectors.toMap(
                                    Function.identity(), // each Stamp as key
                                    stamp -> 0           // initial value
                            ));

                    //here cache the env id because stamp needs to be passed via payload id
                    cachedId = env_id;
                    Optional<Envelope> envelopeDisplayOptional = envelopeDao.findById(cachedId);

                    if (envelopeDisplayOptional.isEmpty()) {
                        log.error("Envelope with ID " + cachedId + " not found. Aborting stamp configuration edit.");
                        break;
                    }

                    EditMessageText edit_stamp_conf = EditMessageText.builder()
                            .chatId(chat_id)
                            .messageId(toIntExact(message_id))
                            .text("Editing stamp configuration for "+envelopeDisplayOptional.get())
                            .replyMarkup(MessageBodyHelper.envelopeBodyC3(env_id,stampConfiguration))
                            .build();

                    try {
                        telegramClient.execute(edit_stamp_conf);
                    } catch (TelegramApiException e) {
                        log.error(Arrays.toString(e.getStackTrace()));
                    }
                    break;
                case "stamp+":
                case "stamp-":
                    UUID stampId = callback.getId();

                    // Get the stamp object by ID
                    Optional<Stamp> matchingStamp = stampConfiguration.keySet().stream()
                            .filter(s -> s.getId().equals(stampId))
                            .findFirst();

                    if (matchingStamp.isPresent()) {
                        Stamp stamp = matchingStamp.get();
                        int currentQty = stampConfiguration.get(stamp);

                        // Update quantity based on action
                        if (callback_action.equals("stamp+")) {
                            stampConfiguration.put(stamp, currentQty + 1);
                        } else if (currentQty > 0) {
                            stampConfiguration.put(stamp, currentQty - 1);
                        }

                        Optional<Envelope> envelopeDisplayOptionalS = envelopeDao.findById(cachedId);

                        if (envelopeDisplayOptionalS.isEmpty()) {
                            log.error("Envelope with ID " + cachedId + " not found. Aborting stamp configuration edit.");
                            break;
                        }


                        // Update the message UI
                        EditMessageText updateQtyView = EditMessageText.builder()
                                .chatId(chat_id)
                                .messageId(toIntExact(message_id))
                                .text("Editing stamp configuration for " + envelopeDisplayOptionalS.get())
                                .replyMarkup(MessageBodyHelper.envelopeBodyC3(callback.getId(), stampConfiguration))
                                .build();

                        try {
                            telegramClient.execute(updateQtyView);
                        } catch (TelegramApiException e) {
                            log.error("Telegram API error: " + Arrays.toString(e.getStackTrace()), e);
                        }
                    } else {
                        try {
                            telegramClient.execute(SendMessage.builder()
                                    .chatId(chat_id)
                                    .text("❌ Stamp not found.")
                                    .build());
                        } catch (TelegramApiException e) {
                            log.error("Telegram API error: " + Arrays.toString(e.getStackTrace()), e);
                        }
                    }
                    break;
                case "estampz":
                    Optional<Envelope> envelopeUpdateOptional = envelopeDao.findById(cachedId);

                    if (envelopeUpdateOptional.isPresent()) {

                        Envelope envelopeUpdate = envelopeUpdateOptional.get();

                        //setup the stamp configuration here we need to ignore zero
                        List<StampCombination> combinations = stampConfiguration.entrySet().stream()
                                .filter(entry -> entry.getValue() != null && entry.getValue() > 0)
                                .map(entry -> StampCombination.builder()
                                        .stamp(entry.getKey())
                                        .quantity(entry.getValue())
                                        .build())
                                .toList();

                        StampConfiguration config = StampConfiguration.builder()
                                .name(envelopeUpdate.getName())
                                .stampCombinations(combinations)
                                .build();
                        stampConfigurationDao.save(config);

                        envelopeUpdate.setStampConfiguration(config);
                        envelopeDao.save(envelopeUpdate);

                        Optional<Envelope> envelopeDisplayOptionalZ = envelopeDao.findById(cachedId);

                        if (envelopeDisplayOptionalZ.isEmpty()) {
                            log.error("Envelope with ID " + cachedId + " not found. Aborting stamp configuration edit.");
                            break;
                        }

                        EditMessageText updateQtyView = EditMessageText.builder()
                                .chatId(chat_id)
                                .messageId(toIntExact(message_id))
                                .text("Successfully edited stamp configuration for " + envelopeDisplayOptionalZ.get())
                                .build();
                        try {
                            telegramClient.execute(updateQtyView);
                        } catch (TelegramApiException e) {
                            log.error("Telegram API error: " + Arrays.toString(e.getStackTrace()), e);
                        }

                    } else {
                        log.error("Error setting stamp configuration for envelope "+ cachedId);
                    }

                    resetEnvelopeFlow();
                    break;
                case "update-envelope":
                    resetEnvelopeFlow();
                    List<Envelope> envelopesUpdate = envelopeDao.findAll();

                    if (envelopesUpdate.isEmpty()) {
                        log.error("No envelopes to update.");
                        break;
                    }

                    EditMessageText updateEnv = EditMessageText.builder()
                            .chatId(chat_id)
                            .messageId(toIntExact(message_id))
                            .text("Update envelope:")
                            .replyMarkup(MessageBodyHelper.envelopeBodyUp(envelopesUpdate))
                            .build();

                    try {
                        telegramClient.execute(updateEnv);
                    } catch (TelegramApiException e) {
                        log.error(Arrays.toString(e.getStackTrace()));
                    }
                    break;
                case "delete-envelope":
                    resetEnvelopeFlow();
                    List<Envelope> envelopesDelete = envelopeDao.findAll();

                    if (envelopesDelete.isEmpty()) {
                        log.error("No envelopes to delete.");
                        break;
                    }

                    EditMessageText delEnv = EditMessageText.builder()
                            .chatId(chat_id)
                            .messageId(toIntExact(message_id))
                            .text("Delete envelope:")
                            .replyMarkup(MessageBodyHelper.envelopeBodyDel(envelopesDelete))
                            .build();

                    try {
                        telegramClient.execute(delEnv);
                    } catch (TelegramApiException e) {
                        log.error(Arrays.toString(e.getStackTrace()));
                    }
                    break;
                case "delenv":
                    resetEnvelopeFlow();
                    UUID del_env = callback.getId();
                    Envelope del_envelope = envelopeDao.findById(del_env).orElseThrow();
                    envelopeDao.deleteById(del_env);
                    editMessageOnCallback(update.getCallbackQuery().getMessage().getChatId(),update.getCallbackQuery().getMessage().getMessageId(),"Envelope deleted successfully :\n"+del_envelope);
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

    private void resetEnvelopeFlow(){
        log.info("Purged cache for envelopes");
        cachedId = null;
        stampConfiguration = new HashMap<>();
    }

    private void editMessageOnCallback(long chat_id, long message_id, String msg){
        EditMessageText edit = EditMessageText.builder()
                .chatId(chat_id)
                .messageId((int) message_id)
                .text(msg)
                .replyMarkup(null) // removes all buttons
                .build();

        try {
            telegramClient.execute(edit);
        } catch (TelegramApiException e) {
            log.error("Failed to confirm envelope deletion: {}", Arrays.toString(e.getStackTrace()), e);
        }
    }

}
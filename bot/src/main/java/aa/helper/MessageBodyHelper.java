package aa.helper;

import aa.dto.CallbackPayload;
import aa.model.Envelope;
import aa.model.Stamp;
import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MessageBodyHelper {

    public static final InlineKeyboardMarkup helpBody = InlineKeyboardMarkup
            .builder()
            .keyboardRow(
                    new InlineKeyboardRow(InlineKeyboardButton
                            .builder()
                            .text(EmojiParser.parseToUnicode(":arrow_left:"))
                            .callbackData(CallbackPayloadUtil.toJson(CallbackPayload.of("help-prev")))
                            .build(),
                            InlineKeyboardButton
                                    .builder()
                                    .text("Wiki "+EmojiParser.parseToUnicode(":open_book:"))
                                    .url("https://pong02.github.io/bot-help/")
                                    .build(),
                            InlineKeyboardButton
                                    .builder()
                                    .text(EmojiParser.parseToUnicode(":arrow_right:"))
                                    .callbackData(CallbackPayloadUtil.toJson(CallbackPayload.of("help-next")))
                                    .build()
                    )
            )
            .build();

    public static final InlineKeyboardMarkup menuBody = InlineKeyboardMarkup
            .builder()
            .keyboardRow(
                    new InlineKeyboardRow(InlineKeyboardButton
                            .builder()
                            .text(EmojiParser.parseToUnicode("ocr-start"))
                            .callbackData(CallbackPayloadUtil.toJson(CallbackPayload.of("?")))
                            .build()
                    )
            ).keyboardRow(
                    new InlineKeyboardRow(InlineKeyboardButton
                            .builder()
                            .text(EmojiParser.parseToUnicode("ocr-reference"))
                            .callbackData(CallbackPayloadUtil.toJson(CallbackPayload.of("?")))
                            .build()
                    )
            ).keyboardRow(
                    new InlineKeyboardRow(InlineKeyboardButton
                            .builder()
                            .text(EmojiParser.parseToUnicode("envelope"))
                            .callbackData(CallbackPayloadUtil.toJson(CallbackPayload.of("?")))
                            .build()
                    )
            ).keyboardRow(
                    new InlineKeyboardRow(InlineKeyboardButton
                            .builder()
                            .text(EmojiParser.parseToUnicode("stamp"))
                            .callbackData(CallbackPayloadUtil.toJson(CallbackPayload.of("?")))
                            .build()
                    )
            ).keyboardRow(
                    new InlineKeyboardRow(InlineKeyboardButton
                            .builder()
                            .text(EmojiParser.parseToUnicode("meta"))
                            .callbackData(CallbackPayloadUtil.toJson(CallbackPayload.of("?")))
                            .build()
                    )
            )
            .build();

    public static final InlineKeyboardMarkup envelopeBodyC = InlineKeyboardMarkup
            .builder()
            .keyboardRow(
                    new InlineKeyboardRow(InlineKeyboardButton
                            .builder()
                            .text("Create")
                            .switchInlineQueryCurrentChat("/create-envelope Name, Description, Quantity, Price")
                            .build()
                    )
            )
            .keyboardRow(
                    new InlineKeyboardRow(InlineKeyboardButton
                            .builder()
                            .text("Update")
                            .callbackData(CallbackPayloadUtil.toJson(CallbackPayload.of("update-envelope")))  // New callback identifier
                            .build()
                    )
            )
            .keyboardRow(
                    new InlineKeyboardRow(InlineKeyboardButton
                            .builder()
                            .text("Delete")
                            .callbackData(CallbackPayloadUtil.toJson(CallbackPayload.of("delete-envelope")))  // New callback identifier
                            .build()
                    )
            )
            .build();

    public static InlineKeyboardMarkup envelopeBodyC2(UUID id) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(
                        new InlineKeyboardRow(InlineKeyboardButton
                                .builder()
                                .text("Add Stamp Configurations")
                                .callbackData(CallbackPayloadUtil.toJson(CallbackPayload.of("estamp", id)))  // New callback identifier
                                .build()
                        )
                )
                .build();
    }

    public static InlineKeyboardMarkup envelopeBodyU2(UUID id) {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(
                        new InlineKeyboardRow(InlineKeyboardButton
                                .builder()
                                .text("Update Stamp Configurations")
                                .callbackData(CallbackPayloadUtil.toJson(CallbackPayload.of("estamp", id)))  // New callback identifier
                                .build()
                        )
                )
                .build();
    }

    public static InlineKeyboardMarkup envelopeBodyC3(UUID envelope_id, Map<Stamp,Integer> stampConfiguration) {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        //for each stamp generate a row with - stamp name +
        stampConfiguration.forEach((stamp, amount) -> {
            System.out.println("Stamp: " + stamp.getName() + ", Quantity: " + amount);

            InlineKeyboardButton minus = InlineKeyboardButton.builder()
                    .text("➖")
                    .callbackData(CallbackPayloadUtil.toJson(
                            CallbackPayload.of("stamp-", stamp.getId())
                    ))
                    .build();

            InlineKeyboardButton label = InlineKeyboardButton.builder()
                    .text(stamp.getName() + ": " + amount)
                    .callbackData("noop") // optional: static or for preview
                    .build();

            InlineKeyboardButton plus = InlineKeyboardButton.builder()
                    .text("➕")
                    .callbackData(CallbackPayloadUtil.toJson(
                            CallbackPayload.of("stamp+", stamp.getId())
                    ))
                    .build();

            rows.add(new InlineKeyboardRow(List.of(minus, label, plus)));
        });


        // Final row: Confirm button
        InlineKeyboardButton confirmButton = InlineKeyboardButton.builder()
                .text("✅ Confirm")
                .callbackData(CallbackPayloadUtil.toJson(CallbackPayload.of("estampz", envelope_id)))
                .build();

        rows.add(new InlineKeyboardRow(confirmButton));

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    public static InlineKeyboardMarkup envelopeBodyUp(List<Envelope> envelopes) {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        // Generate one row per envelope
        envelopes.forEach(envelope -> {
            String prefill = String.format("/update-envelope %s, %s, %s, %d, %.2f",
                    envelope.getId(),
                    envelope.getName(),
                    envelope.getDescription(),
                    envelope.getQuantity(),
                    envelope.getPrice());

            InlineKeyboardButton updateChoice = InlineKeyboardButton.builder()
                    .text("✏️ " + envelope.getName())
                    .switchInlineQueryCurrentChat(prefill)
                    .build();

            rows.add(new InlineKeyboardRow(List.of(updateChoice)));
        });

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

//    public static InlineKeyboardMarkup envelopeBodyUp(List<Envelope> envelopes) {
//        List<InlineKeyboardRow> rows = new ArrayList<>();
//        //for each item in envelope show a button row
//        envelopes.forEach(envelope -> {
//
//            InlineKeyboardButton updateChoice = InlineKeyboardButton.builder()
//                    .text(envelope.getName())
//                    .callbackData(CallbackPayloadUtil.toJson(
//                            CallbackPayload.of("upenv", envelope.getId())
//                    ))
//                    .build();
//            rows.add(new InlineKeyboardRow(List.of(updateChoice)));
//        });
//        return InlineKeyboardMarkup.builder()
//                .keyboard(rows)
//                .build();
//    }

    public static InlineKeyboardMarkup envelopeBodyDel(List<Envelope> envelopes) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        //for each item in envelope show a button row
        envelopes.forEach(envelope -> {

            InlineKeyboardButton updateChoice = InlineKeyboardButton.builder()
                    .text(envelope.getName())
                    .callbackData(CallbackPayloadUtil.toJson(
                            CallbackPayload.of("delenv", envelope.getId())
                    ))
                    .build();
            rows.add(new InlineKeyboardRow(List.of(updateChoice)));
        });
        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }
}

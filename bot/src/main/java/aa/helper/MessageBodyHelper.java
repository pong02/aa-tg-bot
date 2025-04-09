package aa.helper;

import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

public class MessageBodyHelper {

    public static final InlineKeyboardMarkup helpBody = InlineKeyboardMarkup
            .builder()
            .keyboardRow(
                    new InlineKeyboardRow(InlineKeyboardButton
                            .builder()
                            .text(EmojiParser.parseToUnicode(":arrow_left:"))
                            .callbackData("help-prev")
                            .build(),
                            InlineKeyboardButton
                                    .builder()
                                    .text("Wiki "+EmojiParser.parseToUnicode(":open_book:"))
//                                    .callbackData("redirect-wiki")
                                    .url("https://pong02.github.io/bot-help/")
                                    .build(),
                            InlineKeyboardButton
                                    .builder()
                                    .text(EmojiParser.parseToUnicode(":arrow_right:"))
                                    .callbackData("help-next")
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
                            .callbackData("?")
                            .build()
                    )
            ).keyboardRow(
                    new InlineKeyboardRow(InlineKeyboardButton
                            .builder()
                            .text(EmojiParser.parseToUnicode("ocr-reference"))
                            .callbackData("?")
                            .build()
                    )
            ).keyboardRow(
                    new InlineKeyboardRow(InlineKeyboardButton
                            .builder()
                            .text(EmojiParser.parseToUnicode("envelope"))
                            .callbackData("?")
                            .build()
                    )
            ).keyboardRow(
                    new InlineKeyboardRow(InlineKeyboardButton
                            .builder()
                            .text(EmojiParser.parseToUnicode("stamp"))
                            .callbackData("?")
                            .build()
                    )
            ).keyboardRow(
                    new InlineKeyboardRow(InlineKeyboardButton
                            .builder()
                            .text(EmojiParser.parseToUnicode("meta"))
                            .callbackData("?")
                            .build()
                    )
            )
            .build();
}

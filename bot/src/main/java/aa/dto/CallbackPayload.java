package aa.dto;

import lombok.Getter;

import java.util.UUID;

@Getter
public class CallbackPayload {
    private String action;
    private UUID id; // Optional — can be null for simple actions

    public CallbackPayload() {}

    public CallbackPayload(String action, UUID id) {
        this.action = action;
        this.id = id;
    }

    public static CallbackPayload of(String action) {
        return new CallbackPayload(action, null);
    }

    public static CallbackPayload of(String action, UUID id) {
        return new CallbackPayload(action, id);
    }

}

package aa.helper;

import aa.dto.CallbackPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class CallbackPayloadUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toJson(CallbackPayload payload) {
        try {
            return mapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Callback serialization failed", e);
        }
    }

    public static CallbackPayload fromJson(String json) {
        try {
            return mapper.readValue(json, CallbackPayload.class);
        } catch (IOException e) {
            throw new RuntimeException("Callback deserialization failed", e);
        }
    }
}

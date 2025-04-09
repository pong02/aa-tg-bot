package aa.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain=true)
public class UnrecognizedCommand extends WaltuhBaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    private String message;

    public UnrecognizedCommand(String detailedMessage){
        super(detailedMessage);
        this.message = detailedMessage;
    }

}
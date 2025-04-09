package aa.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain=true)
public class WaltuhBaseException extends Exception {

    private String statusCode;
    private String description;
    private Object payload;

    @Serial
    private static final long serialVersionUID = 0L;

    public WaltuhBaseException() {
        super();
    }

    public WaltuhBaseException(String s){
        super(s);
    }
}

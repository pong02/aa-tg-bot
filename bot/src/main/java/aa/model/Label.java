package aa.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Data
@Table(name = "label")
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDateTime entryDate;
    private LocalDateTime date;
    private String orderId;
    private String postToName;
    private String postToAddress;
    private String postToCity;
    private String postToState;
    private String postToPostalCode;
    private String customLabel;

    //meta
    private Boolean pending;
    private Boolean deleted;

}
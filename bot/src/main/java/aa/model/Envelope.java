package aa.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Data
@Table(name = "envelope")
public class Envelope {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String description;
    private Integer quantity;
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "stamp_config_id")
    private StampConfiguration stampConfiguration;

    @Override
    public String toString(){
        return "["+name+"] : "+description+
            "\nqty: "+quantity +
            "\nprice: $" +price+
            "\nstamps: "+ stampConfiguration +"\n";
    }

}
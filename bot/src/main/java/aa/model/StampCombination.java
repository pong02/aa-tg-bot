package aa.model;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "stamp_combination")
public class StampCombination {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stamp_id", nullable = false)
    private Stamp stamp;

    @Column(nullable = false)
    private Integer quantity;
}

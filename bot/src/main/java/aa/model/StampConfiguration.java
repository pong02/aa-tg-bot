package aa.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Data
@Table(name = "stamp_configuration")
public class StampConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @ElementCollection
    @CollectionTable(name = "stamp_combination", joinColumns = @JoinColumn(name = "stamp_configuration_id"))
    private List<StampCombination> stampCombinations;
}

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

    @OneToMany(mappedBy = "stampConfiguration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StampCombination> stampCombinations;
}

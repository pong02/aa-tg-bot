package aa.repository;

import aa.model.Stamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StampDao extends GenericDao<Stamp> {
    List<Stamp> findAllStamps();
    Optional<Stamp> findById(UUID id);
    int purgeAll();
}

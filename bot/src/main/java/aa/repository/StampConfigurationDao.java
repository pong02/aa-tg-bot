package aa.repository;

import aa.model.StampConfiguration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StampConfigurationDao extends GenericDao<StampConfiguration> {
    List<StampConfiguration> findAllConfigs();
    Optional<StampConfiguration> findById(UUID id);
    int purgeAll();
}

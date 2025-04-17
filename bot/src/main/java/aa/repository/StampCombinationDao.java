package aa.repository;

import aa.model.StampCombination;
import java.util.List;
import java.util.UUID;

public interface StampCombinationDao extends GenericDao<StampCombination> {
    List<StampCombination> findByConfigurationId(UUID configId);
    int purgeAll();
}

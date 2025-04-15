package aa.repository;

import aa.model.Label;
import java.util.List;
import java.util.UUID;

public interface LabelDao extends GenericDao<Label> {
    List<Label> findByOrderId(String orderId);

    List<Label> confirmPending();

    List<Label> deletePending();

    Label deleteByLabelId(UUID id);

    //cron function
    int purgeDeleted();

}
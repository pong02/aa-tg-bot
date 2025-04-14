package aa.repository;

import aa.model.Label;
import java.util.List;

public interface LabelDao extends GenericDao<Label> {
    List<Label> findByOrderId(String orderId);
}
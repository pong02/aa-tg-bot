package aa.repository;

import aa.model.Label;
import jakarta.persistence.EntityManager;

import java.util.List;

public class LabelDaoImpl extends GenericDaoImpl<Label> implements LabelDao {

    public LabelDaoImpl(EntityManager em) {
        super(em, Label.class);
    }

    @Override
    public List<Label> findByOrderId(String orderId) {
        return em.createQuery(
                        "SELECT l FROM Label l WHERE l.orderId = :orderId", Label.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }
}
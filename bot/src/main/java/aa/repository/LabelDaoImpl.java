package aa.repository;

import aa.model.Label;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.UUID;

public class LabelDaoImpl extends GenericDaoImpl<Label> implements LabelDao {

    public LabelDaoImpl(EntityManager em) {
        super(em, Label.class);
    }

    @Override
    public List<Label> findByOrderId(String orderId) {
        return em.createQuery(
                        "SELECT l FROM Label l WHERE l.orderId = :orderId and l.deleted = false", Label.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    @Override
    public List<Label> confirmPending() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            List<Label> pendingLabels = em.createQuery(
                            "SELECT l FROM Label l WHERE l.pending = true AND l.deleted = false", Label.class)
                    .getResultList();

            for (Label label : pendingLabels) {
                label.setPending(false);
            }

            em.flush(); // Persist changes
            tx.commit();

            return pendingLabels;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    @Override
    public List<Label> deletePending() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            List<Label> pendingLabels = em.createQuery(
                            "SELECT l FROM Label l WHERE l.pending = true AND l.deleted = false", Label.class)
                    .getResultList();

            for (Label label : pendingLabels) {
                label.setDeleted(true);
            }

            em.flush(); // Persist changes
            tx.commit();

            return pendingLabels;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    @Override
    public Label deleteByLabelId(UUID id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Label label = em.createQuery(
                            "SELECT l FROM Label l WHERE l.id = :id", Label.class)
                    .setParameter("id", id)
                    .getSingleResult();

            label.setDeleted(true);

            em.flush(); // Persist change
            tx.commit();

            return label;
        } catch (NoResultException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            return null; // Or throw custom exception
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    @Override
    public int purgeDeleted() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            int deleted = em.createQuery("DELETE FROM Label l WHERE l.deleted = true")
                    .executeUpdate();
            tx.commit();
            return deleted;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }
}
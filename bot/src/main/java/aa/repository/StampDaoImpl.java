package aa.repository;

import aa.model.Stamp;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StampDaoImpl extends GenericDaoImpl<Stamp> implements StampDao {

    public StampDaoImpl(EntityManager em) {
        super(em, Stamp.class);
    }

    @Override
    public List<Stamp> findAllStamps() {
        return em.createQuery("SELECT s FROM Stamp s", Stamp.class).getResultList();
    }

    @Override
    public Optional<Stamp> findById(UUID id) {
        try {
            return Optional.ofNullable(em.createQuery("SELECT s FROM Stamp s WHERE s.id = :id", Stamp.class)
                    .setParameter("id", id)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public int purgeAll() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            int deleted = em.createQuery("DELETE FROM Stamp").executeUpdate();
            tx.commit();
            return deleted;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
}

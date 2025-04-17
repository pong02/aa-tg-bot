package aa.repository;

import aa.model.Envelope;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EnvelopeDaoImpl extends GenericDaoImpl<Envelope> implements EnvelopeDao {

    public EnvelopeDaoImpl(EntityManager em) {
        super(em, Envelope.class);
    }

    @Override
    public List<Envelope> getAllEnvelopes() {
        return em.createQuery("SELECT e FROM Envelope e", Envelope.class)
                .getResultList();
    }

    @Override
    public Optional<Envelope> findById(UUID id) {
        try {
            return Optional.ofNullable(em.createQuery("SELECT e FROM Envelope e WHERE e.id = :id", Envelope.class)
                    .setParameter("id", id)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Envelope deleteById(UUID id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Envelope envelope = em.find(Envelope.class, id);
            if (envelope != null) {
                em.remove(envelope);
            }

            tx.commit();
            return envelope;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    @Override
    public int purgeAll() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            int deleted = em.createQuery("DELETE FROM Envelope").executeUpdate();
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

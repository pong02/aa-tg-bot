package aa.repository;

import aa.model.StampConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StampConfigurationDaoImpl extends GenericDaoImpl<StampConfiguration> implements StampConfigurationDao {

    public StampConfigurationDaoImpl(EntityManager em) {
        super(em, StampConfiguration.class);
    }

    @Override
    public List<StampConfiguration> findAllConfigs() {
        return em.createQuery("SELECT sc FROM StampConfiguration sc", StampConfiguration.class).getResultList();
    }

    @Override
    public Optional<StampConfiguration> findById(UUID id) {
        try {
            return Optional.ofNullable(em.createQuery("SELECT sc FROM StampConfiguration sc WHERE sc.id = :id", StampConfiguration.class)
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
            int deleted = em.createQuery("DELETE FROM StampConfiguration").executeUpdate();
            tx.commit();
            return deleted;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
}

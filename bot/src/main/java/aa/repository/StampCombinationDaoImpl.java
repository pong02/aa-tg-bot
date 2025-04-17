package aa.repository;

import aa.model.StampCombination;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.UUID;

public class StampCombinationDaoImpl extends GenericDaoImpl<StampCombination> implements StampCombinationDao {

    public StampCombinationDaoImpl(EntityManager em) {
        super(em, StampCombination.class);
    }

    @Override
    public List<StampCombination> findByConfigurationId(UUID configId) {
        return em.createQuery(
                        "SELECT sc FROM StampCombination sc WHERE sc.stampConfiguration.id = :configId", StampCombination.class)
                .setParameter("configId", configId)
                .getResultList();
    }

    @Override
    public int purgeAll() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            int deleted = em.createQuery("DELETE FROM StampCombination").executeUpdate();
            tx.commit();
            return deleted;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
}



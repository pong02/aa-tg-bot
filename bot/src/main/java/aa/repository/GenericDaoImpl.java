package aa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class GenericDaoImpl<T> implements GenericDao<T> {

    protected final EntityManager em;
    private final Class<T> entityClass;

    public GenericDaoImpl(EntityManager em, Class<T> entityClass) {
        this.em = em;
        this.entityClass = entityClass;
    }

    @Override
    public void save(T entity) {
        executeInTransaction(() -> em.persist(entity));
    }

    @Override
    public void update(T entity) {
        executeInTransaction(() -> em.merge(entity));
    }

    @Override
    public void delete(T entity) {
        executeInTransaction(() -> em.remove(em.contains(entity) ? entity : em.merge(entity)));
    }

    @Override
    public Optional<T> findById(UUID id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    @Override
    public List<T> findAll() {
        String ql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        return em.createQuery(ql, entityClass).getResultList();
    }

    protected void executeInTransaction(Runnable action) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            action.run();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Transaction failed", e);
        }
    }
}
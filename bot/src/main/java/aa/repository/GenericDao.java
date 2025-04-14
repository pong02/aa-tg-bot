package aa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GenericDao<T> {
    void save(T entity);
    void update(T entity);
    void delete(T entity);
    Optional<T> findById(UUID id);
    List<T> findAll();
}
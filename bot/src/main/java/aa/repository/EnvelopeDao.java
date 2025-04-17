package aa.repository;

import aa.model.Envelope;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnvelopeDao extends GenericDao<Envelope> {

    List<Envelope> getAllEnvelopes();

    Optional<Envelope> findById(UUID id);

    Envelope deleteById(UUID id);

    int purgeAll();
}

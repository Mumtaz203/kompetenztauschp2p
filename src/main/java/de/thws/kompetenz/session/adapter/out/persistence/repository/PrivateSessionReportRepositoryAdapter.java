package de.thws.kompetenz.session.adapter.out.persistence.repository;

import de.thws.kompetenz.session.adapter.out.persistence.mapper.PrivateSessionReportPersistenceMapper;
import de.thws.kompetenz.session.application.port.out.PrivateSessionReportRepositoryPort;
import de.thws.kompetenz.session.domain.PrivateSessionReport;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PrivateSessionReportRepositoryAdapter implements PrivateSessionReportRepositoryPort {

    private final PrivateSessionReportJpaRepository repository;
    private final PrivateSessionReportPersistenceMapper mapper;

    public PrivateSessionReportRepositoryAdapter(
            PrivateSessionReportJpaRepository repository,
            PrivateSessionReportPersistenceMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PrivateSessionReport save(PrivateSessionReport report) {
        var entity = mapper.toEntity(report);
        repository.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public List<PrivateSessionReport> findAll() {
        return repository.find("order by createdAt desc")
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<PrivateSessionReport> findBySessionId(UUID sessionId) {
        return repository.findBySessionId(sessionId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countByReportedUserId(UUID reportedUserId) {
        return repository.countByReportedUserId(reportedUserId);
    }
}

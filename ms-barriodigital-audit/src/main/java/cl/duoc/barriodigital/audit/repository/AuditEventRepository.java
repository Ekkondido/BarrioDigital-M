package cl.duoc.barriodigital.audit.repository;

import cl.duoc.barriodigital.audit.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findByRequestIdOrderByTimestampDesc(Long requestId);
    List<AuditEvent> findByTrackingNumberOrderByTimestampDesc(String trackingNumber);
    List<AuditEvent> findByActorContainingIgnoreCaseOrderByTimestampDesc(String actor);
    List<AuditEvent> findAllByOrderByTimestampDesc();
}

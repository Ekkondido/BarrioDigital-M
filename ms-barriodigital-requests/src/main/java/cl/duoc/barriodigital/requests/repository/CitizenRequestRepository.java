package cl.duoc.barriodigital.requests.repository;

import cl.duoc.barriodigital.requests.model.CitizenRequest;
import cl.duoc.barriodigital.requests.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CitizenRequestRepository extends JpaRepository<CitizenRequest, Long> {
    Optional<CitizenRequest> findByTrackingNumber(String trackingNumber);
    List<CitizenRequest> findByCitizenEmail(String citizenEmail);
    List<CitizenRequest> findByStatus(RequestStatus status);
    List<CitizenRequest> findByStatusAndCitizenEmail(RequestStatus status, String citizenEmail);
}

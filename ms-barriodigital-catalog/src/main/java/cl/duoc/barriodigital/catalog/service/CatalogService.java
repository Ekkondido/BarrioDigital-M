package cl.duoc.barriodigital.catalog.service;

import cl.duoc.barriodigital.catalog.model.ProcedureType;
import cl.duoc.barriodigital.catalog.repository.ProcedureTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CatalogService {

    private final ProcedureTypeRepository procedureTypeRepository;

    public CatalogService(ProcedureTypeRepository procedureTypeRepository) {
        this.procedureTypeRepository = procedureTypeRepository;
    }

    public List<ProcedureType> getAllProcedures() {
        return procedureTypeRepository.findAll();
    }

    public Optional<ProcedureType> getById(Long id) {
        return procedureTypeRepository.findById(id);
    }

    public Optional<ProcedureType> getByCode(String code) {
        return procedureTypeRepository.findByCode(code);
    }

    @Transactional
    public ProcedureType create(ProcedureType procedure) {
        if (procedure.getAvailableQuota() == null) {
            procedure.setAvailableQuota(procedure.getDailyQuota());
        }
        return procedureTypeRepository.save(procedure);
    }

    @Transactional
    public Optional<ProcedureType> update(Long id, ProcedureType updated) {
        return procedureTypeRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setDescription(updated.getDescription());
            existing.setDailyQuota(updated.getDailyQuota());
            if (updated.getAvailableQuota() != null) {
                existing.setAvailableQuota(updated.getAvailableQuota());
            }
            existing.setRequirements(updated.getRequirements());
            if (updated.getActive() != null) {
                existing.setActive(updated.getActive());
            }
            return procedureTypeRepository.save(existing);
        });
    }

    /**
     * Disminuye el cupo diario disponible al admitir un trámite.
     * Regla clave del caso: "El cupo diario disminuye al admitir el trámite"
     */
    @Transactional
    public synchronized boolean decrementQuota(Long id) {
        Optional<ProcedureType> optional = procedureTypeRepository.findById(id);
        if (optional.isPresent()) {
            ProcedureType p = optional.get();
            if (p.getAvailableQuota() > 0) {
                p.setAvailableQuota(p.getAvailableQuota() - 1);
                procedureTypeRepository.save(p);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void resetDailyQuotas() {
        List<ProcedureType> all = procedureTypeRepository.findAll();
        for (ProcedureType p : all) {
            p.setAvailableQuota(p.getDailyQuota());
        }
        procedureTypeRepository.saveAll(all);
    }
}

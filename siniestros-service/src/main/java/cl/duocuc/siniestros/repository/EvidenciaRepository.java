package cl.duocuc.siniestros.repository;

import cl.duocuc.siniestros.model.Evidencia;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EvidenciaRepository extends MongoRepository<Evidencia, String> {

    List<Evidencia> findBySiniestroIdOrderByIdDesc(String siniestroId);

    long countBySiniestroId(String siniestroId);
}

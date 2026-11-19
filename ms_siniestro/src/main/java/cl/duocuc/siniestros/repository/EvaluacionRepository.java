package cl.duocuc.siniestros.repository;

import cl.duocuc.siniestros.model.Evaluacion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EvaluacionRepository extends MongoRepository<Evaluacion, String> {

    Optional<Evaluacion> findBySiniestroId(String siniestroId);
}

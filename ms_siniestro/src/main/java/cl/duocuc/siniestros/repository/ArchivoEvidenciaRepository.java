package cl.duocuc.siniestros.repository;

import cl.duocuc.siniestros.model.ArchivoEvidencia;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ArchivoEvidenciaRepository extends MongoRepository<ArchivoEvidencia, String> {
}

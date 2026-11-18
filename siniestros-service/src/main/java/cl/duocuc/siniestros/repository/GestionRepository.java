package cl.duocuc.siniestros.repository;

import cl.duocuc.siniestros.model.Gestion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GestionRepository extends MongoRepository<Gestion, String> {

    List<Gestion> findBySiniestroIdOrderByIdAsc(String siniestroId);
}

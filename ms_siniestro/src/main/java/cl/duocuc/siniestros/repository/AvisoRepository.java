package cl.duocuc.siniestros.repository;

import cl.duocuc.siniestros.model.Aviso;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AvisoRepository extends MongoRepository<Aviso, String> {

    List<Aviso> findByDestinatarioIdOrderByIdDesc(String destinatarioId);

    long countByDestinatarioIdAndLeidoFalse(String destinatarioId);
}

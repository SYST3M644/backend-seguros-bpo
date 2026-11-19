package cl.duocuc.siniestros.repository;

import cl.duocuc.siniestros.model.Solicitud;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SolicitudRepository extends MongoRepository<Solicitud, String> {

    List<Solicitud> findBySiniestroIdOrderByIdDesc(String siniestroId);
}

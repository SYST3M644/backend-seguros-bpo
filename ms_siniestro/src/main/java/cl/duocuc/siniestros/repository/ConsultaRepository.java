package cl.duocuc.siniestros.repository;

import cl.duocuc.siniestros.model.Consulta;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ConsultaRepository extends MongoRepository<Consulta, String> {

    List<Consulta> findBySiniestroIdOrderByIdDesc(String siniestroId);
}

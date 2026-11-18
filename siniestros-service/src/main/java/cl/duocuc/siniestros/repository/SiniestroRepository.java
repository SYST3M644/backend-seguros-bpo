package cl.duocuc.siniestros.repository;

import cl.duocuc.siniestros.model.Siniestro;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SiniestroRepository extends MongoRepository<Siniestro, String> {

    List<Siniestro> findByAseguradoIdOrderByIdDesc(String aseguradoId);

    List<Siniestro> findByLiquidadorIdOrderByIdDesc(String liquidadorId);

    List<Siniestro> findByLiquidadorIdIsNullAndEtapaOrderByIdAsc(String etapa);

    boolean existsByNumero(String numero);
}

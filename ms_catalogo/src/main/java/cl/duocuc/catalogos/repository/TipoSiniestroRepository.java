package cl.duocuc.catalogos.repository;

import cl.duocuc.catalogos.model.TipoSiniestro;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TipoSiniestroRepository extends MongoRepository<TipoSiniestro, String> {

    Optional<TipoSiniestro> findByCodigo(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);
}

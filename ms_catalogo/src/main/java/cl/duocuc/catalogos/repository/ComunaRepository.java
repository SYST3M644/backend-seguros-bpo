package cl.duocuc.catalogos.repository;

import cl.duocuc.catalogos.model.Comuna;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ComunaRepository extends MongoRepository<Comuna, String> {

    List<Comuna> findByRegionCodigoOrderByNombreAsc(String regionCodigo);
}

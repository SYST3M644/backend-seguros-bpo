package cl.duocuc.catalogos.repository;

import cl.duocuc.catalogos.model.Compania;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CompaniaRepository extends MongoRepository<Compania, String> {

    Optional<Compania> findByCodigo(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);
}

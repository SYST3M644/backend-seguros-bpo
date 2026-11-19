package cl.duocuc.catalogos.repository;

import cl.duocuc.catalogos.model.Region;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends MongoRepository<Region, String> {

    List<Region> findAllByOrderByOrdenAsc();

    Optional<Region> findByCodigo(String codigo);
}

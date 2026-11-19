package cl.duocuc.roles.repository;

import cl.duocuc.roles.model.Rol;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RolRepository extends MongoRepository<Rol, String> {
}

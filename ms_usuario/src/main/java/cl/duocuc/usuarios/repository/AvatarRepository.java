package cl.duocuc.usuarios.repository;

import cl.duocuc.usuarios.model.Avatar;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AvatarRepository extends MongoRepository<Avatar, String> {
}

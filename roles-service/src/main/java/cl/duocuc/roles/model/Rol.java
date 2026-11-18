package cl.duocuc.roles.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

@Document(collection = "roles")
@Data
public class Rol {

    @Id
    private String id;

    @Field("nombre")
    private String nombre;
}

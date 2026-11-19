package cl.duocuc.usuarios.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

@Document(collection = "avatares")
@Data
public class Avatar {

    @Id
    private String id;

    @Field("contenido")
    private byte[] contenido;
}

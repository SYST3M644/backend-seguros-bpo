package cl.duocuc.catalogos.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

@Document(collection = "comunas")
@Data
public class Comuna {

    @Id
    private String id;

    @Field("region_codigo")
    private String regionCodigo;

    @Field("nombre")
    private String nombre;
}

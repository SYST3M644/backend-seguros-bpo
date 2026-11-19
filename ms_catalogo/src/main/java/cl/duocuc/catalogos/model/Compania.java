package cl.duocuc.catalogos.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

@Document(collection = "companias")
@Data
public class Compania {

    @Id
    private String id;

    @Field("codigo")
    private String codigo;

    @Field("nombre")
    private String nombre;

    @Field("activa")
    private boolean activa;

    @Field("plazo_dias_habiles")
    private Integer plazoDiasHabiles;
}

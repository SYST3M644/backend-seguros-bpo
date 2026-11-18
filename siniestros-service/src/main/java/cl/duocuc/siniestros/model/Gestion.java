package cl.duocuc.siniestros.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

@Document(collection = "gestiones")
@Data
public class Gestion {

    @Id
    private String id;

    @Field("siniestro_id")
    private String siniestroId;

    @Field("fecha")
    private String fecha;

    @Field("responsable")
    private String responsable;

    @Field("descripcion")
    private String descripcion;

    @Field("visible_asegurado")
    private boolean visibleAsegurado;
}

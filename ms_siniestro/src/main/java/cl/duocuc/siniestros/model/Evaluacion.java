package cl.duocuc.siniestros.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

@Document(collection = "evaluaciones")
@Data
public class Evaluacion {

    @Id
    private String id;

    @Field("siniestro_id")
    private String siniestroId;

    @Field("nota")
    private Integer nota;

    @Field("comentario")
    private String comentario;

    @Field("fecha")
    private String fecha;
}

package cl.duocuc.siniestros.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

@Document(collection = "consultas")
@Data
public class Consulta {

    @Id
    private String id;

    @Field("siniestro_id")
    private String siniestroId;

    @Field("pregunta")
    private String pregunta;

    @Field("respuesta")
    private String respuesta;

    @Field("fecha_pregunta")
    private String fechaPregunta;

    @Field("fecha_respuesta")
    private String fechaRespuesta;
}

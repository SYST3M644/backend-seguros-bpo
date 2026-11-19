package cl.duocuc.siniestros.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

@Document(collection = "avisos")
@Data
public class Aviso {

    @Id
    private String id;

    @Field("siniestro_id")
    private String siniestroId;

    @Field("numero")
    private String numero;

    @Field("destinatario_id")
    private String destinatarioId;

    @Field("mensaje")
    private String mensaje;

    @Field("fecha")
    private String fecha;

    @Field("leido")
    private boolean leido;
}

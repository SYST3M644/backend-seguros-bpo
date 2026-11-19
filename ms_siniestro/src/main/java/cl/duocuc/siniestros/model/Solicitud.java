package cl.duocuc.siniestros.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

@Document(collection = "solicitudes")
@Data
public class Solicitud {

    @Id
    private String id;

    @Field("siniestro_id")
    private String siniestroId;

    @Field("tipo_documento")
    private String tipoDocumento;

    @Field("comentario")
    private String comentario;

    @Field("solicitado_por")
    private String solicitadoPor;

    @Field("fecha")
    private String fecha;

    @Field("estado")
    private String estado;
}

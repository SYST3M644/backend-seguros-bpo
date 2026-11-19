package cl.duocuc.siniestros.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

@Document(collection = "evidencias")
@Data
public class Evidencia {

    @Id
    private String id;

    @Field("siniestro_id")
    private String siniestroId;

    @Field("tipo_documento")
    private String tipoDocumento;

    @Field("nombre_archivo")
    private String nombreArchivo;

    @Field("mime")
    private String mime;

    @Field("tamano_bytes")
    private Integer tamanoBytes;

    @Field("fecha_registro")
    private String fechaRegistro;

    @Field("subido_por")
    private String subidoPor;
}

package cl.duocuc.siniestros.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

@Document(collection = "archivos_evidencia")
@Data
public class ArchivoEvidencia {

    @Id
    private String id;

    @Field("contenido")
    private byte[] contenido;
}

package cl.duocuc.catalogos.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

@Document(collection = "tipos_siniestro")
@Data
public class TipoSiniestro {

    @Id
    private String id;

    @Field("codigo")
    private String codigo;

    @Field("nombre")
    private String nombre;

    @Field("es_vehiculo")
    private boolean esVehiculo;

    @Field("documentos")
    private String documentos;
}

package cl.duocuc.siniestros.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

@Document(collection = "siniestros")
@Data
public class Siniestro {

    @Id
    private String id;

    @Field("numero")
    private String numero;

    @Field("asegurado_id")
    private String aseguradoId;

    @Field("compania_codigo")
    private String companiaCodigo;

    @Field("tipo_codigo")
    private String tipoCodigo;

    @Field("fecha_ocurrencia")
    private String fechaOcurrencia;

    @Field("fecha_reporte")
    private String fechaReporte;

    @Field("comuna")
    private String comuna;

    @Field("descripcion")
    private String descripcion;

    @Field("hubo_terceros")
    private boolean huboTerceros;

    @Field("etapa")
    private String etapa;

    @Field("liquidador_id")
    private String liquidadorId;

    @Field("resultado_cierre")
    private String resultadoCierre;

    @Field("mensaje_cierre")
    private String mensajeCierre;

    @Field("ultima_actualizacion")
    private String ultimaActualizacion;
}

package cl.duocuc.usuarios.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

@Document(collection = "usuarios")
@Data
public class Usuario {

    @Id
    private String id;

    @Field("nombre")
    private String nombre;

    @Field("correo")
    private String correo;

    @Field("contrasena")
    private String contrasena;

    @Field("rut")
    private String rut;

    @Field("equipo")
    private String equipo;

    @Field("rol_id")
    private String rolId;

    @Field("avatar")
    private String avatar;
}

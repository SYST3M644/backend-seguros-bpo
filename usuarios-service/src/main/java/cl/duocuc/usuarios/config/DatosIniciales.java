package cl.duocuc.usuarios.config;

import cl.duocuc.usuarios.model.Usuario;
import cl.duocuc.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private static final String ROL_ASEGURADO = "1";
    private static final String ROL_LIQUIDADOR = "2";
    private static final String ROL_ANALISTA = "3";

    private final UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            usuarioRepository.saveAll(List.of(
                    asegurado("Camila Rojas Fuentes", "crojas@correo.cl", "15.842.371-5"),
                    asegurado("Jorge Pizarro Contreras", "jpizarro@correo.cl", "9.418.552-1"),
                    asegurado("Valentina Soto Munoz", "vsoto@correo.cl", "19.205.884-8"),
                    asegurado("Hector Araya Diaz", "haraya@correo.cl", "7.836.219-7"),
                    asegurado("Daniela Vergara Lagos", "dvergara@correo.cl", "17.552.093-7"),
                    asegurado("Ricardo Espinoza Tapia", "respinoza@correo.cl", "13.664.708-3"),
                    asegurado("Francisca Morales Henriquez", "fmorales@correo.cl", "20.118.465-7"),
                    asegurado("Luis Carrasco Pena", "lcarrasco@correo.cl", "11.290.337-2"),
                    liquidador("Maria P. Lagos", "mlagos@segurosbpo.cl", "VEHICULOS"),
                    liquidador("Esteban R. Fuentes", "efuentes@segurosbpo.cl", "VEHICULOS"),
                    liquidador("Pedro C. Rivas", "privas@segurosbpo.cl", "HOGAR"),
                    liquidador("Carolina M. Soto", "csoto@segurosbpo.cl", "HOGAR"),
                    analista("Paulina G. Herrera", "asignaciones@segurosbpo.cl")
            ));
        }
    }

    private Usuario asegurado(String nombre, String correo, String rut) {
        return construir(nombre, correo, rut, null, ROL_ASEGURADO);
    }

    private Usuario liquidador(String nombre, String correo, String equipo) {
        return construir(nombre, correo, "", equipo, ROL_LIQUIDADOR);
    }

    private Usuario analista(String nombre, String correo) {
        return construir(nombre, correo, "", null, ROL_ANALISTA);
    }

    private Usuario construir(String nombre, String correo, String rut, String equipo, String rolId) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setContrasena("123456");
        usuario.setRut(rut);
        usuario.setEquipo(equipo);
        usuario.setRolId(rolId);
        return usuario;
    }
}

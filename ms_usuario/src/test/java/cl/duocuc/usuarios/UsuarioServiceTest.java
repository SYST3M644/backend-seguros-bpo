package cl.duocuc.usuarios;

import cl.duocuc.usuarios.client.RolClient;
import cl.duocuc.usuarios.dto.LoginRequest;
import cl.duocuc.usuarios.dto.RolDto;
import cl.duocuc.usuarios.dto.UsuarioResponse;
import cl.duocuc.usuarios.model.Usuario;
import cl.duocuc.usuarios.repository.UsuarioRepository;
import cl.duocuc.usuarios.service.AlmacenamientoService;
import cl.duocuc.usuarios.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioServiceTest {

    private UsuarioRepository repositorio;
    private RolClient rolClient;
    private UsuarioService servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(UsuarioRepository.class);
        rolClient = mock(RolClient.class);
        servicio = new UsuarioService(repositorio, rolClient, mock(AlmacenamientoService.class));
        when(rolClient.obtener(anyString())).thenReturn(new RolDto("1", "Asegurado"));
    }

    private Usuario camila() {
        Usuario camila = new Usuario();
        camila.setId("1");
        camila.setNombre("Camila Rojas Fuentes");
        camila.setCorreo("crojas@correo.cl");
        camila.setContrasena("123456");
        camila.setRut("15.842.371-5");
        camila.setRolId("1");
        return camila;
    }

    @Test
    void iniciarSesion_conCredencialesCorrectas_devuelveElUsuario() {
        when(repositorio.findByCorreo("crojas@correo.cl")).thenReturn(Optional.of(camila()));

        UsuarioResponse usuario = servicio.iniciarSesion(
                new LoginRequest("crojas@correo.cl", "123456"));

        assertEquals("Camila Rojas Fuentes", usuario.nombre());
        assertEquals("15.842.371-5", usuario.rut());
    }

    @Test
    void iniciarSesion_resuelveElNombreDelRolConElOtroServicio() {
        when(repositorio.findByCorreo(anyString())).thenReturn(Optional.of(camila()));
        when(rolClient.obtener("1")).thenReturn(new RolDto("1", "Asegurado"));

        UsuarioResponse usuario = servicio.iniciarSesion(
                new LoginRequest("crojas@correo.cl", "123456"));

        assertEquals("Asegurado", usuario.rolNombre());
    }

    @Test
    void iniciarSesion_conContrasenaIncorrecta_lanza401() {
        when(repositorio.findByCorreo(anyString())).thenReturn(Optional.of(camila()));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.iniciarSesion(new LoginRequest("crojas@correo.cl", "otra")));

        assertEquals(401, error.getStatusCode().value());
    }

    @Test
    void iniciarSesion_conCorreoQueNoExiste_lanza401() {
        when(repositorio.findByCorreo(anyString())).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.iniciarSesion(new LoginRequest("nadie@correo.cl", "123456")));

        assertEquals(401, error.getStatusCode().value());
    }

    @Test
    void listar_nuncaDevuelveLaContrasena() {
        when(repositorio.findAllByOrderByNombreAsc()).thenReturn(List.of(camila()));
        when(rolClient.listar()).thenReturn(List.of(new RolDto("1", "Asegurado")));

        List<UsuarioResponse> usuarios = servicio.listar(null);

        assertEquals(1, usuarios.size());
        assertTrue(usuarios.get(0).toString().contains("crojas@correo.cl"));
        assertTrue(!usuarios.get(0).toString().contains("123456"));
    }
}

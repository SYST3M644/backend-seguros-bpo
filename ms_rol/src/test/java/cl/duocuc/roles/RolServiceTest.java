package cl.duocuc.roles;

import cl.duocuc.roles.model.Rol;
import cl.duocuc.roles.repository.RolRepository;
import cl.duocuc.roles.service.RolService;
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

class RolServiceTest {

    private RolRepository repositorio;
    private RolService servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(RolRepository.class);
        servicio = new RolService(repositorio);
    }

    @Test
    void listar_devuelveLosTresRolesDelCaso() {
        when(repositorio.findAll()).thenReturn(List.of(
                rol("1", "Asegurado"),
                rol("2", "Liquidador"),
                rol("3", "Analista")));

        List<Rol> roles = servicio.listar();

        assertEquals(3, roles.size());
        assertEquals("Asegurado", roles.get(0).getNombre());
    }

    @Test
    void listar_sinRoles_devuelveListaVacia() {
        when(repositorio.findAll()).thenReturn(List.of());

        assertTrue(servicio.listar().isEmpty());
    }

    @Test
    void obtener_conIdExistente_devuelveElRol() {
        when(repositorio.findById("2")).thenReturn(
                Optional.of(rol("2", "Liquidador")));

        Rol rol = servicio.obtener("2");

        assertEquals("Liquidador", rol.getNombre());
    }

    @Test
    void obtener_conIdQueNoExiste_lanza404() {
        when(repositorio.findById(anyString())).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.obtener("99"));

        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void obtener_elMensajeDeErrorExplicaElProblema() {
        when(repositorio.findById(anyString())).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.obtener("50"));

        assertTrue(error.getReason().contains("Rol no encontrado"));
    }

    private Rol rol(String id, String nombre) {
        Rol rol = new Rol();
        rol.setId(id);
        rol.setNombre(nombre);
        return rol;
    }
}

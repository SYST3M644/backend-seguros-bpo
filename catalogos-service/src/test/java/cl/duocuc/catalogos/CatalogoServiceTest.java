package cl.duocuc.catalogos;

import cl.duocuc.catalogos.dto.CompaniaRequest;
import cl.duocuc.catalogos.dto.TipoSiniestroRequest;
import cl.duocuc.catalogos.dto.TipoSiniestroResponse;
import cl.duocuc.catalogos.model.Compania;
import cl.duocuc.catalogos.repository.CompaniaRepository;
import cl.duocuc.catalogos.repository.TipoSiniestroRepository;
import cl.duocuc.catalogos.service.CatalogoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatalogoServiceTest {

    private CompaniaRepository companias;
    private TipoSiniestroRepository tipos;
    private CatalogoService servicio;

    @BeforeEach
    void preparar() {
        companias = mock(CompaniaRepository.class);
        tipos = mock(TipoSiniestroRepository.class);
        servicio = new CatalogoService(companias, tipos);
        when(companias.save(any(Compania.class)))
                .thenAnswer(llamada -> llamada.getArgument(0));
    }

    @Test
    void crearCompania_generaElCodigoDesdeElNombre() {
        when(companias.existsByCodigoIgnoreCase(anyString())).thenReturn(false);

        Compania creada = servicio.crearCompania(
                new CompaniaRequest("Seguros Cordillera", 10, true));

        assertEquals("SEGUROS_CORDILLERA", creada.getCodigo());
        assertEquals("Seguros Cordillera", creada.getNombre());
    }

    @Test
    void crearCompania_quitaLasTildesDelCodigo() {
        when(companias.existsByCodigoIgnoreCase(anyString())).thenReturn(false);

        Compania creada = servicio.crearCompania(
                new CompaniaRequest("Aseguradora Patagonia Sur", 12, true));

        assertEquals("ASEGURADORA_PATAGONIA_SUR", creada.getCodigo());
    }

    @Test
    void crearCompania_conPlazoFueraDeRango_lanza400() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.crearCompania(new CompaniaRequest("Seguros Litoral", 99, true)));

        assertEquals(400, error.getStatusCode().value());
        assertTrue(error.getReason().contains("plazo"));
    }

    @Test
    void crearCompania_conNombreRepetido_lanza409() {
        when(companias.existsByCodigoIgnoreCase("SEGUROS_LITORAL")).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.crearCompania(new CompaniaRequest("Seguros Litoral", 10, true)));

        assertEquals(409, error.getStatusCode().value());
    }

    @Test
    void crearTipo_guardaLosDocumentosSeparadosPorBarra() {
        when(tipos.existsByCodigoIgnoreCase(anyString())).thenReturn(false);
        when(tipos.save(any())).thenAnswer(llamada -> llamada.getArgument(0));

        TipoSiniestroResponse creado = servicio.crearTipo(new TipoSiniestroRequest(
                "Hogar rotura de caneria", false,
                List.of("Fotografias", "  ", "Boleta del gasfiter")));

        assertEquals(2, creado.documentos().size());
        assertEquals("Fotografias", creado.documentos().get(0));
    }
}

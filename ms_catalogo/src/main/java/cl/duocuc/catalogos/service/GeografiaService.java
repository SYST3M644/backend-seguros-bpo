package cl.duocuc.catalogos.service;

import cl.duocuc.catalogos.model.Comuna;
import cl.duocuc.catalogos.model.Region;
import cl.duocuc.catalogos.repository.ComunaRepository;
import cl.duocuc.catalogos.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GeografiaService {

    private final RegionRepository regionRepository;
    private final ComunaRepository comunaRepository;

    public List<Region> listarRegiones() {
        return regionRepository.findAllByOrderByOrdenAsc();
    }

    public List<Comuna> listarComunas(String regionCodigo) {
        regionRepository.findByCodigo(regionCodigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La región no existe"));
        return comunaRepository.findByRegionCodigoOrderByNombreAsc(regionCodigo);
    }
}

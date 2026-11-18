package cl.duocuc.siniestros.client;

import cl.duocuc.siniestros.dto.CompaniaDto;
import cl.duocuc.siniestros.dto.TipoSiniestroDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
public class CatalogoClient {

    private final WebClient webClient;

    public CatalogoClient(@Value("${catalogos.service.url}") String catalogosUrl) {
        this.webClient = WebClient.builder().baseUrl(catalogosUrl).build();
    }

    public CompaniaDto obtenerCompania(String codigo) {
        try {
            return webClient.get()
                    .uri("/api/companias/{codigo}", codigo)
                    .retrieve()
                    .bodyToMono(CompaniaDto.class)
                    .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                    .block();
        } catch (WebClientRequestException e) {
            throw noDisponible();
        }
    }

    public TipoSiniestroDto obtenerTipo(String codigo) {
        try {
            return webClient.get()
                    .uri("/api/tipos-siniestro/{codigo}", codigo)
                    .retrieve()
                    .bodyToMono(TipoSiniestroDto.class)
                    .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                    .block();
        } catch (WebClientRequestException e) {
            throw noDisponible();
        }
    }

    private ResponseStatusException noDisponible() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio de catálogos no está disponible");
    }
}

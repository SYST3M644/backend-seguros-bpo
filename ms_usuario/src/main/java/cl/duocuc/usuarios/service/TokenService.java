package cl.duocuc.usuarios.service;

import cl.duocuc.usuarios.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class TokenService {

    private final SecretKey clave;
    private final long duracion;

    public TokenService(
            @Value("${app.jwt.secreto}") String secreto,
            @Value("${app.jwt.horas}") long horas) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracion = horas * 60 * 60 * 1000;
    }

    public String generar(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getId())
                .claim("correo", usuario.getCorreo())
                .claim("rol", usuario.getRolId())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracion))
                .signWith(clave)
                .compact();
    }
}

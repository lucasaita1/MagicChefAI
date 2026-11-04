package dev.lucas.magicchef.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;

@Tag(name = "Saúde")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DataSource dataSource;
    private final String version;
    private final boolean aiConfigured;

    public HealthController(DataSource dataSource,
                            @Value("${magicchef.version}") String version,
                            @Value("${spring.ai.openai.api-key:}") String openAiKey) {
        this.dataSource = dataSource;
        this.version = version;
        this.aiConfigured = !openAiKey.isBlank();
    }

    @Operation(summary = "Verifica se a API e o banco de dados estão no ar")
    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        boolean databaseUp = isDatabaseUp();
        HealthResponse body = new HealthResponse(
                databaseUp ? "UP" : "DOWN",
                version,
                databaseUp ? "UP" : "DOWN",
                aiConfigured,
                Instant.now());
        return ResponseEntity.status(databaseUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    private boolean isDatabaseUp() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }

    public record HealthResponse(String status, String version, String database, boolean aiConfigured,
                                 Instant timestamp) {
    }
}

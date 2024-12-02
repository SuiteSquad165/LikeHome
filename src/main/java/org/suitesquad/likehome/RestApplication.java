package org.suitesquad.likehome;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(title = "LikeHome API", version = "1.0", description = "Backend API for LikeHome. Click on 'Authorize' to enter a JWT token."),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(type = SecuritySchemeType.HTTP, bearerFormat = "jwt", name = "bearerAuth", scheme = "bearer",
        in = SecuritySchemeIn.HEADER)
@ControllerAdvice
public class RestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestApplication.class, args);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.toString());
    }

}

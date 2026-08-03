package com.devsu.banco.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Parámetros de negocio externalizados en application.yml. El límite diario de
 * retiro ($1000 según el enunciado) es configurable sin recompilar.
 */
@Component
@ConfigurationProperties(prefix = "banco")
public class BancoProperties {

    private BigDecimal limiteDiarioRetiro = new BigDecimal("1000");

    public BigDecimal getLimiteDiarioRetiro() {
        return limiteDiarioRetiro;
    }

    public void setLimiteDiarioRetiro(BigDecimal limiteDiarioRetiro) {
        this.limiteDiarioRetiro = limiteDiarioRetiro;
    }
}

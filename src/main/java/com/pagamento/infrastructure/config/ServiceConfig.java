package com.pagamento.infrastructure.config;

import com.pagamento.domain.repository.PagamentoRepository;
import com.pagamento.domain.service.PagamentoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public PagamentoService pagamentoService(PagamentoRepository pagamentoRepository) {
        return new PagamentoService(pagamentoRepository);
    }
}
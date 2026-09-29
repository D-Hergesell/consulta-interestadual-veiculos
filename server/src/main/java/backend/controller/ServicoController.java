package backend.controller;

import backend.dto.ServicoResponse;
import backend.services.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
public class ServicoController {

    private final ServicoService servicoService;

    @Operation(summary = "Lista o catálogo de serviços")
    @GetMapping
    public List<ServicoResponse> listar() {
        return servicoService.listar();
    }
}

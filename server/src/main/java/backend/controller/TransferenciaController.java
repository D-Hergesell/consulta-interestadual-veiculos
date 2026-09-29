package backend.controller;

import backend.dto.InstrucaoTransferenciaResponse;
import backend.services.TransferenciaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/transferencias")
@RequiredArgsConstructor
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    @Operation(summary = "Passo a passo, taxa, prazo e documentos para transferir o veículo entre estados")
    @GetMapping
    public InstrucaoTransferenciaResponse buscar(
            @RequestParam @Pattern(regexp = "[A-Za-z]{2}") String origem,
            @RequestParam @Pattern(regexp = "[A-Za-z]{2}") String destino) {
        return transferenciaService.buscarInstrucao(origem, destino);
    }
}

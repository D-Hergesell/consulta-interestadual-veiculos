package backend.controller;

import backend.dto.EstadoResponse;
import backend.dto.ServicoEstadoResponse;
import backend.services.EstadoService;
import backend.services.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/estados")
@RequiredArgsConstructor
public class EstadoController {

    private final EstadoService estadoService;
    private final ServicoService servicoService;

    @Operation(summary = "Lista os estados")
    @GetMapping
    public List<EstadoResponse> listar() {
        return estadoService.listar();
    }

    @Operation(summary = "Serviços oferecidos pelo DETRAN do estado, com taxa e prazo")
    @GetMapping("/{uf}/servicos")
    public List<ServicoEstadoResponse> servicos(@PathVariable @Pattern(regexp = "[A-Za-z]{2}") String uf) {
        return servicoService.listarPorEstado(uf);
    }
}

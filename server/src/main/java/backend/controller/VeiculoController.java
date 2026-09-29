package backend.controller;

import backend.dto.ConsultaPlacaResponse;
import backend.dto.MultaResponse;
import backend.services.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    private final VeiculoService veiculoService;

    @Operation(summary = "Consulta completa da placa (dados, situação, furto/roubo, restrições, multas e proprietário)")
    @GetMapping("/{placa}")
    public ConsultaPlacaResponse consultar(@PathVariable @Size(max = 10) String placa) {
        return veiculoService.consultarPorPlaca(placa);
    }

    @Operation(summary = "Lista todas as multas do veículo")
    @GetMapping("/{placa}/multas")
    public List<MultaResponse> multas(@PathVariable @Size(max = 10) String placa) {
        return veiculoService.listarMultas(placa);
    }
}

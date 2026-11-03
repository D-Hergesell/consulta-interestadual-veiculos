package backend.controller;

import backend.dto.ConsultaDetalhadaResponse;
import backend.dto.ConsultaPlacaResponse;
import backend.dto.MultaResponse;
import backend.services.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    /** RENAVAM vai no header (e nao na URL) para nao ficar em logs de acesso e historico do navegador. */
    public static final String HEADER_RENAVAM = "X-Renavam";

    private static final String DESCRICAO_RENAVAM =
            "RENAVAM do veículo (prova de posse). Ausente ou incorreto: 404, igual a placa inexistente.";

    private final VeiculoService veiculoService;

    @Operation(summary = "Consulta pública da placa: dados do veículo, situação, furto/roubo e se há restrição ativa")
    @GetMapping("/{placa}")
    public ConsultaPlacaResponse consultar(@PathVariable @Size(max = 10) String placa) {
        return veiculoService.consultarPorPlaca(placa);
    }

    // required = false: header ausente cai na mesma resposta 404 de RENAVAM errado
    @Operation(summary = "Consulta completa (ocorrências, restrições e multas pendentes). Exige RENAVAM")
    @GetMapping("/{placa}/detalhes")
    public ConsultaDetalhadaResponse detalhes(
            @PathVariable @Size(max = 10) String placa,
            @Parameter(description = DESCRICAO_RENAVAM)
            @RequestHeader(name = HEADER_RENAVAM, required = false) @Size(max = 20) String renavam) {
        return veiculoService.consultarDetalhes(placa, renavam);
    }

    @Operation(summary = "Lista todas as multas do veículo. Exige RENAVAM")
    @GetMapping("/{placa}/multas")
    public List<MultaResponse> multas(
            @PathVariable @Size(max = 10) String placa,
            @Parameter(description = DESCRICAO_RENAVAM)
            @RequestHeader(name = HEADER_RENAVAM, required = false) @Size(max = 20) String renavam) {
        return veiculoService.listarMultas(placa, renavam);
    }
}

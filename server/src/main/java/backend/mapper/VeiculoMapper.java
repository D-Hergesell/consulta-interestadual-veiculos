package backend.mapper;

import backend.dto.MultaResponse;
import backend.dto.OcorrenciaResponse;
import backend.dto.RestricaoResponse;
import backend.dto.VeiculoResponse;
import backend.entities.Estado;
import backend.entities.Multa;
import backend.entities.OcorrenciaFurtoRoubo;
import backend.entities.Restricao;
import backend.entities.Veiculo;
import org.springframework.stereotype.Component;

@Component
public class VeiculoMapper {

    public VeiculoResponse toResponse(Veiculo veiculo) {
        return VeiculoResponse.builder()
                .placa(veiculo.getPlaca())
                .marca(veiculo.getMarca())
                .modelo(veiculo.getModelo())
                .anoFabricacao(veiculo.getAnoFabricacao())
                .anoModelo(veiculo.getAnoModelo())
                .cor(veiculo.getCor())
                .tipoVeiculo(veiculo.getTipoVeiculo())
                .combustivel(veiculo.getCombustivel())
                .categoria(veiculo.getCategoria())
                .ufRegistro(uf(veiculo.getUfRegistro()))
                .municipioRegistro(veiculo.getMunicipioRegistro())
                .build();
    }

    public OcorrenciaResponse toResponse(OcorrenciaFurtoRoubo ocorrencia) {
        return OcorrenciaResponse.builder()
                .tipoOcorrencia(ocorrencia.getTipoOcorrencia())
                .dataOcorrencia(ocorrencia.getDataOcorrencia())
                .ufOcorrencia(uf(ocorrencia.getUfOcorrencia()))
                .municipioOcorrencia(ocorrencia.getMunicipioOcorrencia())
                .boletimOcorrencia(ocorrencia.getBoletimOcorrencia())
                .orgaoRegistro(ocorrencia.getOrgaoRegistro())
                .build();
    }

    public RestricaoResponse toResponse(Restricao restricao) {
        return new RestricaoResponse(
                restricao.getTipoRestricao(),
                restricao.getDescricao(),
                restricao.getOrgaoResponsavel(),
                restricao.getDataInclusao());
    }

    public MultaResponse toResponse(Multa multa) {
        return MultaResponse.builder()
                .dataInfracao(multa.getDataInfracao())
                .codigoInfracao(multa.getCodigoInfracao())
                .descricao(multa.getDescricao())
                .valor(multa.getValor())
                .pontos(multa.getPontos())
                .statusPagamento(multa.getStatusPagamento())
                .build();
    }

    private static String uf(Estado estado) {
        return estado == null ? null : estado.getUf();
    }
}

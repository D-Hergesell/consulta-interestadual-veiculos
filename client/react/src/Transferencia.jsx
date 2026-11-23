import { useState } from "react";
import { consultarApi } from "./api";

const UFS = [
    "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG",
    "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO",
];

const formatarReais = (valor) =>
    valor == null
        ? "não informado"
        : Number(valor).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });

export default function Transferencia() {
    const [origem, setOrigem] = useState("SC");
    const [destino, setDestino] = useState("SP");
    const [dados, setDados] = useState(null);
    const [erro, setErro] = useState("");
    const [carregando, setCarregando] = useState(false);

    async function consultar(evento) {
        evento.preventDefault();
        setDados(null);
        setErro("");

        if (origem === destino) {
            setErro("Origem e destino devem ser diferentes.");
            return;
        }

        setCarregando(true);
        try {
            const params = new URLSearchParams({ origem, destino });
            const resposta = await consultarApi(`/api/transferencias?${params}`, {
                contexto: "transferencia",
            });
            setDados(resposta);
        } catch (err) {
            setErro(err.message);
        } finally {
            setCarregando(false);
        }
    }

    return (
        <section className="container" aria-labelledby="transferencia-titulo">
            <h2 id="transferencia-titulo">Transferência entre estados</h2>
            <p className="helper-text">Consulte as etapas, os documentos e os custos estimados.</p>

            <form onSubmit={consultar} className="form">
                <div className="field" style={{ flex: "1 1 140px" }}>
                    <label htmlFor="uf-origem">UF de origem</label>
                    <select id="uf-origem" value={origem} onChange={(e) => { setOrigem(e.target.value); setDados(null); }}>
                        {UFS.map((uf) => <option key={uf} value={uf}>{uf}</option>)}
                    </select>
                </div>
                <span aria-hidden="true">→</span>
                <div className="field" style={{ flex: "1 1 140px" }}>
                    <label htmlFor="uf-destino">UF de destino</label>
                    <select id="uf-destino" value={destino} onChange={(e) => { setDestino(e.target.value); setDados(null); }}>
                        {UFS.map((uf) => <option key={uf} value={uf}>{uf}</option>)}
                    </select>
                </div>
                <button type="submit" disabled={carregando}>
                    {carregando ? "Consultando..." : "Ver passo a passo"}
                </button>
            </form>

            {erro && <p className="erro" role="alert">{erro}</p>}

            {dados && (
                <div className="resultado" aria-live="polite">
                    {dados.procedimentoPadrao && (
                        <p><em>Não há instrução específica para este par de UFs. Valem as orientações gerais.</em></p>
                    )}
                    <h3>Passo a passo</h3>
                    <p className="texto">{dados.passoAPasso ?? "Não informado"}</p>
                    <p><strong>Taxa estimada:</strong> {formatarReais(dados.taxaEstimada)}</p>
                    <p><strong>Prazo:</strong> {dados.prazoDias == null ? "não informado" : `${dados.prazoDias} dias`}</p>
                    <h3>Documentos necessários</h3>
                    <p className="texto">{dados.documentosNecessarios ?? "Não informado"}</p>
                    {dados.observacaoLegal && (
                        <>
                            <h3>Observação legal</h3>
                            <p className="texto">{dados.observacaoLegal}</p>
                        </>
                    )}
                    {(dados.regrasEspeciaisDestino ?? []).length > 0 && (
                        <>
                            <h3>Regras especiais do destino ({dados.ufDestino})</h3>
                            {dados.regrasEspeciaisDestino.map((item, i) => (
                                <div className="servico" key={i}>
                                    <strong>{item.titulo}</strong>
                                    <p className="texto">{item.descricao}</p>
                                </div>
                            ))}
                        </>
                    )}
                </div>
            )}
        </section>
    );
}

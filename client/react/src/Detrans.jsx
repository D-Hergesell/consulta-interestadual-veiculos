import { useEffect, useRef, useState } from "react";
import { consultarApi } from "./api";

const formatarReais = (valor) =>
    valor == null
        ? "não informado"
        : Number(valor).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });

export default function Detrans() {
    const [estados, setEstados] = useState([]);
    const [ufSelecionada, setUfSelecionada] = useState("");
    const [servicos, setServicos] = useState([]);
    const [erro, setErro] = useState("");
    const [carregandoEstados, setCarregandoEstados] = useState(true);
    const [carregandoServicos, setCarregandoServicos] = useState(false);
    const requisicaoAtual = useRef(0);

    // GET /api/estados
    useEffect(() => {
        const controller = new AbortController();
        consultarApi("/api/estados", { contexto: "estado", signal: controller.signal })
            .then((dados) => setEstados(dados))
            .catch((err) => {
                if (err.name !== "AbortError") setErro(err.message);
            })
            .finally(() => {
                if (!controller.signal.aborted) setCarregandoEstados(false);
            });
        return () => controller.abort();
    }, []);

    // GET /api/estados/{uf}/servicos
    async function selecionarUf(uf) {
        setUfSelecionada(uf);
        setServicos([]);
        setErro("");
        const id = ++requisicaoAtual.current;
        if (!uf) {
            setCarregandoServicos(false);
            return;
        }
        setCarregandoServicos(true);
        try {
            const dados = await consultarApi(`/api/estados/${uf}/servicos`, { contexto: "estado" });
            if (id === requisicaoAtual.current) setServicos(dados);
        } catch (err) {
            if (id === requisicaoAtual.current) setErro(err.message);
        } finally {
            if (id === requisicaoAtual.current) setCarregandoServicos(false);
        }
    }

    const estado = estados.find((item) => item.uf === ufSelecionada);

    return (
        <section className="container" aria-labelledby="detrans-titulo">
            <h2 id="detrans-titulo">Detrans por estado</h2>
            <p className="helper-text">Selecione uma UF para consultar os contatos e serviços disponíveis.</p>
            <div className="field" style={{ marginTop: 15 }}>
                <label htmlFor="estado-detran">Estado</label>
                <select
                    id="estado-detran"
                    value={ufSelecionada}
                    onChange={(e) => selecionarUf(e.target.value)}
                    disabled={carregandoEstados}
                >
                    <option value="">{carregandoEstados ? "Carregando estados..." : "Escolha um estado"}</option>
                    {estados.map((item) => (
                        <option key={item.uf} value={item.uf}>
                            {item.uf} - {item.nome}
                        </option>
                    ))}
                </select>
            </div>

            {erro && <p className="erro" role="alert">{erro}</p>}
            {estado && (
                <div className="resultado" aria-live="polite">
                    <h3>{estado.siglaOrgao ?? "Detran"} — {estado.nome}</h3>
                    <p>
                        <strong>Site:</strong>{" "}
                        {estado.urlDetran ? (
                            <a href={estado.urlDetran} target="_blank" rel="noopener noreferrer">
                                Acessar site oficial (link externo)
                            </a>
                        ) : "não informado"}
                    </p>
                    <p><strong>Telefone:</strong> {estado.telefone ?? "não informado"}</p>
                    <p><strong>WhatsApp:</strong> {estado.whatsapp ?? "não informado"}</p>

                    <h3>Serviços disponíveis</h3>
                    {carregandoServicos ? <p>Carregando serviços...</p> : null}
                    {!carregandoServicos && servicos.length === 0 && !erro && (
                        <p>Nenhum serviço cadastrado para essa UF.</p>
                    )}
                    {!carregandoServicos && servicos.map((item) => (
                        <div key={item.servico.id} className="servico">
                            <strong>{item.servico.nome}</strong>
                            <p className="texto">{item.servico.descricao ?? "Descrição não informada"}</p>
                            <p>
                                <strong>Taxa estimada:</strong> {formatarReais(item.taxaEstimada)} |{" "}
                                <strong>Prazo:</strong> {item.prazoDias == null ? "não informado" : `${item.prazoDias} dias`}
                            </p>
                            <p><strong>Disponível online:</strong> {item.servico.disponivelOnline ? "Sim" : "Não"}</p>
                            {item.urlServico && (
                                <p>
                                    <a href={item.urlServico} target="_blank" rel="noopener noreferrer">
                                        Acessar serviço (link externo)
                                    </a>
                                </p>
                            )}
                            {item.observacoes && <p className="texto">{item.observacoes}</p>}
                        </div>
                    ))}
                </div>
            )}
        </section>
    );
}

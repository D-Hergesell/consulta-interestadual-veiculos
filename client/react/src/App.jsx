import { useState } from "react";
import "./App.css";
import Transferencia from "./Transferencia";
import Detrans from "./Detrans";
import Servicos from "./Servicos";
import { consultarApi } from "./api";

const TEMPO_LIMITE_CONSULTA = 90_000; // 90 segundos
const TEMPO_AVISO_DEMORA = 8_000; // 8 segundos

const formatarReais = (valor) => valor == null ? "não informado" : Number(valor).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });


function identificarFurtoRoubo(situacao, constaFurtoRoubo) {
    if (!constaFurtoRoubo) {
        return "Não consta";
    }

    const status = String(situacao ?? "").trim().toLowerCase();

    if (status === "furtado" || status === "furto") {
        return "Consta furto";
    }

    if (status === "roubado" || status === "roubo") {
        return "Consta roubo";
    }

    return "Consta registro de furto ou roubo";
}


function Info({ label, children }) {
    return <div className="result-item"><span>{label}</span><strong>{children ?? "não informado"}</strong></div>;
}

export default function App() {
    const [placa, setPlaca] = useState("");
    const [renavam, setRenavam] = useState("");
    const [publica, setPublica] = useState(null);
    const [detalhes, setDetalhes] = useState(null);
    const [multas, setMultas] = useState(null);
    const [erro, setErro] = useState("");
    const [carregando, setCarregando] = useState(false);
    const [esperaLonga, setEsperaLonga] = useState(false);
    const placaValida = /^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$/.test(placa);
    const renavamValido = /^\d{11}$/.test(renavam);

    async function chamarApi(caminho, comRenavam = false) {
        let temporizador;

        try {
            // Limita a espera percebida pelo usuário, sem modificar o backend.
            // A requisição de rede pode continuar, mas o resultado tardio será ignorado.
            return await Promise.race([
                consultarApi(caminho, {
                    contexto: "veiculo",
                    renavam: comRenavam ? renavam.trim() : undefined,
                }),
                new Promise((_, rejeitar) => {
                    temporizador = setTimeout(() => {
                        rejeitar(new Error(
                            "O servidor demorou mais de 90 segundos para responder. " +
                            "Ele pode estar iniciando ou temporariamente indisponível. " +
                            "Tente novamente em alguns instantes."
                        ));
                    }, TEMPO_LIMITE_CONSULTA);
                }),
            ]);
        } finally {
            clearTimeout(temporizador);
        }
    }

    async function executar(acao) {
        setErro("");
        setCarregando(true);
        setEsperaLonga(false);

        const avisoDemora = setTimeout(() => {
            setEsperaLonga(true);
        }, TEMPO_AVISO_DEMORA);

        try {
            await acao();
        } catch (err) {
            setErro(err instanceof Error ? err.message : "Não foi possível concluir a consulta.");
        } finally {
            clearTimeout(avisoDemora);
            setCarregando(false);
            setEsperaLonga(false);
        }
    }

    function consultarPublica(e) {
        e.preventDefault();
        executar(async () => {
            setPublica(null);
            setDetalhes(null);
            setMultas(null);
            setPublica(await chamarApi(`/api/veiculos/${placa.trim()}`));
        });
    }

    function consultarDetalhes() {
        executar(async () => {
            setDetalhes(null);
            setMultas(null);
            setDetalhes(await chamarApi(`/api/veiculos/${placa.trim()}/detalhes`, true));
        });
    }

    function consultarMultas() {
        executar(async () => {
            setMultas(null);
            setDetalhes(null);
            setMultas(await chamarApi(`/api/veiculos/${placa.trim()}/multas`, true));
        });
    }

    return (
        <>
            <header className="site-header">
                <div className="header-inner">
                    <a className="brand" href="#inicio" aria-label="Voltar ao início">
                        <span className="brand-mark">CV</span>
                        <span><strong>Consulta Veicular</strong><small>Consulta interestadual de veículos</small></span>
                    </a>
                    <span className="header-tag">Projeto acadêmico • Não oficial</span>
                </div>
            </header>

            <section className="hero-banner" id="inicio">
                <div className="hero-inner">
                    <span className="eyebrow">Plataforma de consulta interestadual</span>
                    <h1>Consulte informações de veículos de outros estados.</h1>
                    <p>Confira a situação do veículo, possíveis restrições e multas antes de tomar uma decisão de compra.</p>
                </div>
                <div className="road-line" aria-hidden="true" />
            </section>

            <main className="page-container">
                <section className="panel" aria-labelledby="consulta-titulo">
                    <div className="section-heading">
                        <span className="step-number">01</span>
                        <div><h2 id="consulta-titulo">Identifique o veículo</h2><p>Digite a placa para realizar uma consulta pública.</p></div>
                    </div>
                    <form onSubmit={consultarPublica}>
                        <div className="field-grid">
                            <div className="field">
                                <label htmlFor="placa">Placa do veículo</label>
                                <input id="placa" type="text" inputMode="text" autoComplete="off" value={placa} onChange={(e) => { setPlaca(e.target.value.toUpperCase().replace(/[^A-Z0-9]/g, "").slice(0, 7)); setPublica(null); setDetalhes(null); setMultas(null); }} placeholder="Ex.: ABC1D23" maxLength={7} required aria-describedby="placa-hint" />
                                <span className="field-hint" id="placa-hint">Aceita placas antigas e Mercosul.</span>
                            </div>
                            <div className="field">
                                <label htmlFor="renavam">RENAVAM <span className="field-hint">(consulta avançada)</span></label>
                                <input id="renavam" type="text" inputMode="numeric" autoComplete="off" value={renavam} onChange={(e) => { setRenavam(e.target.value.replace(/\D/g, "").slice(0, 11)); setDetalhes(null); setMultas(null); }} placeholder="Digite os 11 números" maxLength={11} aria-describedby="renavam-hint" />
                                <span className="field-hint" id="renavam-hint">Necessário somente para detalhes e multas.</span>
                            </div>
                        </div>
                        <div className="form-actions">
                            <button className="btn" type="submit" disabled={carregando || !placaValida}>{carregando ? "Consultando..." : "Consultar veículo"}</button>
                            <span className="helper-text">Preencha a placa para liberar a consulta.</span>
                        </div>
                    </form>
                    {carregando && esperaLonga && (
                        <p className="helper-text" role="status" style={{ display: "block", marginTop: 12 }}>
                            O servidor está demorando para responder. Ele pode estar iniciando
                            na nuvem. A consulta continuará tentando por até 90 segundos.
                        </p>
                    )}
                    <div className="divider-label">Consultas complementares</div>
                    <div className="form-actions" style={{ marginTop: 0 }}>
                        <button type="button" className="btn btn-secondary" disabled={carregando || !placaValida || !renavamValido} onClick={consultarDetalhes}>Ver detalhes completos</button>
                        <button type="button" className="btn btn-danger" disabled={carregando || !placaValida || !renavamValido} onClick={consultarMultas}>Consultar multas</button>
                    </div>
                    <div className="secure-note">ⓘ As informações exibidas dependem dos dados disponibilizados pela API do projeto. Esta plataforma não é um serviço oficial do Detran-SC.</div>
                </section>

                {erro && <p className="erro" role="alert">{erro}</p>}

                {publica && <section className="resultado" aria-live="polite">
                    <h2>Resultado da consulta</h2>
                    <div className="result-grid">
                        <Info label="Veículo">{publica.veiculo.marca} {publica.veiculo.modelo}</Info>
                        <Info label="Placa">{publica.veiculo.placa}</Info>
                        <Info label="Ano do modelo">{publica.veiculo.anoModelo}</Info>
                        <Info label="Cor">{publica.veiculo.cor ?? "não informado"}</Info>
                        <Info label="Situação">{publica.situacao}</Info>
                        <Info label="Furto ou roubo">
                            <span className={`result-status ${publica.constaFurtoRoubo ? "alert" : ""}`}>
                                {identificarFurtoRoubo(publica.situacao, publica.constaFurtoRoubo)}
                            </span>
                        </Info>

                        <Info label="Restrição ativa"><span className={`result-status ${publica.possuiRestricaoAtiva ? "alert" : ""}`}>{publica.possuiRestricaoAtiva ? "Há restrição" : "Não consta"}</span></Info>
                    </div>
                </section>}

                {detalhes && <section className="resultado" aria-live="polite">
                    <h2>Detalhes completos</h2>
                    <h3>Ocorrências</h3>
                    {(detalhes.ocorrenciasAtivas ?? []).length === 0 && <p>Nenhuma ocorrência ativa.</p>}
                    {(detalhes.ocorrenciasAtivas ?? []).map((o, i) => <p key={i}>{o.tipoOcorrencia} em {o.dataOcorrencia} ({o.ufOcorrencia}), BO: {o.boletimOcorrencia ?? "não informado"}</p>)}
                    <h3>Restrições</h3>
                    {(detalhes.restricoesAtivas ?? []).length === 0 && <p>Nenhuma restrição ativa.</p>}
                    {(detalhes.restricoesAtivas ?? []).map((r, i) => <p key={i}>{r.tipoRestricao}: {r.descricao ?? "sem descrição"}</p>)}
                    <h3>Multas pendentes</h3>
                    {(detalhes.multasPendentes ?? []).length === 0 && <p>Nenhuma multa pendente.</p>}
                    {(detalhes.multasPendentes ?? []).map((m, i) => <p key={i}>{m.dataInfracao}: {m.descricao} ({formatarReais(m.valor)}, {m.pontos} pontos)</p>)}
                    <p><strong>Total pendente:</strong> {formatarReais(detalhes.totalMultasPendentes)}</p>
                </section>}

                {multas && <section className="resultado" aria-live="polite">
                    <h2>Todas as multas</h2>
                    {multas.length === 0 && <p>Nenhuma multa encontrada.</p>}
                    {multas.map((m, i) => <p key={i}>{m.dataInfracao}: {m.descricao} ({formatarReais(m.valor)}, {m.pontos} pontos) – {m.statusPagamento ?? "sem status"}</p>)}
                </section>}

                <h2 className="subsection-title">Outros serviços</h2>
                <div className="modules">
                    <div className="module-card"><Transferencia /></div>
                    <div className="module-card"><Detrans /></div>
                    <div className="module-card"><Servicos /></div>
                </div>
            </main>
            <footer className="site-footer"><strong>Consulta Veicular Interestadual</strong> • Projeto acadêmico inspirado na identidade visual do Detran-SC. Sem vínculo institucional.</footer>
        </>
    );
}

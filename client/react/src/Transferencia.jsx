// client/react/src/Transferencia.jsx
import { useState } from "react";

const API = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

const UFS = [
    "AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS","MG",
    "PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO",
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

    async function consultar(e) {
        e.preventDefault();
        setErro("");
        setDados(null);

        if (origem === destino) {
            setErro("Origem e destino devem ser diferentes.");
            return;
        }

        setCarregando(true);
        try {
            const resp = await fetch(
                `${API}/api/transferencias?origem=${origem}&destino=${destino}`
            );

            if (resp.status === 429) {
                const s = resp.headers.get("Retry-After") ?? 60;
                throw new Error(`Muitas consultas. Tente de novo em ${s} s.`);
            }
            if (!resp.ok) {
                const problema = await resp.json().catch(() => null);
                throw new Error(problema?.detail ?? "Erro no servidor. Tente mais tarde.");
            }

            setDados(await resp.json());
        } catch (err) {
            setErro(err.message);
        } finally {
            setCarregando(false);
        }
    }

    return (
        <section className="container">
            <h2>Transferência entre estados</h2>

            <form onSubmit={consultar} className="form">
                <select value={origem} onChange={(e) => setOrigem(e.target.value)}>
                    {UFS.map((uf) => <option key={uf} value={uf}>{uf}</option>)}
                </select>
                <span>→</span>
                <select value={destino} onChange={(e) => setDestino(e.target.value)}>
                    {UFS.map((uf) => <option key={uf} value={uf}>{uf}</option>)}
                </select>
                <button type="submit" disabled={carregando}>
                    {carregando ? "Consultando..." : "Ver passo a passo"}
                </button>
            </form>

            {erro && <p className="erro">{erro}</p>}

            {dados && (
                <div className="resultado">
                    {dados.procedimentoPadrao && (
                        <p><em>Não há instrução específica para este par. Valem as regras padrão.</em></p>
                    )}

                    <h3>Passo a passo</h3>
                    <p className="texto">{dados.passoAPasso}</p>

                    <p><strong>Taxa estimada:</strong> {formatarReais(dados.taxaEstimada)}</p>
                    <p><strong>Prazo:</strong> {dados.prazoDias ? `${dados.prazoDias} dias` : "não informado"}</p>

                    <h3>Documentos necessários</h3>
                    <p className="texto">{dados.documentosNecessarios}</p>

                    {dados.observacaoLegal && (
                        <>
                            <h3>Observação legal</h3>
                            <p className="texto">{dados.observacaoLegal}</p>
                        </>
                    )}

                    {dados.regrasEspeciaisDestino.length > 0 && (
                        <>
                            <h3>Regras especiais do destino ({dados.ufDestino})</h3>
                            {dados.regrasEspeciaisDestino.map((r, i) => (
                                <div key={i}>
                                    <strong>{r.titulo}</strong>
                                    <p className="texto">{r.descricao}</p>
                                </div>
                            ))}
                        </>
                    )}
                </div>
            )}
        </section>
    );
}
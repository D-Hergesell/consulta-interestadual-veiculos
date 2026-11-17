// client/react/src/App.jsx
import { useState } from "react";
import "./App.css";
import Transferencia from "./Transferencia";
import Detrans from "./Detrans";

const API = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

const formatarReais = (valor) =>
    valor == null
        ? "não informado"
        : Number(valor).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });

export default function App() {
  const [placa, setPlaca] = useState("");
  const [renavam, setRenavam] = useState("");
  const [publica, setPublica] = useState(null);
  const [detalhes, setDetalhes] = useState(null);
  const [multas, setMultas] = useState(null);
  const [erro, setErro] = useState("");
  const [carregando, setCarregando] = useState(false);

  async function chamarApi(caminho, comRenavam = false) {
    const headers = comRenavam ? { "X-Renavam": renavam.trim() } : {};
    const resp = await fetch(`${API}${caminho}`, { headers });

    if (resp.status === 429) {
      const s = resp.headers.get("Retry-After") ?? 60;
      throw new Error(`Muitas consultas. Tente de novo em ${s} s.`);
    }
    if (resp.status === 404) {
      throw new Error(
          comRenavam
              ? "Veículo não encontrado. Confira a placa e o RENAVAM."
              : "Veículo não encontrado. Confira a placa."
      );
    }
    if (!resp.ok) {
      const problema = await resp.json().catch(() => null);
      throw new Error(problema?.detail ?? "Erro no servidor. Tente mais tarde.");
    }
    return resp.json();
  }

  async function executar(acao) {
    setErro("");
    setCarregando(true);
    try {
      await acao();
    } catch (err) {
      setErro(err.message);
    } finally {
      setCarregando(false);
    }
  }

  function consultarPublica(e) {
    e.preventDefault();
    executar(async () => {
      setDetalhes(null);
      setMultas(null);
      setPublica(await chamarApi(`/api/veiculos/${placa.trim()}`));
    });
  }

  function consultarDetalhes() {
    executar(async () => {
      setDetalhes(await chamarApi(`/api/veiculos/${placa.trim()}/detalhes`, true));
      setMultas(null);
    });
  }

  function consultarMultas() {
    executar(async () => {
      setMultas(await chamarApi(`/api/veiculos/${placa.trim()}/multas`, true));
      setDetalhes(null);
    });
  }

  return (
      <main className="container">
        <h1>Consulta de veículo</h1>

        <form onSubmit={consultarPublica} className="form">
          <input
              value={placa}
              onChange={(e) => setPlaca(e.target.value)}
              placeholder="Placa (ABC1234 ou ABC1D23)"
              maxLength={8}
              required
          />
          <button type="submit" disabled={carregando}>
            {carregando ? "Consultando..." : "Consultar"}
          </button>
        </form>

        <div className="form">
          <input
              value={renavam}
              onChange={(e) => setRenavam(e.target.value)}
              placeholder="RENAVAM (para detalhes e multas)"
              maxLength={11}
          />
          <button onClick={consultarDetalhes} disabled={carregando || !placa || !renavam}>
            Detalhes
          </button>
          <button onClick={consultarMultas} disabled={carregando || !placa || !renavam}>
            Multas
          </button>
        </div>

        {erro && <p className="erro">{erro}</p>}

        {publica && (
            <section className="resultado">
              <h2>{publica.veiculo.marca} {publica.veiculo.modelo} ({publica.veiculo.anoModelo})</h2>
              <p><strong>Placa:</strong> {publica.veiculo.placa}</p>
              <p><strong>Cor:</strong> {publica.veiculo.cor ?? "não informado"}</p>
              <p><strong>Situação:</strong> {publica.situacao}</p>
              <p><strong>Consta furto/roubo:</strong> {publica.constaFurtoRoubo ? "Sim" : "Não"}</p>
              <p><strong>Restrição ativa:</strong> {publica.possuiRestricaoAtiva ? "Sim" : "Não"}</p>
            </section>
        )}

        {detalhes && (
            <section className="resultado">
              <h2>Detalhes completos</h2>

              <h3>Ocorrências</h3>
              {detalhes.ocorrenciasAtivas.length === 0 && <p>Nenhuma ocorrência ativa.</p>}
              {detalhes.ocorrenciasAtivas.map((o, i) => (
                  <p key={i}>
                    {o.tipoOcorrencia} em {o.dataOcorrencia} ({o.ufOcorrencia}), BO: {o.boletimOcorrencia ?? "não informado"}
                  </p>
              ))}

              <h3>Restrições</h3>
              {detalhes.restricoesAtivas.length === 0 && <p>Nenhuma restrição ativa.</p>}
              {detalhes.restricoesAtivas.map((r, i) => (
                  <p key={i}>
                    {r.tipoRestricao}: {r.descricao ?? "sem descrição"}
                  </p>
              ))}

              <h3>Multas pendentes</h3>
              {detalhes.multasPendentes.length === 0 && <p>Nenhuma multa pendente.</p>}
              {detalhes.multasPendentes.map((m, i) => (
                  <p key={i}>
                    {m.dataInfracao}: {m.descricao} ({formatarReais(m.valor)}, {m.pontos} pontos)
                  </p>
              ))}
              <p><strong>Total pendente:</strong> {formatarReais(detalhes.totalMultasPendentes)}</p>
            </section>
        )}

        {multas && (
            <section className="resultado">
              <h2>Todas as multas</h2>
              {multas.length === 0 && <p>Nenhuma multa encontrada.</p>}
              {multas.map((m, i) => (
                  <p key={i}>
                    {m.dataInfracao}: {m.descricao} ({formatarReais(m.valor)}, {m.pontos} pontos) - {m.statusPagamento ?? "sem status"}
                  </p>
              ))}
            </section>
        )}

        <Transferencia />
        <Detrans />
      </main>
  );
}
// client/react/src/Detrans.jsx
import { useEffect, useState } from "react";

const API = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

// Contatos usados quando a API não traz telefone/WhatsApp (valor null).
// Só entram aqui números confirmados em fonte oficial ou em notícia.
// "não informado" = ainda não confirmado.
const CONTATOS_FIXOS = {
    SC: {
        telefone: "(48) 3664-1800",
        whatsapp: "(48) 3664-1800",
        email: "centraldeinformacoes@detran.sc.gov.br",
    },
    PR: {
        telefone: "0800 643 7373",
        whatsapp: "Projeto Copiloto (seg. a sex., 8h às 16h)",
    },
    RJ: {
        telefone: "(21) 3460-4040",
        whatsapp: "(21) 3460-4040",
    },
};

const formatarReais = (valor) =>
    valor == null
        ? "não informado"
        : Number(valor).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });

export default function Detrans() {
    const [estados, setEstados] = useState([]);
    const [ufSelecionada, setUfSelecionada] = useState("");
    const [servicos, setServicos] = useState([]);
    const [erro, setErro] = useState("");
    const [carregando, setCarregando] = useState(false);

    // Carrega a lista de estados ao abrir a tela
    useEffect(() => {
        fetch(`${API}/api/estados`)
            .then((resp) => {
                if (!resp.ok) throw new Error("Não foi possível carregar os estados.");
                return resp.json();
            })
            .then((dados) => setEstados(dados))
            .catch((err) => setErro(err.message));
    }, []);

    // Carrega os serviços sempre que uma UF é escolhida
    async function selecionarUf(uf) {
        setUfSelecionada(uf);
        setServicos([]);
        setErro("");
        if (!uf) return;

        setCarregando(true);
        try {
            const resp = await fetch(`${API}/api/estados/${uf}/servicos`);
            if (!resp.ok) throw new Error("Não foi possível carregar os serviços.");
            setServicos(await resp.json());
        } catch (err) {
            setErro(err.message);
        } finally {
            setCarregando(false);
        }
    }

    const estadoAtual = estados.find((e) => e.uf === ufSelecionada);
    const fixo = CONTATOS_FIXOS[ufSelecionada] ?? {};

    // Usa o dado da API; se vier null, usa o contato fixo
    const telefone = estadoAtual?.telefone ?? fixo.telefone ?? "não informado";
    const whatsapp = estadoAtual?.whatsapp ?? fixo.whatsapp ?? "não informado";
    const email = fixo.email;

    return (
        <section className="container">
            <h2>Detrans por estado</h2>

            <select value={ufSelecionada} onChange={(e) => selecionarUf(e.target.value)}>
                <option value="">Escolha um estado</option>
                {estados.map((e) => (
                    <option key={e.uf} value={e.uf}>
                        {e.uf} - {e.nome}
                    </option>
                ))}
            </select>

            {erro && <p className="erro">{erro}</p>}

            {estadoAtual && (
                <div className="resultado">
                    <h3>{estadoAtual.siglaOrgao ?? "Detran"}</h3>
                    <p>
                        <strong>Site:</strong>{" "}
                        {estadoAtual.urlDetran ? (
                            <a href={estadoAtual.urlDetran} target="_blank" rel="noreferrer">
                                {estadoAtual.urlDetran}
                            </a>
                        ) : (
                            "não informado"
                        )}
                    </p>
                    <p><strong>Telefone:</strong> {telefone}</p>
                    <p><strong>WhatsApp:</strong> {whatsapp}</p>
                    {email && <p><strong>E-mail geral:</strong> {email}</p>}

                    <h3>Serviços</h3>
                    {carregando && <p>Carregando serviços...</p>}
                    {!carregando && servicos.length === 0 && <p>Nenhum serviço cadastrado.</p>}
                    {servicos.map((s) => (
                        <div key={s.servico.id} className="servico">
                            <strong>{s.servico.nome}</strong>
                            <p>{s.servico.descricao}</p>
                            <p>
                                <strong>Taxa:</strong> {formatarReais(s.taxaEstimada)} |{" "}
                                <strong>Prazo:</strong> {s.prazoDias ? `${s.prazoDias} dias` : "não informado"}
                            </p>
                            <p><strong>Online:</strong> {s.servico.disponivelOnline ? "Sim" : "Não"}</p>
                            {s.urlServico && (
                                <a href={s.urlServico} target="_blank" rel="noreferrer">Acessar serviço</a>
                            )}
                            {s.observacoes && <p className="texto">{s.observacoes}</p>}
                        </div>
                    ))}
                </div>
            )}
        </section>
    );
}
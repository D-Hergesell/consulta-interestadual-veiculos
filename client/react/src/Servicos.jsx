import { useEffect, useState } from "react";
import { consultarApi } from "./api";

/** Catálogo geral: GET /api/servicos */
export default function Servicos() {
    const [servicos, setServicos] = useState([]);
    const [carregando, setCarregando] = useState(true);
    const [erro, setErro] = useState("");

    useEffect(() => {
        const controller = new AbortController();
        consultarApi("/api/servicos", { contexto: "catalogo", signal: controller.signal })
            .then(setServicos)
            .catch((err) => {
                if (err.name !== "AbortError") setErro(err.message);
            })
            .finally(() => {
                if (!controller.signal.aborted) setCarregando(false);
            });
        return () => controller.abort();
    }, []);

    return (
        <section className="container" aria-labelledby="catalogo-titulo">
            <h2 id="catalogo-titulo">Catálogo geral de serviços</h2>
            <p className="helper-text">Veja os tipos de serviços disponíveis para consulta na plataforma.</p>
            {carregando && <p style={{ marginTop: 16 }}>Carregando catálogo...</p>}
            {erro && <p className="erro" role="alert">{erro}</p>}
            {!carregando && !erro && servicos.length === 0 && <p>Nenhum serviço cadastrado.</p>}
            {!carregando && servicos.map((servico) => (
                <div className="servico" key={servico.id}>
                    <strong>{servico.nome}</strong>
                    <p className="texto">{servico.descricao ?? "Descrição não informada"}</p>
                    <p><strong>Categoria:</strong> {servico.categoria ?? "não informada"}</p>
                    <p><strong>Disponível online:</strong> {servico.disponivelOnline ? "Sim" : "Não"}</p>
                </div>
            ))}
        </section>
    );
}

/**
 * Cliente HTTP compartilhado pelos componentes React.
 * Defina VITE_API_URL no arquivo client/react/.env (sem /api no final).
 */
const API = (import.meta.env.VITE_API_URL || "http://localhost:8080")
    .trim()
    .replace(/\/+$/, "");

/**
 * @param {string} rota Rota iniciada com /api/.
 * @param {{renavam?: string, contexto?: string, signal?: AbortSignal}} options
 */
export async function consultarApi(rota, options = {}) {
    const { renavam, contexto = "geral", signal } = options;
    const headers = { Accept: "application/json" };

    // O contrato exige o RENAVAM exclusivamente no header, nunca na URL.
    if (renavam) headers["X-Renavam"] = renavam.trim();

    let resposta;
    try {
        resposta = await fetch(`${API}${rota}`, {
            method: "GET",
            headers,
            signal,
        });
    } catch (erro) {
        if (erro.name === "AbortError") throw erro;
        throw new Error(
            "Não foi possível acessar o backend. Confira VITE_API_URL, a disponibilidade da API e as permissões de CORS."
        );
    }

    if (resposta.ok) {
        try {
            return await resposta.json();
        } catch {
            throw new Error("A API retornou uma resposta que não é um JSON válido.");
        }
    }

    if (resposta.status === 429) {
        const segundos = resposta.headers.get("Retry-After") ?? "60";
        throw new Error(`Muitas consultas. Tente novamente em ${segundos} segundos.`);
    }

    if (resposta.status === 403) {
        throw new Error(
            "Acesso recusado pela API (403). Confirme a rota e se http://localhost:5173 está liberado no CORS do backend."
        );
    }

    if (resposta.status === 404) {
        const mensagens = {
            veiculo: "Veículo não encontrado. Confira a placa e o RENAVAM, quando informado.",
            estado: "Estado ou serviços não encontrados.",
            transferencia: "Não foram encontradas instruções para essa transferência.",
            catalogo: "Catálogo de serviços não encontrado.",
        };
        throw new Error(mensagens[contexto] ?? "Recurso não encontrado (404).");
    }

    if (resposta.status >= 500) {
        throw new Error("Erro no servidor. Tente novamente mais tarde.");
    }

    // Erros de validação (400) chegam no formato application/problem+json.
    const problema = await resposta.json().catch(() => null);
    throw new Error(problema?.detail || `A API retornou o erro ${resposta.status}.`);
}

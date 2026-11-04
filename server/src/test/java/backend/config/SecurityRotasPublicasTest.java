package backend.config;

import backend.controller.EstadoController;
import backend.services.EstadoService;
import backend.services.ServicoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Toda rota dos controllers do projeto precisa de uma decisao explicita em SecurityConfig.ROTAS_PUBLICAS.
 * Se este teste falhar ao criar um endpoint novo: decida se ele e publico (adicione na lista) ou se
 * precisa de autenticacao (implemente antes de liberar).
 * <p>
 * As rotas sao descobertas lendo as anotacoes dos controllers (sem subir o contexto), entao controller
 * novo nao exige mock novo aqui. O contexto MockMvc so carrega o EstadoController, para os testes de 403.
 */
@WebMvcTest(controllers = EstadoController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, CorsConfig.class})
class SecurityRotasPublicasTest {

    private static final String PACOTE_BASE = "backend";
    private static final AntPathMatcher PATHS = new AntPathMatcher();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstadoService estadoService;
    @MockitoBean
    private ServicoService servicoService;

    @Test
    void listaPublicaNaoTemRotaDuplicada() {
        assertThat(SecurityConfig.ROTAS_PUBLICAS).doesNotHaveDuplicates();
    }

    @Test
    void todoHandlerDoProjetoEGetEEstaNaListaPublica() {
        Map<String, Set<RequestMethod>> rotas = rotasDoProjeto();

        assertThat(rotas).as("nenhum controller encontrado").isNotEmpty();
        rotas.forEach((rota, metodos) ->
                assertThat(metodos).as("metodos de %s (a API so libera GET)", rota).containsOnly(RequestMethod.GET));
        assertThat(SecurityConfig.ROTAS_PUBLICAS)
                .as("rotas sem decisao em SecurityConfig.ROTAS_PUBLICAS")
                .containsAll(rotas.keySet());
    }

    @Test
    void listaPublicaNaoTemRotaSemHandler() {
        assertThat(rotasDoProjeto().keySet())
                .as("entradas de ROTAS_PUBLICAS que nao correspondem a nenhum handler")
                .containsAll(SecurityConfig.ROTAS_PUBLICAS);
    }

    @Test
    void normalizaVariaveisDePathInclusiveComRegex() {
        assertThat(normalizar("/api/veiculos/{placa}/multas")).isEqualTo("/api/veiculos/*/multas");
        assertThat(normalizar("/api/veiculos/{placa:[A-Z]{3}[0-9]{4}}")).isEqualTo("/api/veiculos/*");
        assertThat(normalizar("/api/estados")).isEqualTo("/api/estados");
    }

    @Test
    void getEmRotaForaDaListaENegado() throws Exception {
        mockMvc.perform(get("/api/admin/relatorio")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/proprietarios/1")).andExpect(status().isForbidden());
    }

    @Test
    void rotaDaListaContinuaPublica() throws Exception {
        mockMvc.perform(get("/api/estados")).andExpect(status().isOk());
    }

    /** Rotas dos controllers do pacote backend, com {variavel} trocada por "*" (mesmo formato da lista). */
    private static Map<String, Set<RequestMethod>> rotasDoProjeto() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        // @RestController e meta-anotado com @Controller, entao o filtro pega os dois
        scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));

        Map<String, Set<RequestMethod>> rotas = new TreeMap<>();
        scanner.findCandidateComponents(PACOTE_BASE).forEach(definicao -> {
            Class<?> controller = ClassUtils.resolveClassName(definicao.getBeanClassName(), null);
            RequestMapping doController = AnnotatedElementUtils.findMergedAnnotation(controller, RequestMapping.class);
            for (Method metodo : ReflectionUtils.getUniqueDeclaredMethods(controller)) {
                RequestMapping doMetodo = AnnotatedElementUtils.findMergedAnnotation(metodo, RequestMapping.class);
                if (doMetodo == null) {
                    continue;
                }
                Set<RequestMethod> metodos = metodosDe(doMetodo);
                for (String base : caminhos(doController)) {
                    for (String caminho : caminhos(doMetodo)) {
                        rotas.computeIfAbsent(normalizar(PATHS.combine(base, caminho)), r -> new TreeSet<>())
                                .addAll(metodos);
                    }
                }
            }
        });
        return rotas;
    }

    private static List<String> caminhos(RequestMapping mapeamento) {
        return mapeamento == null || mapeamento.path().length == 0 ? List.of("") : List.of(mapeamento.path());
    }

    // Mapeamento sem metodo declarado aceita todos: conta como nao-GET para o teste acusar
    private static Set<RequestMethod> metodosDe(RequestMapping mapeamento) {
        return mapeamento.method().length == 0 ? Set.of(RequestMethod.values()) : Set.of(mapeamento.method());
    }

    /** Troca cada {variavel} por "*", respeitando chaves aninhadas de regex (ex.: {placa:[A-Z]{3}}). */
    private static String normalizar(String caminho) {
        StringBuilder saida = new StringBuilder();
        int profundidade = 0;
        for (char c : caminho.toCharArray()) {
            if (c == '{') {
                if (profundidade++ == 0) {
                    saida.append('*');
                }
            } else if (c == '}') {
                profundidade--;
            } else if (profundidade == 0) {
                saida.append(c);
            }
        }
        return saida.toString();
    }
}

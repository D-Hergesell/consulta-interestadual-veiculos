package backend.util;

import lombok.experimental.UtilityClass;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

@UtilityClass
public class UrlUtils {

    private static final Set<String> ESQUEMAS_PERMITIDOS = Set.of("http", "https");

    /**
     * Devolve a URL so se for http/https absoluta e com host; qualquer outra coisa vira null.
     * Barra javascript:, data:, vbscript:, URL relativa e texto malformado antes de chegar ao front,
     * onde o valor vai parar num href.
     */
    public String httpOuNula(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String limpa = url.strip();
        try {
            URI uri = new URI(limpa);
            String esquema = uri.getScheme();
            if (esquema == null || !ESQUEMAS_PERMITIDOS.contains(esquema.toLowerCase(Locale.ROOT))
                    || uri.getHost() == null) {
                return null;
            }
            return limpa;
        } catch (URISyntaxException e) {
            return null;
        }
    }
}

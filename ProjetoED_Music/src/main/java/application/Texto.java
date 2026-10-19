package application;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Funcoes utilitarias para tratamento de texto (acentos, caixa, limpeza).
 */
public final class Texto {

    private Texto() {
    }

    /** Remove acentos: "Sao Paulo" e "São Paulo" passam a ser iguais. */
    public static String semAcento(String texto) {
        String decomposto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return decomposto.replaceAll("\\p{M}+", "");
    }

    /**
     * Chave de comparacao: minuscula, sem acentos, apenas letras e espacos,
     * sem espacos duplicados. Usada em buscas e nas tabelas hash.
     */
    public static String chave(String texto) {
        String t = semAcento(texto).toLowerCase(Locale.ROOT);
        t = t.replaceAll("[^a-z ]", " ");
        return t.trim().replaceAll(" +", " ");
    }

    /** Remove caracteres que quebrariam o leiaute do arquivo (separador e quebras de linha). */
    public static String limpar(String texto) {
        return texto.replace(';', ',').replace('\n', ' ').replace('\r', ' ').trim();
    }

    /** Corta o texto para caber em colunas de tabela. */
    public static String cortar(String texto, int tamanho_maximo) {
        if (texto.length() <= tamanho_maximo) {
            return texto;
        }
        return texto.substring(0, tamanho_maximo - 1) + "…";
    }
}

package servico;
/** Contadores de operacoes de um algoritmo de ordenacao (apoio a analise de desempenho). */
public class EstatisticaOrdenacao {
    /** Quantidade de comparacoes entre dois registros. */
    public long comparacoes;
    /** Trocas (bubble/selection), deslocamentos (insertion) ou copias (merge). */
    public long movimentos;
}

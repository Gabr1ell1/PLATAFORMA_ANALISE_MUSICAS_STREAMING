package servico;
import servico.EstatisticaOrdenacao;
import servico.Desempenho;
import persistencia.GeradorDados;
import Ordenacao.Algoritmo;
import model.Criterio;
import model.Musica;
import Ordenacao.Ordenacao;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Comparativo de desempenho: executa os quatro algoritmos com conjuntos
 * equivalentes (mesma semente, mesmo criterio) em varios tamanhos, confere se
 * todos produzem o mesmo resultado e grava benchmark.csv e desempenho.log.
 */
public final class Benchmark {

    public static final String ARQUIVO_CSV = "benchmark.csv";
    private static final int REPETICOES = 3;
    private static final long SEMENTE = 42;

    private Benchmark() {
    }

    public static void executar(int[] tamanhos, Criterio criterio) throws IOException {
        aquecer(criterio);

        System.out.println("\nCriterio: " + criterio.getDescricao() + " | média de " + REPETICOES
                + " execuções | semente " + SEMENTE);
        System.out.printf("%-8s %-14s %14s %14s %12s%n", "n", "algoritmo", "comparacoes", "movimentos", "tempo (ms)");
        System.out.println("-".repeat(66));

        try (BufferedWriter csv = Files.newBufferedWriter(Paths.get(ARQUIVO_CSV), StandardCharsets.UTF_8)) {
            csv.write("n,algoritmo,comparacoes,movimentos,tempo_ms");
            csv.newLine();

            for (int n : tamanhos) {
                Musica[] base = GeradorDados.gerar(n, SEMENTE);
                Musica[] referencia = null;

                for (Algoritmo algoritmo : Algoritmo.values()) {
                    long total_nanos = 0;
                    EstatisticaOrdenacao estatistica = null;
                    Musica[] resultado = null;

                    for (int r = 0; r < REPETICOES; r++) {
                        resultado = base.clone();
                        estatistica = new EstatisticaOrdenacao();
                        long inicio = System.nanoTime();
                        algoritmo.ordenar(resultado, criterio, estatistica);
                        total_nanos += System.nanoTime() - inicio;
                    }

                    if (!Ordenacao.estaOrdenado(resultado, criterio)) {
                        throw new IllegalStateException(algoritmo + " nao ordenou corretamente (n=" + n + ")");
                    }
                    if (referencia == null) {
                        referencia = resultado;
                    } else {
                        for (int i = 0; i < n; i++) {
                            if (referencia[i].getId() != resultado[i].getId()) {
                                throw new IllegalStateException("Resultados divergentes entre algoritmos (n=" + n + ")");
                            }
                        }
                    }

                    long media_nanos = total_nanos / REPETICOES;
                    double ms = Desempenho.paraMs(media_nanos);
                    System.out.printf(Locale.US, "%-8d %-14s %,14d %,14d %12.3f%n",
                            n, algoritmo.getNomeCurto(), estatistica.comparacoes, estatistica.movimentos, ms);
                    csv.write(String.format(Locale.US, "%d,%s,%d,%d,%.3f",
                            n, algoritmo.getNomeCurto(), estatistica.comparacoes, estatistica.movimentos, ms));
                    csv.newLine();
                    Desempenho.registrar("benchmark_" + algoritmo.getNomeCurto(), n, media_nanos,
                            "criterio=" + criterio.name() + " comparacoes=" + estatistica.comparacoes
                                    + " movimentos=" + estatistica.movimentos);
                }
                System.out.println();
            }
        }
        System.out.println("Resultados gravados em " + ARQUIVO_CSV + " e " + Desempenho.ARQUIVO_LOG
                + ". Todos os algoritmos geraram a mesma ordenacao (verificado).");
    }

    /** Execucoes de aquecimento para que a JVM compile o codigo antes das medicoes. */
    private static void aquecer(Criterio criterio) {
        Musica[] base = GeradorDados.gerar(2000, 7);
        for (int i = 0; i < 3; i++) {
            for (Algoritmo algoritmo : Algoritmo.values()) {
                algoritmo.ordenar(base.clone(), criterio, new EstatisticaOrdenacao());
            }
        }
    }
}

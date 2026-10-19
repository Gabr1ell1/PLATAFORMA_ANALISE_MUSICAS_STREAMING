package servico;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Registro de desempenho: grava no arquivo desempenho.log o tempo de cada operacao. */
public final class Desempenho {

    public static final String ARQUIVO_LOG = "desempenho.log";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Desempenho() {
    }

    public static double paraMs(long nanos) {
        return nanos / 1_000_000.0;
    }

    /** Acrescenta uma linha ao log: data | operacao | tamanho do conjunto | tempo em ms | detalhes. */
    public static void registrar(String operacao, int tamanho, long nanos, String detalhe) {
        String linha = String.format(Locale.US, "%s | operacao=%s | n=%d | tempo_ms=%.3f | %s",
                LocalDateTime.now().format(FORMATO), operacao, tamanho, paraMs(nanos), detalhe);
        try (BufferedWriter escritor = Files.newBufferedWriter(Paths.get(ARQUIVO_LOG),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            escritor.write(linha);
            escritor.newLine();
        } catch (IOException e) {
            System.err.println("Não foi possível gravar o log: " + e.getMessage());
        }
    }
}

package persistencia;
import estrutura.ListaDeque;
import model.Criterio;
import model.Musica;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia em arquivos de texto (entrada.txt e arquivos de saida).
 * A gravacao e feita em arquivo temporario e depois renomeada, para que uma
 * falha no meio do processo nao corrompa os dados existentes (RNF03).
Método	O que faz
criarVazio()	Cria o arquivo com o cabeçalho, sem registros.
salvar()	Grava as músicas no arquivo de texto.
salvarLista()	Grava os elementos de uma ListaDeque.
ler()	Lê os registros do arquivo e retorna um vetor de músicas.
carregarLista()	Carrega os registros do arquivo para a lista ligada/deque.
lerCriterio()	Identifica o critério de ordenação registrado no cabeçalho.
*/
public final class ArquivoMusicas {

    public static final String CABECALHO =
            "# ordem;id;titulo;artista;album;genero;duracao_s;ano;pais;reproducoes;avaliacao;atualizado_em;status";
    private static final String PREFIXO_CRITERIO = "# criterio=";

    private ArquivoMusicas() {
    }

    public static boolean existe(String caminho) {
        return Files.exists(Paths.get(caminho));
    }

    /** Cria (ou zera) o arquivo apenas com o cabecalho do leiaute. */
    public static void criarVazio(String caminho) throws IOException {
        salvar(caminho, new Musica[0], null);
    }

    /** Grava os registros; se criterio != null, registra no cabecalho como o arquivo foi ordenado. */
    public static void salvar(String caminho, Musica[] registros, Criterio criterio) throws IOException {
        Path destino = Paths.get(caminho);
        Path temporario = Paths.get(caminho + ".tmp");
        try (BufferedWriter escritor = Files.newBufferedWriter(temporario, StandardCharsets.UTF_8)) {
            escritor.write(CABECALHO);
            escritor.newLine();
            if (criterio != null) {
                escritor.write(PREFIXO_CRITERIO + criterio.name());
                escritor.newLine();
            }
            for (Musica m : registros) {
                escritor.write(m.paraLinha());
                escritor.newLine();
            }
        }
        Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void salvarLista(String caminho, ListaDeque lista) throws IOException {
        salvar(caminho, lista.paraArray(), null);
    }

    /** Le todos os registros do arquivo, ignorando linhas de comentario (#) e em branco. */
    public static Musica[] ler(String caminho) throws IOException {
        List<Musica> registros = new ArrayList<>();
        try (BufferedReader leitor = Files.newBufferedReader(Paths.get(caminho), StandardCharsets.UTF_8)) {
            String linha;
            int numero_linha = 0;
            while ((linha = leitor.readLine()) != null) {
                numero_linha++;
                if (linha.isBlank() || linha.startsWith("#")) {
                    continue;
                }
                try {
                    registros.add(Musica.deLinha(linha));
                } catch (RuntimeException e) {
                    throw new IOException("Linha " + numero_linha + " de " + caminho
                            + " inválida (" + e.getMessage() + ")");
                }
            }
        }
        return registros.toArray(new Musica[0]);
    }

    /** Carrega o arquivo para a lista ligada/deque, preservando a ordem do arquivo. */
    public static ListaDeque carregarLista(String caminho) throws IOException {
        ListaDeque lista = new ListaDeque();
        for (Musica m : ler(caminho)) {
            lista.anexarCarregado(m);
        }
        return lista;
    }

    /** Descobre pelo cabecalho se o arquivo esta ordenado e por qual criterio (null = nao ordenado). */
    public static Criterio lerCriterio(String caminho) throws IOException {
        try (BufferedReader leitor = Files.newBufferedReader(Paths.get(caminho), StandardCharsets.UTF_8)) {
            String linha;
            while ((linha = leitor.readLine()) != null && linha.startsWith("#")) {
                if (linha.startsWith(PREFIXO_CRITERIO)) {
                    return Criterio.valueOf(linha.substring(PREFIXO_CRITERIO.length()).trim());
                }
            }
        }
        return null;
    }
}

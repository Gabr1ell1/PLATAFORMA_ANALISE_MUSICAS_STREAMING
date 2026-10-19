package TabelaHash;
import application.Texto;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tabela hash (encadeamento separado) que associa cada pais a lista de paises
 * com os quais faz fronteira. A chave e normalizada (sem acento/caixa), logo
 * "Brasil", "brasil" e "BRASIL" sao o mesmo pais; "Colômbia" = "colombia".
 * A tabela dobra de tamanho quando o fator de carga passa de 0,75.
 */
public class TabelaHashPaises {

    private static class Entrada {
        final String chave;
        String nome;
        List<String> fronteiras;
        Entrada proxima;

        Entrada(String chave, String nome, List<String> fronteiras) {
            this.chave = chave;
            this.nome = nome;
            this.fronteiras = fronteiras;
        }
    }

    private static final double FATOR_CARGA_MAXIMO = 0.75;

    private Entrada[] tabela = new Entrada[16];
    private int tamanho = 0;

    /** Funcao hash polinomial (base 31) reduzida ao tamanho da tabela. */
    private static int hash(String chave, int capacidade) {
        int h = 7;
        for (int i = 0; i < chave.length(); i++) {
            h = 31 * h + chave.charAt(i);
        }
        return Math.floorMod(h, capacidade);
    }

    /** Insere o pais ou, se ja existir, substitui sua lista de fronteiras. */
    public void inserir(String nome, List<String> fronteiras) {
        String chave = Texto.chave(nome);
        if (chave.isEmpty()) {
            throw new IllegalArgumentException("Nome de país inválido.");
        }
        int posicao = hash(chave, tabela.length);
        for (Entrada e = tabela[posicao]; e != null; e = e.proxima) {
            if (e.chave.equals(chave)) {
                e.nome = nome.trim();
                e.fronteiras = new ArrayList<>(fronteiras);
                return;
            }
        }
        Entrada nova = new Entrada(chave, nome.trim(), new ArrayList<>(fronteiras));
        nova.proxima = tabela[posicao];
        tabela[posicao] = nova;
        tamanho++;
        if (tamanho > FATOR_CARGA_MAXIMO * tabela.length) {
            redimensionar();
        }
    }

    /** Retorna as fronteiras do pais, ou null se o pais nao estiver na tabela. */
    public List<String> buscar(String nome) {
        String chave = Texto.chave(nome);
        for (Entrada e = tabela[hash(chave, tabela.length)]; e != null; e = e.proxima) {
            if (e.chave.equals(chave)) {
                return Collections.unmodifiableList(e.fronteiras);
            }
        }
        return null;
    }

    public boolean remover(String nome) {
        String chave = Texto.chave(nome);
        int posicao = hash(chave, tabela.length);
        Entrada anterior = null;
        for (Entrada e = tabela[posicao]; e != null; anterior = e, e = e.proxima) {
            if (e.chave.equals(chave)) {
                if (anterior == null) {
                    tabela[posicao] = e.proxima;
                } else {
                    anterior.proxima = e.proxima;
                }
                tamanho--;
                return true;
            }
        }
        return false;
    }

    private void redimensionar() {
        Entrada[] antiga = tabela;
        tabela = new Entrada[antiga.length * 2];
        for (Entrada cabeca : antiga) {
            Entrada e = cabeca;
            while (e != null) {
                Entrada proxima = e.proxima;
                int posicao = hash(e.chave, tabela.length);
                e.proxima = tabela[posicao];
                tabela[posicao] = e;
                e = proxima;
            }
        }
    }

    public int tamanho() {
        return tamanho;
    }

    public int capacidade() {
        return tabela.length;
    }

    /** Nomes de todos os paises, em ordem alfabetica (apenas para exibicao). */
    public List<String> nomes() {
        List<String> nomes = new ArrayList<>();
        for (Entrada cabeca : tabela) {
            for (Entrada e = cabeca; e != null; e = e.proxima) {
                nomes.add(e.nome);
            }
        }
        nomes.sort((a, b) -> Texto.chave(a).compareTo(Texto.chave(b)));
        return nomes;
    }

    // ---------- persistencia: uma linha por pais -> "Pais;vizinho1,vizinho2" ----------

    public void salvar(String caminho) throws IOException {
        try (BufferedWriter escritor = Files.newBufferedWriter(Paths.get(caminho), StandardCharsets.UTF_8)) {
            for (String nome : nomes()) {
                escritor.write(nome + ";" + String.join(",", buscar(nome)));
                escritor.newLine();
            }
        }
    }

    public static TabelaHashPaises carregar(String caminho) throws IOException {
        TabelaHashPaises tabela = new TabelaHashPaises();
        try (BufferedReader leitor = Files.newBufferedReader(Paths.get(caminho), StandardCharsets.UTF_8)) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                if (linha.isBlank()) {
                    continue;
                }
                String[] partes = linha.split(";", -1);
                List<String> vizinhos = new ArrayList<>();
                if (partes.length > 1 && !partes[1].isBlank()) {
                    for (String v : partes[1].split(",")) {
                        vizinhos.add(v.trim());
                    }
                }
                tabela.inserir(partes[0], vizinhos);
            }
        }
        return tabela;
    }

    /** Tabela inicial com os dados de Geografia.FRONTEIRAS. */
    public static TabelaHashPaises padrao() {
        TabelaHashPaises tabela = new TabelaHashPaises();
        for (String[] linha : Geografia.FRONTEIRAS) {
            List<String> vizinhos = new ArrayList<>();
            for (int i = 1; i < linha.length; i++) {
                vizinhos.add(linha[i]);
            }
            tabela.inserir(linha[0], vizinhos);
        }
        return tabela;
    }
}

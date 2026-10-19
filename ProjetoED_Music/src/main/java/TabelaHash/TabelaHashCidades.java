package TabelaHash;
import application.Texto;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabela hash de cidades cuja funcao hash PRESERVA A ORDEM ALFABETICA.
 *
 * Funcao hash: cada um dos 3 primeiros caracteres da chave normalizada recebe
 * um valor (espaco/fim de texto = 0, 'a' = 1, ..., 'z' = 26), formando um numero
 * na base 27:   v = c1*27^2 + c2*27 + c3        (0 <= v < 27^3 = 19683)
 * O indice do balde e  floor(v * capacidade / 19683).
 *
 * Garantia: se a vem antes de b alfabeticamente, entao v(a) <= v(b) (a
 * comparacao lexicografica dos 3 primeiros caracteres equivale a comparar v) e,
 * como a escala e monotona, hash(a) <= hash(b). Cidades que caem no mesmo balde
 * (mesmo prefixo de 3 letras) ficam numa lista ligada mantida ORDENADA por
 * insercao. Assim, percorrer os baldes de 0 ate capacidade-1 devolve as cidades
 * ja em ordem alfabetica, sem nenhum algoritmo de ordenacao posterior.
 */
public class TabelaHashCidades {

    private static class Entrada {
        final String chave;   // normalizada: usada na comparacao
        final String cidade;  // nome original (com acentos)
        final String pais;
        Entrada proxima;

        Entrada(String chave, String cidade, String pais) {
            this.chave = chave;
            this.cidade = cidade;
            this.pais = pais;
        }
    }

    private static final int BASE = 27;
    private static final int LETRAS_NO_PREFIXO = 3;
    private static final int LIMITE = BASE * BASE * BASE; // 19683

    private final Entrada[] tabela;
    private int tamanho = 0;

    public TabelaHashCidades(int capacidade) {
        if (capacidade < 1) {
            throw new IllegalArgumentException("Capacidade deve ser positiva.");
        }
        tabela = new Entrada[capacidade];
    }

    public TabelaHashCidades() {
        this(1024);
    }

    private static int valorLetra(char c) {
        return (c >= 'a' && c <= 'z') ? (c - 'a' + 1) : 0;
    }

    /** Funcao hash monotona (nao-decrescente) em relacao a ordem alfabetica. */
    int hash(String chave) {
        int valor = 0;
        for (int i = 0; i < LETRAS_NO_PREFIXO; i++) {
            int letra = i < chave.length() ? valorLetra(chave.charAt(i)) : 0;
            valor = valor * BASE + letra;
        }
        return (int) ((long) valor * tabela.length / LIMITE);
    }

    /** Insere mantendo a lista do balde ordenada. Retorna false se ja existir (mesma cidade e pais). */
    public boolean inserir(String cidade, String pais) {
        String chave = Texto.chave(cidade);
        if (chave.isEmpty()) {
            throw new IllegalArgumentException("Nome de cidade inválido.");
        }
        String chave_pais = Texto.chave(pais);
        int posicao = hash(chave);

        Entrada anterior = null;
        Entrada atual = tabela[posicao];
        while (atual != null) {
            int cmp = atual.chave.compareTo(chave);
            if (cmp == 0) {
                cmp = Texto.chave(atual.pais).compareTo(chave_pais);
            }
            if (cmp == 0) {
                return false;
            }
            if (cmp > 0) {
                break;
            }
            anterior = atual;
            atual = atual.proxima;
        }
        Entrada nova = new Entrada(chave, cidade.trim(), pais.trim());
        nova.proxima = atual;
        if (anterior == null) {
            tabela[posicao] = nova;
        } else {
            anterior.proxima = nova;
        }
        tamanho++;
        return true;
    }

    /** Busca todas as cidades com esse nome (podem existir em paises diferentes). Retorna {cidade, pais}. */
    public List<String[]> buscar(String cidade) {
        String chave = Texto.chave(cidade);
        List<String[]> resultado = new ArrayList<>();
        for (Entrada e = tabela[hash(chave)]; e != null; e = e.proxima) {
            if (e.chave.equals(chave)) {
                resultado.add(new String[] {e.cidade, e.pais});
            }
        }
        return resultado;
    }

    /** Percorre baldes de 0 a n-1: a saida ja sai em ordem alfabetica. Retorna {cidade, pais}. */
    public List<String[]> listarEmOrdem() {
        List<String[]> lista = new ArrayList<>();
        for (Entrada cabeca : tabela) {
            for (Entrada e = cabeca; e != null; e = e.proxima) {
                lista.add(new String[] {e.cidade, e.pais});
            }
        }
        return lista;
    }

    /** Confere a propriedade principal: a varredura da tabela esta mesmo em ordem alfabetica. */
    public boolean estaOrdenada() {
        String anterior = "";
        for (Entrada cabeca : tabela) {
            for (Entrada e = cabeca; e != null; e = e.proxima) {
                if (e.chave.compareTo(anterior) < 0) {
                    return false;
                }
                anterior = e.chave;
            }
        }
        return true;
    }

    public int tamanho() {
        return tamanho;
    }

    public int capacidade() {
        return tabela.length;
    }

    // ---------- persistencia: uma linha por cidade -> "Cidade;Pais" ----------

    public void salvar(String caminho) throws IOException {
        try (BufferedWriter escritor = Files.newBufferedWriter(Paths.get(caminho), StandardCharsets.UTF_8)) {
            for (String[] c : listarEmOrdem()) {
                escritor.write(c[0] + ";" + c[1]);
                escritor.newLine();
            }
        }
    }

    public static TabelaHashCidades carregar(String caminho) throws IOException {
        TabelaHashCidades tabela = new TabelaHashCidades();
        try (BufferedReader leitor = Files.newBufferedReader(Paths.get(caminho), StandardCharsets.UTF_8)) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                String[] partes = linha.split(";", -1);
                if (partes.length == 2 && !partes[0].isBlank()) {
                    tabela.inserir(partes[0], partes[1]);
                }
            }
        }
        return tabela;
    }

    /** Tabela inicial com os dados de Geografia.CIDADES. */
    public static TabelaHashCidades padrao() {
        TabelaHashCidades tabela = new TabelaHashCidades();
        for (String[] linha : Geografia.CIDADES) {
            for (int i = 1; i < linha.length; i++) {
                tabela.inserir(linha[i], linha[0]);
            }
        }
        return tabela;
    }
}

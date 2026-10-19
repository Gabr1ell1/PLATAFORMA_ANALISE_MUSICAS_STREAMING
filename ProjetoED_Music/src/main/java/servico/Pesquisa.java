package servico;
import application.Texto;
import model.Musica;
import java.util.ArrayList;
import java.util.List;

/** Metodos de pesquisa sobre vetores de Musica lidos dos arquivos. */
public final class Pesquisa {

    private Pesquisa() {
    }

    /** Busca sequencial por ID: serve para arquivos em qualquer ordem. Retorna o indice ou -1. */
    public static int buscaSequencialPorId(Musica[] vetor, int id) {
        for (int i = 0; i < vetor.length; i++) {
            if (vetor[i].getId() == id) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Busca binaria por ID: so e valida se o vetor estiver ordenado por ID crescente
     * (Criterio.ID_ASC). Retorna o indice ou -1.
     */
    public static int buscaBinariaPorId(Musica[] vetor, int id) {
        int baixo = 0;
        int alto = vetor.length - 1;
        while (baixo <= alto) {
            int meio = (baixo + alto) >>> 1;
            int id_meio = vetor[meio].getId();
            if (id_meio == id) {
                return meio;
            } else if (id_meio < id) {
                baixo = meio + 1;
            } else {
                alto = meio - 1;
            }
        }
        return -1;
    }

    /** Busca sequencial por trecho do titulo (ignora maiusculas e acentos). */
    public static List<Musica> buscarPorTitulo(Musica[] vetor, String trecho) {
        String procurado = Texto.chave(trecho);
        List<Musica> encontrados = new ArrayList<>();
        for (Musica m : vetor) {
            if (Texto.chave(m.getTitulo()).contains(procurado)) {
                encontrados.add(m);
            }
        }
        return encontrados;
    }

    /** Busca sequencial por trecho do nome do artista (ignora maiusculas e acentos). */
    public static List<Musica> buscarPorArtista(Musica[] vetor, String trecho) {
        String procurado = Texto.chave(trecho);
        List<Musica> encontrados = new ArrayList<>();
        for (Musica m : vetor) {
            if (Texto.chave(m.getArtista()).contains(procurado)) {
                encontrados.add(m);
            }
        }
        return encontrados;
    }
}

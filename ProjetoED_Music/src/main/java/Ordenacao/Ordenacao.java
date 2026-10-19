package Ordenacao;
import model.Musica;
import model.Criterio;
import servico.EstatisticaOrdenacao;

/**
 * Quatro algoritmos de ordenacao sobre vetores de Musica, todos usando o mesmo
 * criterio (Criterio.comparar) e contabilizando comparacoes e movimentos.
 */
public final class Ordenacao {

    private Ordenacao() {
    }

    /** Ordenacao por TROCA: compara pares adjacentes e troca os que estao fora de ordem. */
    public static void bubbleSort(Musica[] vetor, Criterio criterio, EstatisticaOrdenacao estatistica) {
        int n = vetor.length;
        for (int i = 0; i < n - 1; i++) {
            boolean houve_troca = false;
            for (int j = 0; j < n - 1 - i; j++) {
                estatistica.comparacoes++;
                if (criterio.comparar(vetor[j], vetor[j + 1]) > 0) {
                    Musica auxiliar = vetor[j];
                    vetor[j] = vetor[j + 1];
                    vetor[j + 1] = auxiliar;
                    estatistica.movimentos++;
                    houve_troca = true;
                }
            }
            if (!houve_troca) {
                break; // vetor ja ordenado: encerra antecipadamente
            }
        }
    }

    /** Ordenacao por INSERCAO: insere cada elemento na posicao correta do trecho ja ordenado. */
    public static void insertionSort(Musica[] vetor, Criterio criterio, EstatisticaOrdenacao estatistica) {
        for (int i = 1; i < vetor.length; i++) {
            Musica chave = vetor[i];
            int j = i - 1;
            while (j >= 0) {
                estatistica.comparacoes++;
                if (criterio.comparar(vetor[j], chave) <= 0) {
                    break;
                }
                vetor[j + 1] = vetor[j];
                estatistica.movimentos++;
                j--;
            }
            vetor[j + 1] = chave;
        }
    }

    /** Ordenacao por SELECAO: seleciona o menor elemento do trecho restante e o leva para a posicao i. */
    public static void selectionSort(Musica[] vetor, Criterio criterio, EstatisticaOrdenacao estatistica) {
        int n = vetor.length;
        for (int i = 0; i < n - 1; i++) {
            int indice_menor = i;
            for (int j = i + 1; j < n; j++) {
                estatistica.comparacoes++;
                if (criterio.comparar(vetor[j], vetor[indice_menor]) < 0) {
                    indice_menor = j;
                }
            }
            if (indice_menor != i) {
                Musica auxiliar = vetor[i];
                vetor[i] = vetor[indice_menor];
                vetor[indice_menor] = auxiliar;
                estatistica.movimentos++;
            }
        }
    }

    /** INTERCALACAO (Merge Sort): divide ao meio, ordena cada metade e intercala os resultados. */
    public static void mergeSort(Musica[] vetor, Criterio criterio, EstatisticaOrdenacao estatistica) {
        if (vetor.length < 2) {
            return;
        }
        Musica[] auxiliar = new Musica[vetor.length];
        dividir(vetor, auxiliar, 0, vetor.length, criterio, estatistica);
    }

    // intervalo semiaberto [inicio, fim)
    private static void dividir(Musica[] vetor, Musica[] auxiliar, int inicio, int fim,
                                Criterio criterio, EstatisticaOrdenacao estatistica) {
        if (fim - inicio < 2) {
            return;
        }
        int meio = (inicio + fim) / 2;
        dividir(vetor, auxiliar, inicio, meio, criterio, estatistica);
        dividir(vetor, auxiliar, meio, fim, criterio, estatistica);
        intercalar(vetor, auxiliar, inicio, meio, fim, criterio, estatistica);
    }

    private static void intercalar(Musica[] vetor, Musica[] auxiliar, int inicio, int meio, int fim,
                                   Criterio criterio, EstatisticaOrdenacao estatistica) {
        int esquerda = inicio;
        int direita = meio;
        int destino = inicio;
        while (esquerda < meio && direita < fim) {
            estatistica.comparacoes++;
            if (criterio.comparar(vetor[esquerda], vetor[direita]) <= 0) {
                auxiliar[destino++] = vetor[esquerda++];
            } else {
                auxiliar[destino++] = vetor[direita++];
            }
            estatistica.movimentos++;
        }
        while (esquerda < meio) {
            auxiliar[destino++] = vetor[esquerda++];
            estatistica.movimentos++;
        }
        while (direita < fim) {
            auxiliar[destino++] = vetor[direita++];
            estatistica.movimentos++;
        }
        System.arraycopy(auxiliar, inicio, vetor, inicio, fim - inicio);
    }

    /** Verifica se o vetor esta ordenado segundo o criterio (usado nos testes). */
    public static boolean estaOrdenado(Musica[] vetor, Criterio criterio) {
        for (int i = 1; i < vetor.length; i++) {
            if (criterio.comparar(vetor[i - 1], vetor[i]) > 0) {
                return false;
            }
        }
        return true;
    }
}

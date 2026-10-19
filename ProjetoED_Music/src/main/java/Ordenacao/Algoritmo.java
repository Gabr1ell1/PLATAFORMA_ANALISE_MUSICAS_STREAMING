package Ordenacao;
import model.Criterio;
import model.Musica;
import Ordenacao.Ordenacao;
import servico.EstatisticaOrdenacao;

/** Algoritmos de ordenacao disponiveis; o nome do arquivo de saida deriva do algoritmo. */
public enum Algoritmo {
    BUBBLE("Bubble Sort (troca)", "bubblesort"),
    INSERTION("Insertion Sort (inserção)", "insertionsort"),
    SELECTION("Selection Sort (seleção)", "selectionsort"),
    MERGE("Merge Sort (intercalação)", "mergesort");

    private final String descricao;
    private final String nome_arquivo;

    Algoritmo(String descricao, String nome_arquivo) {
        this.descricao = descricao;
        this.nome_arquivo = nome_arquivo;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Ex.: "mergesort.txt" */
    public String getArquivoSaida() {
        return nome_arquivo + ".txt";
    }

    public String getNomeCurto() {
        return nome_arquivo;
    }

    public void ordenar(Musica[] vetor, Criterio criterio, EstatisticaOrdenacao estatistica) {
        switch (this) {
            case BUBBLE -> Ordenacao.bubbleSort(vetor, criterio, estatistica);
            case INSERTION -> Ordenacao.insertionSort(vetor, criterio, estatistica);
            case SELECTION -> Ordenacao.selectionSort(vetor, criterio, estatistica);
            case MERGE -> Ordenacao.mergeSort(vetor, criterio, estatistica);
        }
    }
}

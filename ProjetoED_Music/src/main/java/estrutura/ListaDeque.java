package estrutura;

import model.Musica;

/**
 * Lista duplamente ligada usada como DEQUE (fila de duas pontas).
 *
 * Politicas adotadas (ver README):
 *  - INCLUSAO (FIFO): os registros entram pela ordem de chegada. Cada inclusao
 *    recebe um numero de ordem crescente; por padrao entra no FIM da fila, mas o
 *    deque permite incluir tambem no INICIO.
 *  - EXCLUSAO (LIFO): sai sempre o registro incluido mais recentemente
 *    (maior numero de ordem), independentemente da ponta em que foi inserido.
 */
public class ListaDeque {

    private static class No {
        Musica dado;
        No anterior;
        No proximo;

        No(Musica dado) {
            this.dado = dado;
        }
    }

    private No inicio;
    private No fim;
    private int tamanho;
    private long proxima_ordem = 1;

    public int tamanho() {
        return tamanho;
    }

    public boolean vazia() {
        return tamanho == 0;
    }

    // ---------- inclusao (deque) ----------

    public void inserirInicio(Musica musica) {
        musica.setOrdemInsercao(proxima_ordem++);
        No no = new No(musica);
        if (inicio == null) {
            inicio = fim = no;
        } else {
            no.proximo = inicio;
            inicio.anterior = no;
            inicio = no;
        }
        tamanho++;
    }

    public void inserirFim(Musica musica) {
        musica.setOrdemInsercao(proxima_ordem++);
        anexar(musica);
    }

    /** Usado ao carregar o arquivo: mantem a ordem e a posicao ja gravadas. */
    public void anexarCarregado(Musica musica) {
        if (musica.getOrdemInsercao() >= proxima_ordem) {
            proxima_ordem = musica.getOrdemInsercao() + 1;
        }
        anexar(musica);
    }

    private void anexar(Musica musica) {
        No no = new No(musica);
        if (fim == null) {
            inicio = fim = no;
        } else {
            no.anterior = fim;
            fim.proximo = no;
            fim = no;
        }
        tamanho++;
    }

    // ---------- remocao ----------

    /** Remocao nas pontas (operacoes basicas do deque). */
    public Musica removerInicio() {
        return inicio == null ? null : desligar(inicio);
    }

    public Musica removerFim() {
        return fim == null ? null : desligar(fim);
    }

    /** Politica LIFO: remove o ultimo registro incluido (maior ordem de insercao). */
    public Musica removerUltimoIncluido() {
        No mais_recente = noMaisRecente();
        return mais_recente == null ? null : desligar(mais_recente);
    }

    /** Consulta (sem remover) qual registro seria excluido pela politica LIFO. */
    public Musica espiarUltimoIncluido() {
        No mais_recente = noMaisRecente();
        return mais_recente == null ? null : mais_recente.dado;
    }

    private No noMaisRecente() {
        No escolhido = null;
        for (No atual = inicio; atual != null; atual = atual.proximo) {
            if (escolhido == null || atual.dado.getOrdemInsercao() > escolhido.dado.getOrdemInsercao()) {
                escolhido = atual;
            }
        }
        return escolhido;
    }

    private Musica desligar(No no) {
        if (no.anterior == null) {
            inicio = no.proximo;
        } else {
            no.anterior.proximo = no.proximo;
        }
        if (no.proximo == null) {
            fim = no.anterior;
        } else {
            no.proximo.anterior = no.anterior;
        }
        no.anterior = null;
        no.proximo = null;
        tamanho--;
        return no.dado;
    }

    // ---------- consulta ----------

    /** Busca sequencial pelo ID (a lista nao e ordenada). */
    public Musica buscarPorId(int id) {
        for (No atual = inicio; atual != null; atual = atual.proximo) {
            if (atual.dado.getId() == id) {
                return atual.dado;
            }
        }
        return null;
    }

    public int maiorId() {
        int maior = 0;
        for (No atual = inicio; atual != null; atual = atual.proximo) {
            maior = Math.max(maior, atual.dado.getId());
        }
        return maior;
    }

    public Musica[] paraArray() {
        Musica[] vetor = new Musica[tamanho];
        int i = 0;
        for (No atual = inicio; atual != null; atual = atual.proximo) {
            vetor[i++] = atual.dado;
        }
        return vetor;
    }
}

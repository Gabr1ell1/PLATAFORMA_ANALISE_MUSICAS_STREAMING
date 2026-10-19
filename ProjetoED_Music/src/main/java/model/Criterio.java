package model;

import model.Musica;

/**
 * Criterios de classificacao do catalogo. Todos os algoritmos usam o mesmo
 * metodo comparar(); em caso de empate o desempate e feito pelo ID, o que torna
 * a ordem final deterministica (todos os algoritmos geram arquivos identicos).
 */
public enum Criterio {
    REPRODUCOES_DESC("Reproducoes (decrescente)"),
    AVALIACAO_DESC("Avaliacao media (decrescente)"),
    ANO_ASC("Ano de lancamento (crescente)"),
    DURACAO_ASC("Duracao (crescente)"),
    ID_ASC("ID (crescente)");

    private final String descricao;

    Criterio(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Retorna valor negativo se a deve vir antes de b, positivo se depois, 0 se empate total. */
    public int comparar(Musica a, Musica b) {
        int resultado = switch (this) {
            case REPRODUCOES_DESC -> Long.compare(b.getReproducoes(), a.getReproducoes());
            case AVALIACAO_DESC -> Double.compare(b.getAvaliacaoMedia(), a.getAvaliacaoMedia());
            case ANO_ASC -> Integer.compare(a.getAnoLancamento(), b.getAnoLancamento());
            case DURACAO_ASC -> Integer.compare(a.getDuracaoSegundos(), b.getDuracaoSegundos());
            case ID_ASC -> 0;
        };
        return resultado != 0 ? resultado : Integer.compare(a.getId(), b.getId());
    }
}

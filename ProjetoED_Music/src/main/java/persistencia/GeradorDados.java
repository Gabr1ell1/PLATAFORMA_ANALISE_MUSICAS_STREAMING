package persistencia;
import model.Musica;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Gera registros musicais SIMULADOS (nao usa a API real do Spotify).
 * A mesma semente produz sempre o mesmo conjunto, o que permite comparar
 * algoritmos com dados equivalentes (RNF02).
 */
public final class GeradorDados {

    // {artista ficticio, pais de origem}
    private static final String[][] ARTISTAS = {
        {"Luna Alves", "Brasil"}, {"Os Vagalumes", "Brasil"}, {"Clara Menezes", "Brasil"},
        {"Duo Mare Alta", "Brasil"}, {"Rafael Quintana", "Argentina"}, {"Valentina Rios", "Colombia"},
        {"Los Faroles", "Mexico"}, {"Midnight Parade", "Estados Unidos"}, {"Ava Sterling", "Estados Unidos"},
        {"The Paper Kites Club", "Reino Unido"}, {"Oliver Gray", "Reino Unido"}, {"Ines Carvalho", "Portugal"},
        {"Marta Solano", "Espanha"}, {"Etienne Laurent", "Franca"}, {"Nacht & Tag", "Alemanha"},
        {"Saga Lindqvist", "Suecia"}, {"Neon Seoul", "Coreia do Sul"}, {"Aarav Mehta", "India"},
        {"Sakura Wave", "Japao"}, {"Tunde Bello", "Nigeria"}, {"Maple Harbor", "Canada"},
        {"Giulia Ferrante", "Italia"}, {"Camila Reyes", "Chile"}
    };

    private static final String[] GENEROS = {
        "Pop", "Rock", "Sertanejo", "MPB", "Hip Hop", "Eletronica", "Funk", "Jazz",
        "Indie", "Reggaeton", "Samba", "R&B", "K-Pop", "Afrobeats", "Classica"
    };

    private static final String[] PALAVRAS_1 = {
        "Noite", "Luz", "Estrada", "Mar", "Coracao", "Sonho", "Vento", "Cidade", "Fogo",
        "Chuva", "Verao", "Saudade", "Horizonte", "Estrela", "Neon", "Silencio"
    };

    private static final String[] PALAVRAS_2 = {
        "Eterna", "de Cristal", "Sem Fim", "Perdida", "em Chamas", "Distante", "Azul",
        "Dourada", "Invisivel", "da Madrugada", "Infinita", "Secreta"
    };

    private static final String[] ALBUNS = {
        "Primeiros Passos", "Linhas Cruzadas", "Rumo ao Sul", "Fases da Lua", "Cartas ao Mar",
        "Ao Vivo na Cidade", "Pequenos Milagres", "Entre Ondas", "Luz de Neon", "Raizes",
        "Frequencia", "Capítulo Dois", "Terra Firme", "Sessoes de Estudio"
    };

    private static final String[] STATUS = {"lancado", "em divulgacao", "arquivado"};

    private GeradorDados() {
    }

    /**
     * Gera n registros com IDs unicos (1..n) em ordem embaralhada, de forma que o
     * arquivo de entrada NAO esteja ordenado por nenhum criterio.
     */
    public static Musica[] gerar(int quantidade, long semente) {
        Random sorteio = new Random(semente);
        Musica[] registros = new Musica[quantidade];

        for (int i = 0; i < quantidade; i++) {
            String[] artista = ARTISTAS[sorteio.nextInt(ARTISTAS.length)];
            String titulo = PALAVRAS_1[sorteio.nextInt(PALAVRAS_1.length)] + " "
                    + PALAVRAS_2[sorteio.nextInt(PALAVRAS_2.length)];
            long reproducoes = (long) Math.exp(sorteio.nextGaussian() * 1.6 + 13.0); // cauda longa
            double avaliacao = Math.min(5.0, Math.max(1.0, 3.9 + sorteio.nextGaussian() * 0.5));
            avaliacao = Math.round(avaliacao * 100.0) / 100.0;
            LocalDateTime atualizacao = LocalDateTime.of(2026, 1, 1, 0, 0)
                    .plusMinutes(sorteio.nextInt(60 * 24 * 290));

            registros[i] = new Musica(
                    i + 1, titulo, artista[0],
                    ALBUNS[sorteio.nextInt(ALBUNS.length)],
                    GENEROS[sorteio.nextInt(GENEROS.length)],
                    120 + sorteio.nextInt(241),       // 2 a 6 minutos
                    1990 + sorteio.nextInt(37),       // 1990..2026
                    artista[1], reproducoes, avaliacao, atualizacao,
                    STATUS[sorteio.nextInt(STATUS.length)]);
        }

        // Fisher-Yates: embaralha a ordem de gravacao
        for (int i = quantidade - 1; i > 0; i--) {
            int j = sorteio.nextInt(i + 1);
            Musica auxiliar = registros[i];
            registros[i] = registros[j];
            registros[j] = auxiliar;
        }
        for (int i = 0; i < quantidade; i++) {
            registros[i].setOrdemInsercao(i + 1);
        }
        return registros;
    }
}

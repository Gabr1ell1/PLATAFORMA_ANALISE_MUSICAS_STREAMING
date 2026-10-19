package model;

import application.Texto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Registro NAO-HOMOGENEO do catalogo: reune campos de tipos diferentes
 * (long, int, String, double, LocalDateTime).
 *
 * Leiaute de uma linha em entrada.txt (separador ';'):
 * ordem;id;titulo;artista;album;genero;duracao_s;ano;pais;reproducoes;avaliacao;atualizado_em;status
 */
public final class Musica {

    public static final String SEPARADOR = ";";
    public static final int NUMERO_CAMPOS = 13;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private long ordem_insercao;          // posicao cronologica de inclusao (base da politica LIFO)
    private int id;
    private String titulo;
    private String artista;
    private String album;
    private String genero;
    private int duracao_segundos;
    private int ano_lancamento;
    private String pais_origem;
    private long reproducoes;
    private double avaliacao_media;
    private LocalDateTime data_atualizacao;
    private String status;

    public Musica(int id, String titulo, String artista, String album, String genero,
                  int duracao_segundos, int ano_lancamento, String pais_origem,
                  long reproducoes, double avaliacao_media, LocalDateTime data_atualizacao,
                  String status) {
        this.id = id;
        setTitulo(titulo);
        setArtista(artista);
        setAlbum(album);
        setGenero(genero);
        setDuracaoSegundos(duracao_segundos);
        setAnoLancamento(ano_lancamento);
        setPaisOrigem(pais_origem);
        setReproducoes(reproducoes);
        setAvaliacaoMedia(avaliacao_media);
        this.data_atualizacao = data_atualizacao.truncatedTo(ChronoUnit.SECONDS);
        setStatus(status);
    }

    // ---------- getters ----------
    public long getOrdemInsercao() { return ordem_insercao; }
    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getArtista() { return artista; }
    public String getAlbum() { return album; }
    public String getGenero() { return genero; }
    public int getDuracaoSegundos() { return duracao_segundos; }
    public int getAnoLancamento() { return ano_lancamento; }
    public String getPaisOrigem() { return pais_origem; }
    public long getReproducoes() { return reproducoes; }
    public double getAvaliacaoMedia() { return avaliacao_media; }
    public LocalDateTime getDataAtualizacao() { return data_atualizacao; }
    public String getStatus() { return status; }

    // ---------- setters (validam e saneiam os dados) ----------
    public void setOrdemInsercao(long ordem_insercao) { this.ordem_insercao = ordem_insercao; }
    public void setTitulo(String titulo) { this.titulo = Texto.limpar(titulo); }
    public void setArtista(String artista) { this.artista = Texto.limpar(artista); }
    public void setAlbum(String album) { this.album = Texto.limpar(album); }
    public void setGenero(String genero) { this.genero = Texto.limpar(genero); }
    public void setPaisOrigem(String pais_origem) { this.pais_origem = Texto.limpar(pais_origem); }
    public void setStatus(String status) { this.status = Texto.limpar(status); }

    public void setDuracaoSegundos(int duracao_segundos) {
        if (duracao_segundos <= 0) {
            throw new IllegalArgumentException("A duracao deve ser maior que zero.");
        }
        this.duracao_segundos = duracao_segundos;
    }

    public void setAnoLancamento(int ano_lancamento) {
        if (ano_lancamento < 1900 || ano_lancamento > 2100) {
            throw new IllegalArgumentException("Ano de lancamento invalido: " + ano_lancamento);
        }
        this.ano_lancamento = ano_lancamento;
    }

    public void setReproducoes(long reproducoes) {
        if (reproducoes < 0) {
            throw new IllegalArgumentException("A quantidade de reproducoes nao pode ser negativa.");
        }
        this.reproducoes = reproducoes;
    }

    public void setAvaliacaoMedia(double avaliacao_media) {
        if (avaliacao_media < 0.0 || avaliacao_media > 5.0) {
            throw new IllegalArgumentException("A avaliacao deve estar entre 0 e 5.");
        }
        this.avaliacao_media = avaliacao_media;
    }

    public void setDataAtualizacao(LocalDateTime data_atualizacao) {
        this.data_atualizacao = data_atualizacao.truncatedTo(ChronoUnit.SECONDS);
    }

    // ---------- serializacao ----------
    /** Converte o registro em uma linha do arquivo de texto. */
    public String paraLinha() {
        return String.join(SEPARADOR,
                Long.toString(ordem_insercao),
                Integer.toString(id),
                titulo, artista, album, genero,
                Integer.toString(duracao_segundos),
                Integer.toString(ano_lancamento),
                pais_origem,
                Long.toString(reproducoes),
                String.format(Locale.US, "%.2f", avaliacao_media),
                data_atualizacao.format(FORMATO_DATA),
                status);
    }

    /** Reconstroi o registro a partir de uma linha do arquivo. */
    public static Musica deLinha(String linha) {
        String[] c = linha.split(SEPARADOR, -1);
        if (c.length != NUMERO_CAMPOS) {
            throw new IllegalArgumentException(
                    "esperados " + NUMERO_CAMPOS + " campos, encontrados " + c.length);
        }
        Musica m = new Musica(
                Integer.parseInt(c[1].trim()), c[2], c[3], c[4], c[5],
                Integer.parseInt(c[6].trim()), Integer.parseInt(c[7].trim()), c[8],
                Long.parseLong(c[9].trim()), Double.parseDouble(c[10].trim()),
                LocalDateTime.parse(c[11].trim(), FORMATO_DATA), c[12]);
        m.setOrdemInsercao(Long.parseLong(c[0].trim()));
        return m;
    }
}

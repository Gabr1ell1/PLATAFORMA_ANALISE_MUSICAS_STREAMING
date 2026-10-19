package application;
import servico.Benchmark;
import servico.EstatisticaOrdenacao;
import servico.Desempenho;
import servico.Pesquisa;
import persistencia.GeradorDados;
import persistencia.ArquivoMusicas;
import Ordenacao.Algoritmo;
import estrutura.ListaDeque;
import model.Criterio;
import model.Musica;
import TabelaHash.TabelaHashPaises;
import TabelaHash.TabelaHashCidades;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Plataforma de Analise de Musicas em Streaming (simulacao local).
 * Menu principal: CRUD sobre entrada.txt (lista/deque), ordenacao, pesquisa,
 * tabelas hash e relatorio de desempenho.
 */
public class Main {

    private static final String ARQUIVO_ENTRADA = "entrada.txt";
    private static final String ARQUIVO_PAISES = "fronteiras.txt";
    private static final String ARQUIVO_CIDADES = "cidades.txt";
    private static final String[] STATUS_VALIDOS = {"lancado", "em divulgacao", "arquivado"};
    private static final long SEMENTE_PADRAO = 42;

    private static final Scanner teclado = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Plataforma de Analise de Musicas em Streaming (simulacao) ===");
        try {
            if (!ArquivoMusicas.existe(ARQUIVO_ENTRADA)) {
                ArquivoMusicas.criarVazio(ARQUIVO_ENTRADA);
                System.out.println("Arquivo " + ARQUIVO_ENTRADA
                        + " criado vazio. Use a opção 1 para gerar dados simulados.");
            }
        } catch (IOException e) {
            System.out.println("Erro ao preparar " + ARQUIVO_ENTRADA + ": " + e.getMessage());
            return;
        }

        boolean continuar = true;
        while (continuar) {
            exibirMenu();
            int opcao = lerInteiro("Opcao", -1);
            try {
                switch (opcao) {
                    case 1 -> criarArquivo();
                    case 2 -> cadastrar();
                    case 3 -> listar();
                    case 4 -> atualizar();
                    case 5 -> excluir();
                    case 6 -> ordenar();
                    case 7 -> pesquisar();
                    case 8 -> menuPaises();
                    case 9 -> menuCidades();
                    case 10 -> executarBenchmark();
                    case 0 -> continuar = false;
                    default -> System.out.println("Opcao invalida.");
                }
            } catch (IOException | RuntimeException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
        System.out.println("Ate logo! Os tempos ficaram registrados em " + Desempenho.ARQUIVO_LOG + ".");
    }

    private static void exibirMenu() {
        System.out.println("\n--- MENU ---");
        System.out.println(" 1) Create  - criar entrada.txt (dados simulados ou vazio)");
        System.out.println(" 2) Create  - cadastrar musica (deque: inicio ou fim)");
        System.out.println(" 3) Read    - listar registros de entrada.txt");
        System.out.println(" 4) Update  - alterar uma musica");
        System.out.println(" 5) Delete  - remover um registro (politica LIFO)");
        System.out.println(" 6) Ordenar - bubble, insertion, selection, merge");
        System.out.println(" 7) Pesquisar musicas (entrada ou saida ordenada)");
        System.out.println(" 8) Tabela hash: paises e fronteiras");
        System.out.println(" 9) Tabela hash: cidades em ordem alfabetica");
        System.out.println("10) Benchmark de desempenho dos algoritmos");
        System.out.println(" 0) Sair");
    }

    // ================= entrada de dados =================

    private static String lerLinha() {
        if (!teclado.hasNextLine()) {
            System.out.println("\nEntrada encerrada.");
            System.exit(0);
        }
        return teclado.nextLine().trim();
    }

    private static int lerInteiro(String rotulo, int padrao) {
        while (true) {
            System.out.print(rotulo + (padrao >= 0 ? " [" + padrao + "]" : "") + ": ");
            String texto = lerLinha();
            if (texto.isEmpty() && padrao >= 0) {
                return padrao;
            }
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Digite um numero inteiro.");
            }
        }
    }

    private static long lerLongo(String rotulo, long atual) {
        while (true) {
            System.out.print(rotulo + (atual >= 0 ? " [" + atual + "]" : "") + ": ");
            String texto = lerLinha();
            if (texto.isEmpty() && atual >= 0) {
                return atual;
            }
            try {
                return Long.parseLong(texto);
            } catch (NumberFormatException e) {
                System.out.println("Digite um numero inteiro.");
            }
        }
    }

    private static double lerReal(String rotulo, double atual) {
        while (true) {
            System.out.print(rotulo + (atual >= 0 ? String.format(Locale.US, " [%.2f]", atual) : "") + ": ");
            String texto = lerLinha();
            if (texto.isEmpty() && atual >= 0) {
                return atual;
            }
            try {
                return Double.parseDouble(texto.replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Digite um numero (ex.: 4.5).");
            }
        }
    }

    /** Pede texto; Enter mantem o valor atual (se houver). */
    private static String lerTexto(String rotulo, String atual) {
        while (true) {
            System.out.print(rotulo + (atual != null ? " [" + atual + "]" : "") + ": ");
            String texto = lerLinha();
            if (texto.isEmpty() && atual != null) {
                return atual;
            }
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("Campo obrigatorio.");
        }
    }

    private static boolean confirmar(String pergunta) {
        System.out.print(pergunta + " (s/n): ");
        return lerLinha().toLowerCase(Locale.ROOT).startsWith("s");
    }

    private static String escolherStatus(String atual) {
        System.out.println("Status: 1) lancado  2) em divulgacao  3) arquivado");
        int padrao = 1;
        for (int i = 0; i < STATUS_VALIDOS.length; i++) {
            if (STATUS_VALIDOS[i].equals(atual)) {
                padrao = i + 1;
            }
        }
        while (true) {
            int escolha = lerInteiro("Status", padrao);
            if (escolha >= 1 && escolha <= 3) {
                return STATUS_VALIDOS[escolha - 1];
            }
            System.out.println("Escolha 1, 2 ou 3.");
        }
    }

    private static Criterio escolherCriterio() {
        Criterio[] criterios = Criterio.values();
        System.out.println("Criterio de classificacao:");
        for (int i = 0; i < criterios.length; i++) {
            System.out.println("  " + (i + 1) + ") " + criterios[i].getDescricao());
        }
        while (true) {
            int escolha = lerInteiro("Criterio", 1);
            if (escolha >= 1 && escolha <= criterios.length) {
                return criterios[escolha - 1];
            }
            System.out.println("Opcao invalida.");
        }
    }

    // ================= CRUD =================

    /** RF01: cria entrada.txt com dados simulados ou apenas com o leiaute. */
    private static void criarArquivo() throws IOException {
        if (ArquivoMusicas.existe(ARQUIVO_ENTRADA) && ArquivoMusicas.ler(ARQUIVO_ENTRADA).length > 0
                && !confirmar("entrada.txt ja contem registros e sera substituido. Continuar?")) {
            System.out.println("Operacao cancelada.");
            return;
        }
        System.out.println("1) Gerar registros simulados   2) Arquivo vazio (somente leiaute)");
        int modo = lerInteiro("Modo", 1);

        long inicio = System.nanoTime();
        int quantidade = 0;
        if (modo == 2) {
            ArquivoMusicas.criarVazio(ARQUIVO_ENTRADA);
        } else {
            quantidade = lerInteiro("Quantidade de registros", 1000);
            if (quantidade < 1) {
                System.out.println("Quantidade invalida.");
                return;
            }
            inicio = System.nanoTime(); // nao conta o tempo de digitacao
            ArquivoMusicas.salvar(ARQUIVO_ENTRADA, GeradorDados.gerar(quantidade, SEMENTE_PADRAO), null);
        }
        long tempo = System.nanoTime() - inicio;
        Desempenho.registrar("create_arquivo", quantidade, tempo, "arquivo=" + ARQUIVO_ENTRADA);
        System.out.printf(Locale.US, "%s criado com %d registro(s) em %.3f ms.%n",
                ARQUIVO_ENTRADA, quantidade, Desempenho.paraMs(tempo));
    }

    /** RF01/RF05: inclusao de nova musica no inicio ou no fim do deque. */
    private static void cadastrar() throws IOException {
        ListaDeque lista = ArquivoMusicas.carregarLista(ARQUIVO_ENTRADA);

        System.out.println("Cadastro de musica (politica de inclusão FIFO: por padrao entra no fim da fila)");
        int id_sugerido = lista.maiorId() + 1;
        int id;
        while (true) {
            id = lerInteiro("ID", id_sugerido);
            if (id > 0 && lista.buscarPorId(id) == null) {
                break;
            }
            System.out.println("ID invalido ou ja existente.");
        }
        String titulo = lerTexto("Titulo", null);
        String artista = lerTexto("Artista", null);
        String album = lerTexto("Album", null);
        String genero = lerTexto("Genero", null);
        String pais = lerTexto("Pais de origem do artista", null);
        Musica nova = null;
        while (nova == null) {
            try {
                nova = new Musica(id, titulo, artista, album, genero,
                        lerInteiro("Duracao (segundos)", -1),
                        lerInteiro("Ano de lancamento", LocalDateTime.now().getYear()),
                        pais,
                        lerLongo("Reproducoes", 0),
                        lerReal("Avaliacao media (0 a 5)", 0.0),
                        LocalDateTime.now(),
                        escolherStatus("lancado"));
            } catch (IllegalArgumentException e) {
                System.out.println("Dado invalido: " + e.getMessage() + " Digite novamente os valores numericos.");
            }
        }
        System.out.println("Onde incluir? 1) Fim (padrão FIFO)   2) Início (deque)");
        int ponta = lerInteiro("Posicao", 1);

        long inicio = System.nanoTime();
        lista = ArquivoMusicas.carregarLista(ARQUIVO_ENTRADA);
        if (ponta == 2) {
            lista.inserirInicio(nova);
        } else {
            lista.inserirFim(nova);
        }
        ArquivoMusicas.salvarLista(ARQUIVO_ENTRADA, lista);
        long tempo = System.nanoTime() - inicio;

        String operacao = ponta == 2 ? "inclusao_inicio" : "inclusao_fim";
        Desempenho.registrar(operacao, lista.tamanho(), tempo, "id=" + id);
        System.out.printf(Locale.US, "Musica %d incluida no %s (ordem de inclusao %d). Tempo: %.3f ms%n",
                id, ponta == 2 ? "início" : "fim", nova.getOrdemInsercao(), Desempenho.paraMs(tempo));
    }

    /** RF02: le e exibe os registros de entrada.txt. */
    private static void listar() throws IOException {
        long inicio = System.nanoTime();
        Musica[] registros = ArquivoMusicas.ler(ARQUIVO_ENTRADA);
        long tempo = System.nanoTime() - inicio;
        Desempenho.registrar("leitura", registros.length, tempo, "arquivo=" + ARQUIVO_ENTRADA);

        if (registros.length == 0) {
            System.out.println("Nenhum registro em " + ARQUIVO_ENTRADA + ".");
            return;
        }
        int limite = lerInteiro("Quantos registros exibir (0 = todos)", 20);
        if (limite == 0 || limite > registros.length) {
            limite = registros.length;
        }
        imprimirTabela(registros, limite);
        System.out.printf(Locale.US, "Exibindo %d de %d registros. Tempo de leitura do arquivo: %.3f ms%n",
                limite, registros.length, Desempenho.paraMs(tempo));
    }

    private static void imprimirTabela(Musica[] registros, int limite) {
        System.out.printf("%-6s %-24s %-20s %-12s %12s %5s %5s %-14s%n",
                "ID", "Título", "Artista", "Gênero", "Reprod.", "Aval.", "Ano", "Status");
        System.out.println("-".repeat(105));
        for (int i = 0; i < limite; i++) {
            Musica m = registros[i];
            System.out.printf(Locale.US, "%-6d %-24s %-20s %-12s %,12d %5.2f %5d %-14s%n",
                    m.getId(), Texto.cortar(m.getTitulo(), 24), Texto.cortar(m.getArtista(), 20),
                    Texto.cortar(m.getGenero(), 12), m.getReproducoes(), m.getAvaliacaoMedia(),
                    m.getAnoLancamento(), m.getStatus());
        }
    }

    private static void imprimirDetalhe(Musica m) {
        System.out.println("  ID " + m.getId() + " | \"" + m.getTitulo() + "\" - " + m.getArtista()
                + " (" + m.getPaisOrigem() + ")");
        System.out.println("  Álbum: " + m.getAlbum() + " | Gênero: " + m.getGenero() + " | Ano: " + m.getAnoLancamento()
                + " | Duração: " + m.getDuracaoSegundos() / 60 + ":" + String.format("%02d", m.getDuracaoSegundos() % 60));
        System.out.printf(Locale.US, "  Reproduções: %,d | Avaliação: %.2f | Status: %s | Atualizado em: %s%n",
                m.getReproducoes(), m.getAvaliacaoMedia(), m.getStatus(), m.getDataAtualizacao());
    }

    /** RF03: pesquisa uma musica na lista ligada e persiste a alteracao. */
    private static void atualizar() throws IOException {
        int id = lerInteiro("ID da música a alterar", -1);

        long inicio = System.nanoTime();
        ListaDeque lista = ArquivoMusicas.carregarLista(ARQUIVO_ENTRADA);
        Musica m = lista.buscarPorId(id);
        long tempo_busca = System.nanoTime() - inicio;
        if (m == null) {
            System.out.println("Música " + id + " não encontrada.");
            Desempenho.registrar("update_nao_encontrado", lista.tamanho(), tempo_busca, "id=" + id);
            return;
        }
        System.out.println("Registro atual:");
        imprimirDetalhe(m);
        System.out.println("(Enter mantem o valor atual)");

        try {
            m.setAlbum(lerTexto("Album", m.getAlbum()));
            m.setGenero(lerTexto("Genero", m.getGenero()));
            m.setReproducoes(lerLongo("Reproducoes", m.getReproducoes()));
            m.setAvaliacaoMedia(lerReal("Avaliacao media (0 a 5)", m.getAvaliacaoMedia()));
            m.setStatus(escolherStatus(m.getStatus()));
            m.setDataAtualizacao(LocalDateTime.now());
        } catch (IllegalArgumentException e) {
            System.out.println("Dado invalido, nada foi gravado: " + e.getMessage());
            return;
        }

        inicio = System.nanoTime();
        ArquivoMusicas.salvarLista(ARQUIVO_ENTRADA, lista);
        long tempo_gravacao = System.nanoTime() - inicio;
        Desempenho.registrar("update", lista.tamanho(), tempo_busca + tempo_gravacao,
                String.format(Locale.US, "id=%d busca_ms=%.3f gravacao_ms=%.3f", id,
                        Desempenho.paraMs(tempo_busca), Desempenho.paraMs(tempo_gravacao)));
        System.out.printf(Locale.US, "Registro atualizado. Busca: %.3f ms | gravação: %.3f ms%n",
                Desempenho.paraMs(tempo_busca), Desempenho.paraMs(tempo_gravacao));
    }

    /** RF04/RF05: remove um unico registro por execucao, seguindo a politica LIFO. */
    private static void excluir() throws IOException {
        ListaDeque lista = ArquivoMusicas.carregarLista(ARQUIVO_ENTRADA);
        Musica candidato = lista.espiarUltimoIncluido();
        if (candidato == null) {
            System.out.println("Nao ha registros para remover.");
            return;
        }
        System.out.println("Politica LIFO: sera removido o ultimo registro incluido:");
        imprimirDetalhe(candidato);
        if (!confirmar("Confirmar remocao?")) {
            System.out.println("Operacao cancelada.");
            return;
        }
        long inicio = System.nanoTime();
        lista = ArquivoMusicas.carregarLista(ARQUIVO_ENTRADA);
        Musica removida = lista.removerUltimoIncluido();
        ArquivoMusicas.salvarLista(ARQUIVO_ENTRADA, lista);
        long tempo = System.nanoTime() - inicio;

        Desempenho.registrar("exclusao_lifo", lista.tamanho() + 1, tempo, "id=" + removida.getId());
        System.out.printf(Locale.US, "Registro %d removido (restam %d). Tempo: %.3f ms%n",
                removida.getId(), lista.tamanho(), Desempenho.paraMs(tempo));
    }

    // ================= ordenacao =================

    /** RF06/RF07: ordena entrada.txt e grava um arquivo de saida por algoritmo. */
    private static void ordenar() throws IOException {
        Musica[] original = ArquivoMusicas.ler(ARQUIVO_ENTRADA);
        if (original.length == 0) {
            System.out.println("entrada.txt está vazio. Gere ou cadastre dados primeiro.");
            return;
        }
        Criterio criterio = escolherCriterio();

        Algoritmo[] algoritmos = Algoritmo.values();
        System.out.println("Algoritmo:");
        for (int i = 0; i < algoritmos.length; i++) {
            System.out.println("  " + (i + 1) + ") " + algoritmos[i].getDescricao());
        }
        System.out.println("  5) Todos (comparativo)");
        int escolha = lerInteiro("Algoritmo", 5);
        List<Algoritmo> executar = new ArrayList<>();
        if (escolha >= 1 && escolha <= algoritmos.length) {
            executar.add(algoritmos[escolha - 1]);
        } else {
            executar.addAll(List.of(algoritmos));
        }

        System.out.println("\nPrimeiros 5 registros ANTES da ordenacao (ordem do arquivo):");
        imprimirTabela(original, Math.min(5, original.length));

        System.out.printf("%n%-28s %8s %14s %14s %12s  %s%n",
                "Algoritmo", "n", "comparacoes", "movimentos", "tempo (ms)", "arquivo de saida");
        System.out.println("-".repeat(100));
        Musica[] ultimo_resultado = null;
        for (Algoritmo algoritmo : executar) {
            Musica[] copia = original.clone();   // mesmos dados de entrada para todos
            EstatisticaOrdenacao estatistica = new EstatisticaOrdenacao();

            long inicio = System.nanoTime();
            algoritmo.ordenar(copia, criterio, estatistica);
            long tempo_ordenacao = System.nanoTime() - inicio;

            inicio = System.nanoTime();
            ArquivoMusicas.salvar(algoritmo.getArquivoSaida(), copia, criterio);
            long tempo_gravacao = System.nanoTime() - inicio;

            Desempenho.registrar("ordenacao_" + algoritmo.getNomeCurto(), original.length, tempo_ordenacao,
                    String.format(Locale.US, "criterio=%s comparacoes=%d movimentos=%d gravacao_ms=%.3f saida=%s",
                            criterio.name(), estatistica.comparacoes, estatistica.movimentos,
                            Desempenho.paraMs(tempo_gravacao), algoritmo.getArquivoSaida()));
            System.out.printf(Locale.US, "%-28s %8d %,14d %,14d %12.3f  %s%n",
                    algoritmo.getDescricao(), original.length, estatistica.comparacoes,
                    estatistica.movimentos, Desempenho.paraMs(tempo_ordenacao), algoritmo.getArquivoSaida());
            ultimo_resultado = copia;
        }

        System.out.println("\nPrimeiros 5 registros DEPOIS da ordenação (" + criterio.getDescricao() + "):");
        imprimirTabela(ultimo_resultado, Math.min(5, ultimo_resultado.length));
        System.out.println("\nentrada.txt não foi alterado. Tempo de gravação de cada saída está em "
                + Desempenho.ARQUIVO_LOG + ".");
    }

    // ================= pesquisa =================

    /** RF08: pesquisa no arquivo de entrada (nao ordenado) ou em uma saida ordenada. */
    private static void pesquisar() throws IOException {
        System.out.println("Pesquisar em: 1) entrada.txt (nao ordenado)   2) arquivo de saida ordenado");
        String caminho = ARQUIVO_ENTRADA;
        if (lerInteiro("Fonte", 1) == 2) {
            List<Algoritmo> disponiveis = new ArrayList<>();
            for (Algoritmo a : Algoritmo.values()) {
                if (ArquivoMusicas.existe(a.getArquivoSaida())) {
                    disponiveis.add(a);
                    System.out.println("  " + disponiveis.size() + ") " + a.getArquivoSaida()
                            + "  [" + ArquivoMusicas.lerCriterio(a.getArquivoSaida()) + "]");
                }
            }
            if (disponiveis.isEmpty()) {
                System.out.println("Nenhum arquivo de saida existe ainda. Use a opcao 6 primeiro.");
                return;
            }
            int indice = lerInteiro("Arquivo", 1);
            if (indice < 1 || indice > disponiveis.size()) {
                System.out.println("Opcao invalida.");
                return;
            }
            caminho = disponiveis.get(indice - 1).getArquivoSaida();
        }

        System.out.println("Pesquisar por: 1) ID   2) Titulo   3) Artista");
        int campo = lerInteiro("Campo", 1);
        if (campo < 1 || campo > 3) {
            System.out.println("Opcao invalida.");
            return;
        }
        int id_procurado = 0;
        String texto_procurado = "";
        if (campo == 1) {
            id_procurado = lerInteiro("ID", -1);
        } else {
            texto_procurado = lerTexto("Texto (trecho)", null);
        }

        long inicio = System.nanoTime();
        Musica[] registros = ArquivoMusicas.ler(caminho);
        Criterio ordenado_por = ArquivoMusicas.lerCriterio(caminho);
        long tempo_leitura = System.nanoTime() - inicio;

        List<Musica> encontrados = new ArrayList<>();
        String metodo;
        inicio = System.nanoTime();
        if (campo == 1) {
            boolean pode_binaria = ordenado_por == Criterio.ID_ASC;
            int indice = pode_binaria
                    ? Pesquisa.buscaBinariaPorId(registros, id_procurado)
                    : Pesquisa.buscaSequencialPorId(registros, id_procurado);
            metodo = pode_binaria ? "busca binaria (arquivo ordenado por ID)" : "busca sequencial";
            if (indice >= 0) {
                encontrados.add(registros[indice]);
            }
        } else {
            metodo = "busca sequencial";
            encontrados = campo == 2
                    ? Pesquisa.buscarPorTitulo(registros, texto_procurado)
                    : Pesquisa.buscarPorArtista(registros, texto_procurado);
        }
        long tempo_busca = System.nanoTime() - inicio;

        Desempenho.registrar("pesquisa_" + (campo == 1 ? "id" : campo == 2 ? "titulo" : "artista"),
                registros.length, tempo_leitura + tempo_busca,
                String.format(Locale.US, "arquivo=%s metodo=\"%s\" leitura_ms=%.3f busca_ms=%.3f encontrados=%d",
                        caminho, metodo, Desempenho.paraMs(tempo_leitura), Desempenho.paraMs(tempo_busca),
                        encontrados.size()));

        System.out.println("\nMetodo: " + metodo + " | arquivo: " + caminho);
        if (encontrados.isEmpty()) {
            System.out.println("Nenhuma musica encontrada.");
        } else {
            System.out.println(encontrados.size() + " musica(s) encontrada(s):");
            int limite = Math.min(10, encontrados.size());
            for (int i = 0; i < limite; i++) {
                imprimirDetalhe(encontrados.get(i));
            }
            if (encontrados.size() > limite) {
                System.out.println("  ... e mais " + (encontrados.size() - limite) + " resultado(s).");
            }
        }
        System.out.printf(Locale.US, "Leitura do arquivo: %.3f ms | busca: %.3f ms%n",
                Desempenho.paraMs(tempo_leitura), Desempenho.paraMs(tempo_busca));
    }

    // ================= tabelas hash =================

    private static TabelaHashPaises carregarPaises() throws IOException {
        if (!ArquivoMusicas.existe(ARQUIVO_PAISES)) {
            TabelaHashPaises padrao = TabelaHashPaises.padrao();
            padrao.salvar(ARQUIVO_PAISES);
            return padrao;
        }
        return TabelaHashPaises.carregar(ARQUIVO_PAISES);
    }

    private static void menuPaises() throws IOException {
        TabelaHashPaises tabela = carregarPaises();
        boolean voltar = false;
        while (!voltar) {
            System.out.printf("%n--- Paises e fronteiras (%d paises, capacidade %d) ---%n",
                    tabela.tamanho(), tabela.capacidade());
            System.out.println("1) Consultar fronteiras de um pais");
            System.out.println("2) Listar todos os paises");
            System.out.println("3) Inserir/atualizar pais");
            System.out.println("4) Remover pais");
            System.out.println("5) Paises do catalogo e suas fronteiras");
            System.out.println("0) Voltar");
            int opcao = lerInteiro("Opcao", -1);
            switch (opcao) {
                case 1 -> {
                    String pais = lerTexto("Pais", null);
                    long inicio = System.nanoTime();
                    List<String> vizinhos = tabela.buscar(pais);
                    long tempo = System.nanoTime() - inicio;
                    Desempenho.registrar("hash_paises_busca", tabela.tamanho(), tempo, "pais=" + pais);
                    if (vizinhos == null) {
                        System.out.println("Pais nao cadastrado na tabela.");
                    } else if (vizinhos.isEmpty()) {
                        System.out.println(pais + " nao faz fronteira com outros paises.");
                    } else {
                        System.out.println("Fronteiras de " + pais + ": " + String.join(", ", vizinhos));
                    }
                    System.out.printf(Locale.US, "Tempo de busca: %.4f ms%n", Desempenho.paraMs(tempo));
                }
                case 2 -> {
                    for (String nome : tabela.nomes()) {
                        System.out.println(nome + " -> " + String.join(", ", tabela.buscar(nome)));
                    }
                }
                case 3 -> {
                    String pais = lerTexto("País", null);
                    System.out.print("Fronteiras (separadas por vírgula; Enter = nenhuma): ");
                    String linha = lerLinha();
                    List<String> vizinhos = new ArrayList<>();
                    if (!linha.isEmpty()) {
                        for (String v : linha.split(",")) {
                            if (!v.isBlank()) {
                                vizinhos.add(v.trim());
                            }
                        }
                    }
                    long inicio = System.nanoTime();
                    tabela.inserir(pais, vizinhos);
                    tabela.salvar(ARQUIVO_PAISES);
                    Desempenho.registrar("hash_paises_insercao", tabela.tamanho(), System.nanoTime() - inicio,
                            "pais=" + pais);
                    System.out.println("Pais gravado em " + ARQUIVO_PAISES + ".");
                }
                case 4 -> {
                    String pais = lerTexto("Pais", null);
                    if (tabela.remover(pais)) {
                        tabela.salvar(ARQUIVO_PAISES);
                        System.out.println("Pais removido.");
                    } else {
                        System.out.println("Pais não encontrado.");
                    }
                }
                case 5 -> paisesDoCatalogo(tabela);
                case 0 -> voltar = true;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    /** Cruza os paises de origem dos artistas do catalogo com a tabela de fronteiras. */
    private static void paisesDoCatalogo(TabelaHashPaises tabela) throws IOException {
        Musica[] registros = ArquivoMusicas.ler(ARQUIVO_ENTRADA);
        List<String> paises = new ArrayList<>();
        List<Integer> contagem = new ArrayList<>();
        for (Musica m : registros) {
            int indice = paises.indexOf(m.getPaisOrigem());
            if (indice < 0) {
                paises.add(m.getPaisOrigem());
                contagem.add(1);
            } else {
                contagem.set(indice, contagem.get(indice) + 1);
            }
        }
        if (paises.isEmpty()) {
            System.out.println("O catalogo está vazio.");
            return;
        }
        for (int i = 0; i < paises.size(); i++) {
            List<String> vizinhos = tabela.buscar(paises.get(i));
            System.out.printf("%-16s (%d músicas) -> %s%n", paises.get(i), contagem.get(i),
                    vizinhos == null ? "(sem dados de fronteira)"
                            : vizinhos.isEmpty() ? "(sem fronteiras terrestres)" : String.join(", ", vizinhos));
        }
    }

    private static TabelaHashCidades carregarCidades() throws IOException {
        if (!ArquivoMusicas.existe(ARQUIVO_CIDADES)) {
            TabelaHashCidades padrao = TabelaHashCidades.padrao();
            padrao.salvar(ARQUIVO_CIDADES);
            return padrao;
        }
        return TabelaHashCidades.carregar(ARQUIVO_CIDADES);
    }

    private static void menuCidades() throws IOException {
        TabelaHashCidades tabela = carregarCidades();
        boolean voltar = false;
        while (!voltar) {
            System.out.printf("%n--- Cidades em ordem alfabetica (%d cidades, %d baldes) ---%n",
                    tabela.tamanho(), tabela.capacidade());
            System.out.println("1) Listar cidades em ordem alfabetica (varredura da tabela hash)");
            System.out.println("2) Buscar cidade");
            System.out.println("3) Inserir cidade");
            System.out.println("4) Verificar se a varredura está ordenada");
            System.out.println("0) Voltar");
            int opcao = lerInteiro("Opcao", -1);
            switch (opcao) {
                case 1 -> {
                    long inicio = System.nanoTime();
                    List<String[]> lista = tabela.listarEmOrdem();
                    long tempo = System.nanoTime() - inicio;
                    Desempenho.registrar("hash_cidades_listagem", tabela.tamanho(), tempo, "sem ordenacao adicional");
                    for (String[] c : lista) {
                        System.out.println(c[0] + " (" + c[1] + ")");
                    }
                    System.out.printf(Locale.US, "%d cidades em %.4f ms (a ordem vem da função hash).%n",
                            lista.size(), Desempenho.paraMs(tempo));
                }
                case 2 -> {
                    String nome = lerTexto("Cidade", null);
                    long inicio = System.nanoTime();
                    List<String[]> achadas = tabela.buscar(nome);
                    long tempo = System.nanoTime() - inicio;
                    Desempenho.registrar("hash_cidades_busca", tabela.tamanho(), tempo, "cidade=" + nome);
                    if (achadas.isEmpty()) {
                        System.out.println("Cidade nao encontrada.");
                    }
                    for (String[] c : achadas) {
                        System.out.println(c[0] + " - " + c[1]);
                    }
                    System.out.printf(Locale.US, "Tempo de busca: %.4f ms%n", Desempenho.paraMs(tempo));
                }
                case 3 -> {
                    String cidade = lerTexto("Cidade", null);
                    String pais = lerTexto("Pais", null);
                    long inicio = System.nanoTime();
                    boolean inserida = tabela.inserir(cidade, pais);
                    long tempo = System.nanoTime() - inicio;
                    if (inserida) {
                        tabela.salvar(ARQUIVO_CIDADES);
                        Desempenho.registrar("hash_cidades_insercao", tabela.tamanho(), tempo, "cidade=" + cidade);
                        System.out.println("Cidade inserida e gravada em " + ARQUIVO_CIDADES + ".");
                    } else {
                        System.out.println("Essa cidade ja existe para esse pais.");
                    }
                }
                case 4 -> System.out.println(tabela.estaOrdenada()
                        ? "OK: a varredura dos baldes esta em ordem alfabetica."
                        : "ERRO: a varredura nao esta em ordem alfabetica.");
                case 0 -> voltar = true;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    // ================= benchmark =================

    private static void executarBenchmark() throws IOException {
        System.out.println("Tamanhos testados: 100, 500, 1000, 5000, 10000 e 20000 registros simulados.");
        System.out.println("(Pode levar alguns segundos: Bubble/Insertion/Selection são O(n²).)");
        Criterio criterio = escolherCriterio();
        Benchmark.executar(new int[] {100, 500, 1000, 5000, 10000, 20000}, criterio);
    }
}

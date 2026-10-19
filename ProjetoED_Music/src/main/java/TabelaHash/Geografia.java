package TabelaHash;
/**
 * Dados geograficos iniciais para as tabelas hash e para o gerador de dados.
 * Cada linha de FRONTEIRAS: {pais, vizinho1, vizinho2, ...}.
 * Cada linha de CIDADES: {pais, cidade1, cidade2, ...}.
 */
public final class Geografia {

    private Geografia() {
    }

    public static final String[][] FRONTEIRAS = {
        {"Brasil", "Argentina", "Bolívia", "Colômbia", "Guiana", "Guiana Francesa", "Paraguai", "Peru", "Suriname", "Uruguai", "Venezuela"},
        {"Argentina", "Bolívia", "Brasil", "Chile", "Paraguai", "Uruguai"},
        {"Bolívia", "Argentina", "Brasil", "Chile", "Paraguai", "Peru"},
        {"Chile", "Argentina", "Bolívia", "Peru"},
        {"Colômbia", "Brasil", "Equador", "Panamá", "Peru", "Venezuela"},
        {"Equador", "Colômbia", "Peru"},
        {"Paraguai", "Argentina", "Bolívia", "Brasil"},
        {"Peru", "Bolívia", "Brasil", "Chile", "Colômbia", "Equador"},
        {"Uruguai", "Argentina", "Brasil"},
        {"Venezuela", "Brasil", "Colômbia", "Guiana"},
        {"Estados Unidos", "Canadá", "México"},
        {"Canadá", "Estados Unidos"},
        {"México", "Belize", "Estados Unidos", "Guatemala"},
        {"Portugal", "Espanha"},
        {"Espanha", "Andorra", "França", "Portugal"},
        {"França", "Alemanha", "Andorra", "Bélgica", "Espanha", "Itália", "Luxemburgo", "Mônaco", "Suíça"},
        {"Alemanha", "Áustria", "Bélgica", "Dinamarca", "França", "Luxemburgo", "Países Baixos", "Polônia", "República Tcheca", "Suíça"},
        {"Itália", "Áustria", "Eslovênia", "França", "San Marino", "Suíça", "Vaticano"},
        {"Reino Unido", "Irlanda"},
        {"Irlanda", "Reino Unido"},
        {"Suécia", "Finlândia", "Noruega"},
        {"Coreia do Sul", "Coreia do Norte"},
        {"Índia", "Bangladesh", "Butão", "China", "Mianmar", "Nepal", "Paquistão"},
        {"Nigéria", "Benin", "Camarões", "Chade", "Níger"},
        {"Japão"},
        {"Austrália"}
    };

    public static final String[][] CIDADES = {
        {"Brasil", "São Paulo", "Rio de Janeiro", "Salvador", "Belo Horizonte", "Curitiba", "Recife", "Porto Alegre", "Brasília", "Fortaleza"},
        {"Argentina", "Buenos Aires", "Córdoba", "Rosário", "Mendoza"},
        {"Colômbia", "Bogotá", "Medellín", "Cali", "Cartagena"},
        {"México", "Cidade do México", "Guadalajara", "Monterrey", "Puebla"},
        {"Estados Unidos", "Nova York", "Los Angeles", "Chicago", "Miami", "Houston"},
        {"Reino Unido", "Londres", "Manchester", "Liverpool", "Birmingham"},
        {"Portugal", "Lisboa", "Porto", "Coimbra", "Braga"},
        {"Espanha", "Madri", "Barcelona", "Sevilha", "Valência"},
        {"França", "Paris", "Lyon", "Marselha", "Nice"},
        {"Alemanha", "Berlim", "Munique", "Hamburgo", "Colônia"},
        {"Suécia", "Estocolmo", "Gotemburgo", "Malmö"},
        {"Coreia do Sul", "Seul", "Busan", "Incheon"},
        {"Índia", "Mumbai", "Nova Délhi", "Bangalore", "Chennai"},
        {"Japão", "Tóquio", "Osaka", "Quioto", "Nagoia"},
        {"Nigéria", "Lagos", "Abuja", "Kano"},
        {"Canadá", "Toronto", "Montreal", "Vancouver"},
        {"Itália", "Roma", "Milão", "Nápoles"},
        {"Chile", "Santiago", "Valparaíso", "Concepción"}
    };
}

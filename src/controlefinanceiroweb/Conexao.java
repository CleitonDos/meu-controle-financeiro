package controledespesapessoal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexao {

    // Configurações extraídas do seu TiDB Cloud
    private static final String HOST = "gateway01.us-east-1.prod.aws.tidbcloud.com";
    private static final String PORTA = "4000";
    private static final String BANCO = "sys";
    private static final String USUARIO = "3ujUqDVrbjXcqg9.root";
    
    // COLE AQUI ENTRE AS ASPAS A SENHA QUE VOCÊ SALVOU NO BLOCO DE NOTAS:
    private static final String SENHA = "SUA_SENHA_AQUI";

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORTA + "/" + BANCO + "?sslMode=VERIFY_IDENTITY";

    public static Connection getConexao() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL não encontrado: " + e.getMessage());
        }
    }

    public static void criarTabelaSeNaoExistir() {
        String sql = "CREATE TABLE IF NOT EXISTS despesas ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "descricao VARCHAR(255) NOT NULL, "
                + "tipo VARCHAR(50) NOT NULL, "
                + "categoria VARCHAR(50) NOT NULL, "
                + "valor DOUBLE NOT NULL, "
                + "data_vencimento DATE NOT NULL, "
                + "data_pagamento DATE NULL"
                + ");";

        try (Connection con = getConexao(); Statement stmt = con.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabela verificada/criada com sucesso no TiDB!");
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabela no TiDB: " + e.getMessage());
        }
    }
}

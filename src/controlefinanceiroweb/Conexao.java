package controlefinanceiroweb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexao {

    // Configurações para o banco de dados LOCAL no DBeaver (MySQL)
    private static final String HOST = "localhost";
    private static final String PORTA = "3306";
    private static final String BANCO = "controle_financeiro"; // Nome do banco que criou no DBeaver
    private static final String USUARIO = "root";             // O seu utilizador do MySQL local
    
    // Insira a sua senha do MySQL local entre as aspas abaixo:
    private static final String SENHA = "sua_senha_aqui";     

    // URL de conexão local (sem necessidade de SSL)
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORTA + "/" + BANCO + "?useSSL=false&serverTimezone=UTC";

    public static Connection conectar() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL não encontrado: " + e.getMessage());
        }
    }

    public static void inicializarBanco() {
        // Cria a tabela automaticamente se ela não existir
        String createSql = "CREATE TABLE IF NOT EXISTS lancamentos ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "descricao VARCHAR(255) NOT NULL, "
                + "valor DOUBLE NOT NULL, "
                + "tipo VARCHAR(20) NOT NULL, "
                + "categoria VARCHAR(50) NULL, "
                + "banco VARCHAR(50) NULL, "
                + "saldo DOUBLE NULL, "
                + "observacao TEXT NULL, "
                + "data_movimentacao DATE NOT NULL"
                + ");";

        try (Connection conn = conectar(); Statement stmt = conn.createStatement()) {
            stmt.execute(createSql);
            System.out.println("Tabela verificada com sucesso no banco local!");
        } catch (SQLException e) {
            System.err.println("Erro ao verificar tabela no MySQL local: " + e.getMessage());
        }
    }
    
}

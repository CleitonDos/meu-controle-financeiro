package controlefinanceiroweb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexao {

    // Dados da Nuvem Aiven
    private static final String HOST = "mysql-2832c419-cleiton-79f7.b.aivencloud.com";
    private static final String PORT = "15858";
    private static final String DATABASE = "defaultdb";
    private static final String USER = "avnadmin";
    
    // COLE SUA SENHA DO AIVEN ENTRE AS ASPAS ABAIXO:
    private static final String PASSWORD = "AVNS_Di07zr4fxNqmdIvNy2Y";

    // URL com SSL habilitado e parâmetros para evitar erros de certificado
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE 
            + "?useSSL=true&requireSSL=true&verifyServerCertificate=false&serverTimezone=UTC";

    public static Connection conectar() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver JDBC do MySQL nao encontrado!");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Cria a tabela automaticamente se ela ainda não existir na nuvem
    public static void inicializarBanco() {
        String sql = "CREATE TABLE IF NOT EXISTS lancamentos ("
                   + "id INT AUTO_INCREMENT PRIMARY KEY, "
                   + "data_movimentacao DATE NOT NULL, "
                   + "banco VARCHAR(50) NOT NULL, "
                   + "tipo VARCHAR(20) NOT NULL, "
                   + "descricao VARCHAR(255) NOT NULL, "
                   + "valor DECIMAL(10,2) NOT NULL, "
                   + "saldo DECIMAL(10,2) NOT NULL, "
                   + "observacao VARCHAR(255)"
                   + ")";
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println(">> BANCO AIVEN CONECTADO E TABELA VERIFICADA COM SUCESSO! <<");
        } catch (SQLException e) {
            System.out.println("Erro ao inicializar tabela no Aiven: " + e.getMessage());
        }
    }
}
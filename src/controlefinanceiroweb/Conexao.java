package controlefinanceiroweb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexao {

    private static final String HOST = "gateway01.us-east-1.prod.aws.tidbcloud.com";
    private static final String PORTA = "4000";
    private static final String BANCO = "test";
    private static final String USUARIO = "3ujUqDVrbjXcqg9.root";
    
    // COLE SUA SENHA DO TIDB AQUI ENTRE AS ASPAS:
    private static final String SENHA = "pcy84y6oVVitezTC";

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORTA + "/" + BANCO + "?sslMode=VERIFY_IDENTITY";

    public static Connection conectar() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL não encontrado: " + e.getMessage());
        }
    }
    public static void inicializarBanco() {
        // Apaga a tabela antiga para aplicar todas as colunas definitivas
        String dropSql = "DROP TABLE IF EXISTS lancamentos;";
        String createSql = "CREATE TABLE lancamentos ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "descricao VARCHAR(255) NOT NULL, "
                + "valor DOUBLE NOT NULL, "
                + "tipo VARCHAR(20) NOT NULL, "
                + "categoria VARCHAR(50) NULL, "
                + "banco VARCHAR(50) NULL, "
                + "observacao TEXT NULL, "
                + "data_movimentacao DATE NOT NULL"
                + ");";

        try (Connection conn = conectar(); Statement stmt = conn.createStatement()) {
            stmt.execute(dropSql);
            stmt.execute(createSql);
            System.out.println("Tabela de lancamentos 100% pronta com observacao!");
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar tabela no TiDB: " + e.getMessage());
        }
    }
    
    }

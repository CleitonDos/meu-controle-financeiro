package controlefinanceiroweb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LancamentoDAO {

    // Gera as linhas da tabela com os botões de Editar e Excluir
    public static String gerarLinhasTabelaHTML() {
        StringBuilder html = new StringBuilder();
        String sql = "SELECT * FROM lancamentos ORDER BY data_movimentacao ASC, id ASC";
        DateTimeFormatter formatoBr = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String dataBanco = rs.getString("data_movimentacao");
                String banco = rs.getString("banco");
                String tipo = rs.getString("tipo");
                String desc = rs.getString("descricao");
                double valor = rs.getDouble("valor");
                double saldo = rs.getDouble("saldo");
                String obs = rs.getString("observacao");
                if (obs == null) obs = "";

                String dataExibicao = dataBanco;
                try {
                    LocalDate dataConvertida = LocalDate.parse(dataBanco);
                    dataExibicao = dataConvertida.format(formatoBr);
                } catch (Exception e) {}

                boolean isEntrada = tipo != null && tipo.equalsIgnoreCase("Entrada");
                String classeLinha = isEntrada ? "linha-entrada" : "linha-saida";
                String badgeTipo = isEntrada ? "badge-tipo entrada" : "badge-tipo saida";
                String iconeSeta = isEntrada ? "fa-arrow-down" : "fa-arrow-up";

                String iconeObs;
                if (!obs.trim().isEmpty()) {
                    iconeObs = "<span class='obs-icon' title='" + obs + "'><i class='fas fa-comment'></i></span>";
                } else {
                    iconeObs = "<span class='obs-icon' title='Sem observação'><i class='fas fa-comment-slash'></i></span>";
                }

                // Trata aspas na descrição/observação para não quebrar o JavaScript do botão Editar
                String descJs = desc.replace("'", "\\'");
                String obsJs = obs.replace("'", "\\'");

                html.append("<tr class='").append(classeLinha).append("' ")
                    .append("data-banco='").append(banco).append("' ")
                    .append("data-tipo='").append(tipo).append("' ")
                    .append("data-data='").append(dataBanco).append("' ")
                    .append("data-valor='").append(valor).append("' ")
                    .append("data-saldo='").append(saldo).append("'>\n");

                html.append("  <td>").append(dataExibicao).append("</td>\n");
                html.append("  <td><i class='fas fa-building-columns me-1'></i> ").append(banco).append("</td>\n");
                html.append("  <td><span class='").append(badgeTipo).append("'><i class='fas ").append(iconeSeta).append(" me-1'></i> ").append(tipo).append("</span></td>\n");
                html.append("  <td>").append(desc).append("</td>\n");
                html.append("  <td class='text-end'>R$ ").append(String.format("%.2f", valor)).append("</td>\n");
                html.append("  <td class='text-end'>R$ ").append(String.format("%.2f", saldo)).append("</td>\n");
                html.append("  <td>").append(iconeObs).append("</td>\n");

                // Coluna de Ações: Editar e Excluir
                html.append("  <td class='text-center'>\n");
                html.append("    <button class='btn-acao btn-acao-edit' title='Editar' onclick=\"abrirModalEditar(")
                    .append(id).append(", '").append(dataBanco).append("', '").append(banco).append("', '").append(tipo)
                    .append("', '").append(descJs).append("', ").append(valor).append(", '").append(obsJs).append("')\">\n");
                html.append("      <i class='fas fa-pen-to-square'></i>\n");
                html.append("    </button>\n");
                html.append("    <a href='/excluir?id=").append(id)
                    .append("' class='btn-acao btn-acao-del' title='Excluir' onclick=\"return confirm('Deseja realmente excluir este lançamento?');\">\n");
                html.append("      <i class='fas fa-trash-can'></i>\n");
                html.append("    </a>\n");
                html.append("  </td>\n");

                html.append("</tr>\n");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao carregar do banco: " + e.getMessage());
        }

        return html.toString();
    }

    // Inserir novo registro e recalcular saldos
    public static void inserir(String data, String banco, String tipo, String desc, double valor, String obs) {
        String sql = "INSERT INTO lancamentos (data_movimentacao, banco, tipo, descricao, valor, saldo, observacao) VALUES (?, ?, ?, ?, ?, 0.0, ?)";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, data);
            stmt.setString(2, banco);
            stmt.setString(3, tipo);
            stmt.setString(4, desc);
            stmt.setDouble(5, valor);
            stmt.setString(6, obs);
            stmt.executeUpdate();

            // Recalcula o saldo cronológico de todas as linhas
            recalcularSaldos();
            System.out.println("Lançamento inserido com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao inserir: " + e.getMessage());
        }
    }

    // Editar registro existente e recalcular saldos
    public static void atualizar(int id, String data, String banco, String tipo, String desc, double valor, String obs) {
        String sql = "UPDATE lancamentos SET data_movimentacao = ?, banco = ?, tipo = ?, descricao = ?, valor = ?, observacao = ? WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, data);
            stmt.setString(2, banco);
            stmt.setString(3, tipo);
            stmt.setString(4, desc);
            stmt.setDouble(5, valor);
            stmt.setString(6, obs);
            stmt.setInt(7, id);
            stmt.executeUpdate();

            recalcularSaldos();
            System.out.println("Lançamento atualizado com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar: " + e.getMessage());
        }
    }

    // Excluir registro e recalcular saldos
    public static void excluir(int id) {
        String sql = "DELETE FROM lancamentos WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

            recalcularSaldos();
            System.out.println("Lançamento excluído com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao excluir: " + e.getMessage());
        }
    }

    // Recalcula o saldo acumulado de toda a tabela em ordem cronológica
    private static void recalcularSaldos() {
        String sqlBusca = "SELECT id, tipo, valor FROM lancamentos ORDER BY data_movimentacao ASC, id ASC";
        String sqlAtualiza = "UPDATE lancamentos SET saldo = ? WHERE id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmtBusca = conn.prepareStatement(sqlBusca);
             ResultSet rs = stmtBusca.executeQuery();
             PreparedStatement stmtAtualiza = conn.prepareStatement(sqlAtualiza)) {

            double saldoAcumulado = 0.0;
            while (rs.next()) {
                int id = rs.getInt("id");
                String tipo = rs.getString("tipo");
                double valor = rs.getDouble("valor");

                if (tipo != null && tipo.equalsIgnoreCase("Entrada")) {
                    saldoAcumulado += valor;
                } else {
                    saldoAcumulado -= valor;
                }

                stmtAtualiza.setDouble(1, saldoAcumulado);
                stmtAtualiza.setInt(2, id);
                stmtAtualiza.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao recalcular saldos: " + e.getMessage());
        }
    }
}
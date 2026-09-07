package controlefinanceiroweb;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ControleFinanceiroWeb {

    public static void main(String[] args) throws IOException {
        
        Conexao.inicializarBanco();

        HttpServer servidor = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);
        
     

        // ROTA 1: Página inicial
        servidor.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange troca) throws IOException {
                File arquivoHtml = new File("financeiro.htm");

                if (arquivoHtml.exists()) {
                    String conteudo = new String(Files.readAllBytes(arquivoHtml.toPath()), "UTF-8");
                    String linhasBanco = LancamentoDAO.gerarLinhasTabelaHTML();
                    conteudo = conteudo.replace("<!--DADOS_TABELA-->", linhasBanco);

                    byte[] resposta = conteudo.getBytes("UTF-8");
                    troca.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    troca.sendResponseHeaders(200, resposta.length);

                    OutputStream os = troca.getResponseBody();
                    os.write(resposta);
                    os.close();
                } else {
                    String erro = "Arquivo F:/financeiro.htm nao encontrado!";
                    troca.sendResponseHeaders(404, erro.length());
                    OutputStream os = troca.getResponseBody();
                    os.write(erro.getBytes());
                    os.close();
                }
            }
        });

        // ROTA 2: Salvar Novo Lançamento
        servidor.createContext("/salvar", new HttpHandler() {
            @Override
            public void handle(HttpExchange troca) throws IOException {
                if ("POST".equalsIgnoreCase(troca.getRequestMethod())) {
                    Map<String, String> campos = lerFormulario(troca);

                    String data = campos.getOrDefault("data", "");
                    String banco = campos.getOrDefault("banco", "");
                    String tipo = campos.getOrDefault("tipo", "");
                    String desc = campos.getOrDefault("descricao", "");
                    double valor = Double.parseDouble(campos.getOrDefault("valor", "0").replace(",", "."));
                    String obs = campos.getOrDefault("observacao", "");

                    LancamentoDAO.inserir(data, banco, tipo, desc, valor, obs);

                    redirecionarParaInicio(troca);
                }
            }
        });

        // ROTA 3: Editar Lançamento Existente
        servidor.createContext("/editar", new HttpHandler() {
            @Override
            public void handle(HttpExchange troca) throws IOException {
                if ("POST".equalsIgnoreCase(troca.getRequestMethod())) {
                    Map<String, String> campos = lerFormulario(troca);

                    int id = Integer.parseInt(campos.getOrDefault("id", "0"));
                    String data = campos.getOrDefault("data", "");
                    String banco = campos.getOrDefault("banco", "");
                    String tipo = campos.getOrDefault("tipo", "");
                    String desc = campos.getOrDefault("descricao", "");
                    double valor = Double.parseDouble(campos.getOrDefault("valor", "0").replace(",", "."));
                    String obs = campos.getOrDefault("observacao", "");

                    LancamentoDAO.atualizar(id, data, banco, tipo, desc, valor, obs);

                    redirecionarParaInicio(troca);
                }
            }
        });

        // ROTA 4: Excluir Lançamento
        servidor.createContext("/excluir", new HttpHandler() {
            @Override
            public void handle(HttpExchange troca) throws IOException {
                URI uri = troca.getRequestURI();
                String query = uri.getQuery(); // pega ?id=X
                if (query != null && query.startsWith("id=")) {
                    int id = Integer.parseInt(query.replace("id=", ""));
                    LancamentoDAO.excluir(id);
                }
                redirecionarParaInicio(troca);
            }
        });

        servidor.start();
        System.out.println("==================================================");
        System.out.println("SISTEMA ONLINE E INTERATIVO EM http://localhost:8080");
        System.out.println("==================================================");
    }

    private static Map<String, String> lerFormulario(HttpExchange troca) throws IOException {
        InputStreamReader isr = new InputStreamReader(troca.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder corpo = new StringBuilder();
        String linha;
        while ((linha = br.readLine()) != null) {
            corpo.append(linha);
        }

        Map<String, String> campos = new HashMap<>();
        for (String par : corpo.toString().split("&")) {
            String[] kv = par.split("=");
            if (kv.length == 2) {
                campos.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1], "UTF-8"));
            } else if (kv.length == 1) {
                campos.put(URLDecoder.decode(kv[0], "UTF-8"), "");
            }
        }
        return campos;
    }

    private static void redirecionarParaInicio(HttpExchange troca) throws IOException {
        troca.getResponseHeaders().set("Location", "/");
        troca.sendResponseHeaders(302, -1);
    }
}
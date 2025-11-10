import java.io.*;
import java.net.*;
import javax.swing.SwingUtilities;

public class ClienteDamas {
    private Socket socket;
    private BufferedReader entrada;
    private PrintStream saida;
    private TabuleiroForm form;
    private String minhaCor = ""; // "BRANCAS" ou "PRETAS"

    public ClienteDamas(TabuleiroForm form) {
        this.form = form;
    }

    public boolean conectar(String ip, int porta) {
        try {
            socket = new Socket(ip, porta);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            saida = new PrintStream(socket.getOutputStream());

            // Recebe a cor do servidor
            minhaCor = entrada.readLine();
            
            // Atualiza a GUI na EDT
            SwingUtilities.invokeLater(() -> {
                form.setStatusConexao("Status: Conectado. Você é o jogador " + (minhaCor.equals("COR_BRANCAS") ? "BRANCO (Primeiro)" : "PRETO"), true);
                form.setCorLocal(minhaCor.equals("COR_BRANCAS"));
                form.habilitarJogadas(minhaCor.equals("COR_BRANCAS")); // Brancas começam
            });

            // Inicia a thread para receber movimentos
            new Thread(this::ouvirMovimentos).start();
            return true;
        } catch (IOException e) {
            System.err.println("Falha na conexão: " + e.getMessage());
            SwingUtilities.invokeLater(() -> {
                form.setStatusConexao("Status: Falha na conexão.", false);
            });
            return false;
        }
    }

    private void ouvirMovimentos() {
        try {
            String movimento;
            while ((movimento = entrada.readLine()) != null) {
                if (movimento.equals("OPONENTE_DESCONECTOU")) {
                    SwingUtilities.invokeLater(() -> {
                        form.setStatusConexao("OPONENTE DESCONECTOU! Você venceu por W.O.", false);
                    });
                    break;
                }
                
                // Processa a jogada do oponente (ex: "JOGADA:r1,c1,r2,c2")
                System.out.println("Movimento recebido: " + movimento);
                SwingUtilities.invokeLater(() -> form.receberJogadaOponente(movimento));
            }
        } catch (IOException e) {
            System.out.println("Conexão perdida com o servidor.");
        } finally {
            fecharConexao();
        }
    }

    public void enviarJogada(String jogada) {
        if (saida != null) {
            saida.println(jogada);
        }
    }

    public void fecharConexao() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ServidorDamas {
    private static final int PORTA = 12345;
    private static final int MAX_JOGADORES = 2;
    private static final ExecutorService pool = Executors.newFixedThreadPool(MAX_JOGADORES);

    private static Socket jogador1 = null;
    private static Socket jogador2 = null;

    public static void main(String[] args) {
        System.out.println("Servidor de Damas iniciado na porta " + PORTA);
        try (ServerSocket listener = new ServerSocket(PORTA)) {
            
            // Aceita o Jogador 1 (Brancas)
            System.out.println("Aguardando Jogador 1 (Brancas)...");
            jogador1 = listener.accept();
            System.out.println("Jogador 1 (Brancas) conectado: " + jogador1.getInetAddress());
            // Envia a cor para o Jogador 1
            new PrintStream(jogador1.getOutputStream()).println("COR_BRANCAS");
            
            // Aceita o Jogador 2 (Pretas)
            System.out.println("Aguardando Jogador 2 (Pretas)...");
            jogador2 = listener.accept();
            System.out.println("Jogador 2 (Pretas) conectado: " + jogador2.getInetAddress());
            // Envia a cor para o Jogador 2
            new PrintStream(jogador2.getOutputStream()).println("COR_PRETAS");
            
            System.out.println("Ambos os jogadores conectados. O jogo começa!");

            // Cria e executa as threads de manipulação dos jogadores
            pool.execute(new ManipuladorJogador(jogador1, jogador2)); // J1 escuta e envia para J2
            pool.execute(new ManipuladorJogador(jogador2, jogador1)); // J2 escuta e envia para J1

        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    /**
     * Classe interna para manipular a comunicação de um jogador (ouvinte/retransmissor).
     */
    private static class ManipuladorJogador implements Runnable {
        private final Socket jogadorOrigem;
        private final Socket jogadorDestino;
        
        public ManipuladorJogador(Socket origem, Socket destino) {
            this.jogadorOrigem = origem;
            this.jogadorDestino = destino;
        }

        @Override
        public void run() {
            try (
                BufferedReader entrada = new BufferedReader(new InputStreamReader(jogadorOrigem.getInputStream()));
                PrintStream saida = new PrintStream(jogadorDestino.getOutputStream())
            ) {
                String movimento;
                while ((movimento = entrada.readLine()) != null) {
                    System.out.println("Recebido de " + jogadorOrigem.getInetAddress() + ": " + movimento);
                    // Retransmite a jogada para o oponente
                    saida.println(movimento);
                    System.out.println("Enviado para " + jogadorDestino.getInetAddress());
                }
            } catch (IOException e) {
                System.out.println("Jogador desconectado: " + jogadorOrigem.getInetAddress());
                // Notificar o outro jogador sobre a desconexão (melhoria)
                try {
                     new PrintStream(jogadorDestino.getOutputStream()).println("OPONENTE_DESCONECTOU");
                } catch (IOException ex) {
                    // O destino também está desconectado
                }
            }
        }
    }
}
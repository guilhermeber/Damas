package server;

import model.GameState;
import network.GameProtocol;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * Servidor de Damas - Gerencia partidas entre dois jogadores
 */
public class CheckersServer {
    private static final int PORT = 5000;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private BlockingQueue<GameRoom> waitingRooms;
    private int gameCounter = 0;
    
    public CheckersServer() {
        threadPool = Executors.newCachedThreadPool();
        waitingRooms = new LinkedBlockingQueue<>();
    }
    
    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("==============================================");
        System.out.println("   SERVIDOR DE DAMAS INICIADO");
        System.out.println("   IP: " + InetAddress.getLocalHost().getHostAddress());
        System.out.println("   Porta: " + PORT);
        System.out.println("==============================================");
        
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("\n[CONEXÃO] Novo cliente conectado: " + 
                    clientSocket.getInetAddress().getHostAddress());
                
                threadPool.execute(() -> handleClient(clientSocket));
            } catch (IOException e) {
                System.err.println("[ERRO] Erro ao aceitar conexão: " + e.getMessage());
            }
        }
    }
    
    private void handleClient(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            // Aguarda mensagem de conexão
            String connectMsg = in.readLine();
            if (connectMsg == null || !connectMsg.startsWith(GameProtocol.CONNECT)) {
                out.println(GameProtocol.createErrorMessage("Mensagem de conexão inválida"));
                socket.close();
                return;
            }
            
            String playerName = GameProtocol.getMessageContent(connectMsg);
            System.out.println("[JOGADOR] " + playerName + " entrou na fila");
            
            out.println(GameProtocol.CONNECTED);
            
            // Tenta encontrar uma sala de espera ou cria uma nova
            GameRoom room = waitingRooms.poll();
            if (room == null) {
                // Cria nova sala e aguarda segundo jogador
                room = new GameRoom(++gameCounter);
                room.setPlayer1(socket, in, out, playerName);
                out.println(GameProtocol.WAIT_FOR_PLAYER);
                System.out.println("[SALA " + room.getRoomId() + "] " + playerName + " aguardando oponente...");
                waitingRooms.offer(room);
            } else {
                // Adiciona à sala existente e inicia o jogo
                room.setPlayer2(socket, in, out, playerName);
                System.out.println("[SALA " + room.getRoomId() + "] Jogo iniciado!");
                System.out.println("  - Brancas (Jogador 1): " + room.getPlayer1Name());
                System.out.println("  - Pretas (Jogador 2): " + room.getPlayer2Name());
                final GameRoom finalRoom = room;
                threadPool.execute(finalRoom::startGame);
            }
            
        } catch (IOException e) {
            System.err.println("[ERRO] Erro ao processar cliente: " + e.getMessage());
        }
    }
    
    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();
            System.out.println("\n[SERVIDOR] Servidor encerrado");
        } catch (IOException e) {
            System.err.println("[ERRO] Erro ao fechar servidor: " + e.getMessage());
        }
    }
    
    /**
     * Classe interna que representa uma sala de jogo
     */
    private static class GameRoom {
        private final int roomId;
        private Socket player1Socket, player2Socket;
        private BufferedReader player1In, player2In;
        private PrintWriter player1Out, player2Out;
        private String player1Name, player2Name;
        private GameState gameState;
        
        public GameRoom(int roomId) {
            this.roomId = roomId;
            this.gameState = new GameState();
        }
        
        public void setPlayer1(Socket socket, BufferedReader in, PrintWriter out, String name) {
            this.player1Socket = socket;
            this.player1In = in;
            this.player1Out = out;
            this.player1Name = name;
        }
        
        public void setPlayer2(Socket socket, BufferedReader in, PrintWriter out, String name) {
            this.player2Socket = socket;
            this.player2In = in;
            this.player2Out = out;
            this.player2Name = name;
        }
        
        public void startGame() {
            try {
                // Envia mensagem de início para ambos os jogadores
                player1Out.println(GameProtocol.createStartMessage("WHITE"));
                player2Out.println(GameProtocol.createStartMessage("BLACK"));
                
                player1Out.println(GameProtocol.YOUR_TURN);
                
                System.out.println("[SALA " + roomId + "] Jogo em andamento...");
                
                // Loop do jogo
                boolean gameRunning = true;
                boolean player1Turn = true;
                
                while (gameRunning) {
                    try {
                        BufferedReader currentIn = player1Turn ? player1In : player2In;
                        PrintWriter currentOut = player1Turn ? player1Out : player2Out;
                        PrintWriter opponentOut = player1Turn ? player2Out : player1Out;
                        String currentPlayer = player1Turn ? player1Name : player2Name;
                        
                        String message = currentIn.readLine();
                        
                        if (message == null || message.startsWith(GameProtocol.DISCONNECT)) {
                            System.out.println("[SALA " + roomId + "] " + currentPlayer + " desconectou");
                            opponentOut.println(GameProtocol.createGameOverMessage("Oponente desconectou"));
                            gameRunning = false;
                            break;
                        }
                        
                        if (message.startsWith(GameProtocol.MOVE)) {
                            String moveData = GameProtocol.getMessageContent(message);
                            int[] move = GameProtocol.parseMove(moveData);
                            
                            if (move != null && move.length == 4) {
                                System.out.println("[SALA " + roomId + "] Recebeu movimento de " + currentPlayer + 
                                    ": (" + move[0] + "," + move[1] + ") -> (" + move[2] + "," + move[3] + 
                                    ") | player1Turn=" + player1Turn);
                                boolean validMove = gameState.executeMove(move[0], move[1], move[2], move[3]);
                                System.out.println("[SALA " + roomId + "] Movimento " + (validMove ? "VÁLIDO" : "INVÁLIDO"));
                                
                                if (validMove) {
                                    System.out.println("[SALA " + roomId + "] " + currentPlayer + 
                                        " moveu: (" + move[0] + "," + move[1] + ") -> (" + move[2] + "," + move[3] + ")");
                                    
                                    // Envia o movimento para AMBOS os jogadores
                                    String moveMsg = GameProtocol.createOpponentMoveMessage(
                                        move[0], move[1], move[2], move[3]);
                                    player1Out.println(moveMsg);
                                    player2Out.println(moveMsg);
                                    
                                    // Verifica fim de jogo
                                    String gameOverMsg = gameState.checkGameOver();
                                    if (gameOverMsg != null) {
                                        System.out.println("[SALA " + roomId + "] Jogo finalizado: " + gameOverMsg);
                                        player1Out.println(GameProtocol.createGameOverMessage(gameOverMsg));
                                        player2Out.println(GameProtocol.createGameOverMessage(gameOverMsg));
                                        gameRunning = false;
                                    } else {
                                        player1Turn = !player1Turn;
                                        String nextPlayer = player1Turn ? player1Name : player2Name;
                                        System.out.println("[SALA " + roomId + "] *** ENVIANDO YOUR_TURN para " + nextPlayer + " ***");
                                        opponentOut.println(GameProtocol.YOUR_TURN);
                                        System.out.println("[SALA " + roomId + "] YOUR_TURN enviado!");
                                    }
                                } else {
                                    currentOut.println(GameProtocol.MOVE_INVALID);
                                }
                            } else {
                                currentOut.println(GameProtocol.createErrorMessage("Formato de movimento inválido"));
                            }
                        }
                        
                    } catch (IOException e) {
                        System.err.println("[SALA " + roomId + "] Erro na comunicação: " + e.getMessage());
                        gameRunning = false;
                    }
                }
                
            } catch (Exception e) {
                System.err.println("[SALA " + roomId + "] Erro no jogo: " + e.getMessage());
            } finally {
                closeConnections();
            }
        }
        
        private void closeConnections() {
            try {
                if (player1Socket != null) player1Socket.close();
                if (player2Socket != null) player2Socket.close();
                System.out.println("[SALA " + roomId + "] Conexões fechadas");
            } catch (IOException e) {
                System.err.println("[ERRO] Erro ao fechar conexões: " + e.getMessage());
            }
        }
        
        public int getRoomId() { return roomId; }
        public String getPlayer1Name() { return player1Name; }
        public String getPlayer2Name() { return player2Name; }
    }
    
    public static void main(String[] args) {
        CheckersServer server = new CheckersServer();
        
        // Adiciona shutdown hook para encerrar o servidor corretamente
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[SERVIDOR] Encerrando...");
            server.stop();
        }));
        
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("[ERRO FATAL] Não foi possível iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

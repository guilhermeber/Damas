package network;

/**
 * Protocolo de comunicação entre cliente e servidor
 */
public class GameProtocol {
    
    private GameProtocol() {
        // Construtor privado para classe utilitária
    }
    
    // Tipos de mensagem
    public static final String CONNECT = "CONNECT";
    public static final String CONNECTED = "CONNECTED";
    public static final String WAIT_FOR_PLAYER = "WAIT";
    public static final String START = "START";
    public static final String MOVE = "MOVE";
    public static final String MOVE_OK = "MOVE_OK";
    public static final String MOVE_INVALID = "MOVE_INVALID";
    public static final String OPPONENT_MOVE = "OPPONENT_MOVE";
    public static final String YOUR_TURN = "YOUR_TURN";
    public static final String GAME_OVER = "GAME_OVER";
    public static final String DISCONNECT = "DISCONNECT";
    public static final String ERROR = "ERROR";
    
    // Separadores
    public static final String SEPARATOR = "|";
    public static final String FIELD_SEPARATOR = ",";
    
    /**
     * Cria mensagem de conexão
     * Formato: CONNECT|nomeJogador
     */
    public static String createConnectMessage(String playerName) {
        return CONNECT + SEPARATOR + playerName;
    }
    
    /**
     * Cria mensagem de movimento
     * Formato: MOVE|r1,c1,r2,c2
     */
    public static String createMoveMessage(int r1, int c1, int r2, int c2) {
        return MOVE + SEPARATOR + r1 + FIELD_SEPARATOR + c1 + FIELD_SEPARATOR + r2 + FIELD_SEPARATOR + c2;
    }
    
    /**
     * Cria mensagem de início de jogo
     * Formato: START|COR (WHITE ou BLACK)
     */
    public static String createStartMessage(String color) {
        return START + SEPARATOR + color;
    }
    
    /**
     * Cria mensagem de movimento do oponente
     * Formato: OPPONENT_MOVE|r1,c1,r2,c2
     */
    public static String createOpponentMoveMessage(int r1, int c1, int r2, int c2) {
        return OPPONENT_MOVE + SEPARATOR + r1 + FIELD_SEPARATOR + c1 + FIELD_SEPARATOR + r2 + FIELD_SEPARATOR + c2;
    }
    
    /**
     * Cria mensagem de fim de jogo
     * Formato: GAME_OVER|mensagem
     */
    public static String createGameOverMessage(String message) {
        return GAME_OVER + SEPARATOR + message;
    }
    
    /**
     * Cria mensagem de erro
     * Formato: ERROR|mensagem
     */
    public static String createErrorMessage(String message) {
        return ERROR + SEPARATOR + message;
    }
    
    /**
     * Extrai o tipo da mensagem
     */
    public static String getMessageType(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        if (!message.contains(SEPARATOR)) {
            return message; // Mensagens sem conteúdo (YOUR_TURN, MOVE_INVALID, etc.)
        }
        return message.substring(0, message.indexOf(SEPARATOR));
    }
    
    /**
     * Extrai o conteúdo da mensagem
     */
    public static String getMessageContent(String message) {
        if (message == null || message.isEmpty() || !message.contains(SEPARATOR)) {
            return "";
        }
        return message.substring(message.indexOf(SEPARATOR) + 1);
    }
    
    /**
     * Parse de movimento (r1,c1,r2,c2)
     * Retorna array [r1, c1, r2, c2]
     */
    public static int[] parseMove(String moveData) {
        String[] parts = moveData.split(FIELD_SEPARATOR);
        if (parts.length != 4) {
            return new int[0];
        }
        try {
            return new int[] {
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3])
            };
        } catch (NumberFormatException e) {
            return new int[0];
        }
    }
}

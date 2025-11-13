package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa o estado do jogo de damas
 */
public class GameState {
    private String[][] board;
    private boolean whiteTurn;
    
    public GameState() {
        board = new String[8][8];
        whiteTurn = true;
        initializeBoard();
    }
    
    /**
     * Inicializa o tabuleiro com as peças nas posições iniciais
     * Pretas (⚫) nas linhas 0-2, Brancas (⚪) nas linhas 5-7
     * Apenas nas casas escuras (onde r+c é ímpar)
     */
    private void initializeBoard() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board[r][c] = "";
                // Casas escuras são onde (r + c) é ímpar
                if ((r + c) % 2 == 1) {
                    if (r < 3) {
                        board[r][c] = "⚫"; // Pretas no topo
                    } else if (r > 4) {
                        board[r][c] = "⚪"; // Brancas no fundo
                    }
                }
            }
        }
    }
    
    /**
     * Valida e executa um movimento
     * @return true se o movimento foi válido e executado
     */
    public synchronized boolean executeMove(int r1, int c1, int r2, int c2) {
        String piece = board[r1][c1];
        if (piece.isEmpty()) {
            System.out.println("[GAMESTATE] Movimento inválido: origem vazia");
            return false;
        }
        
        boolean isWhite = piece.contains("⚪");
        if (isWhite != whiteTurn) {
            System.out.println("[GAMESTATE] Movimento inválido: não é a vez de " + (isWhite ? "BRANCO" : "PRETO") + 
                ", vez atual: " + (whiteTurn ? "BRANCO" : "PRETO"));
            return false;
        }
        
        int dr = r2 - r1;
        int dc = c2 - c1;
        if (Math.abs(dr) != Math.abs(dc)) {
            System.out.println("[GAMESTATE] Movimento inválido: não é diagonal (dr=" + dr + ", dc=" + dc + ")");
            return false;
        }
        
        boolean isKing = piece.contains("D");
        System.out.println("[GAMESTATE] Validando movimento: peça=" + piece + " de (" + r1 + "," + c1 + 
            ") para (" + r2 + "," + c2 + ") | isKing=" + isKing + " | isWhite=" + isWhite + 
            " | dr=" + dr + " | dc=" + dc + ")");
        
        // Verifica se há capturas obrigatórias
        List<int[]> allCaptures = findAllCaptures(whiteTurn);
        boolean mustCapture = !allCaptures.isEmpty();
        
        // Movimento de captura
        if (Math.abs(dr) >= 2) {
            List<int[]> capturesFromPiece = getCaptureMovements(piece, r1, c1);
            boolean validCapture = false;
            for (int[] move : capturesFromPiece) {
                if (move[0] == r2 && move[1] == c2) {
                    validCapture = true;
                    break;
                }
            }
            
            if (validCapture) {
                if (performCapture(r1, c1, r2, c2)) {
                    // Verifica capturas sequenciais
                    List<int[]> sequentialCaptures = getCaptureMovements(board[r2][c2], r2, c2);
                    if (sequentialCaptures.isEmpty()) {
                        whiteTurn = !whiteTurn;
                    }
                    return true;
                }
            }
            return false;
        }
        
        // Movimento simples (não-captura)
        if (mustCapture) {
            System.out.println("[GAMESTATE] Movimento inválido: captura obrigatória disponível");
            return false; // Se há captura disponível, movimento simples não é permitido
        }
        
        if (!board[r2][c2].isEmpty()) {
            System.out.println("[GAMESTATE] Movimento inválido: destino ocupado");
            return false; // Destino deve estar vazio
        }
        
        if (!isKing) {
            // Peça normal: move 1 casa diagonalmente
            // Brancas sobem (dr < 0), Pretas descem (dr > 0)
            System.out.println("[GAMESTATE] Peça normal: dr=" + dr + ", dc=" + dc + 
                " | Direção correta? " + ((isWhite && dr < 0) || (!isWhite && dr > 0)));
            if (Math.abs(dr) == 1 && Math.abs(dc) == 1 && ((isWhite && dr < 0) || (!isWhite && dr > 0))) {
                System.out.println("[GAMESTATE] Movimento simples VÁLIDO!");
                performSimpleMove(r1, c1, r2, c2);
                whiteTurn = !whiteTurn;
                return true;
            }
        } else {
            // Dama: move qualquer distância na diagonal se caminho livre
            if (isPathClear(r1, c1, r2, c2)) {
                System.out.println("[GAMESTATE] Movimento de dama VÁLIDO!");
                performSimpleMove(r1, c1, r2, c2);
                whiteTurn = !whiteTurn;
                return true;
            }
        }
        
        System.out.println("[GAMESTATE] Movimento inválido: nenhuma condição satisfeita");
        return false;
    }
    
    private boolean performCapture(int r1, int c1, int r2, int c2) {
        String piece = board[r1][c1];
        boolean isKing = piece.contains("D");
        boolean isWhite = piece.contains("⚪");
        int dr = r2 - r1;
        int dc = c2 - c1;
        int stepR = Integer.signum(dr);
        int stepC = Integer.signum(dc);
        
        int enemyR = -1;
        int enemyC = -1;
        boolean captured = false;
        
        if (isKing) {
            for (int i = 1; i < Math.abs(dr); i++) {
                int rr = r1 + i * stepR;
                int cc = c1 + i * stepC;
                if (!board[rr][cc].isEmpty()) {
                    boolean isSameColor = board[rr][cc].contains("⚪") == isWhite;
                    if (!isSameColor) {
                        if (!captured) {
                            captured = true;
                            enemyR = rr;
                            enemyC = cc;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        } else {
            int rm = (r1 + r2) / 2;
            int cm = (c1 + c2) / 2;
            if (Math.abs(dr) == 2 && !board[rm][cm].isEmpty()) {
                boolean isDifferentColor = board[rm][cm].contains("⚪") != isWhite;
                if (isDifferentColor) {
                    captured = true;
                    enemyR = rm;
                    enemyC = cm;
                }
            }
        }
        
        if (captured) {
            board[enemyR][enemyC] = "";
            performSimpleMove(r1, c1, r2, c2);
            return true;
        }
        return false;
    }
    
    private void performSimpleMove(int r1, int c1, int r2, int c2) {
        board[r2][c2] = board[r1][c1];
        board[r1][c1] = "";
        
        // Promove a dama quando atinge a última linha
        // Brancas (⚪) atingem o topo (linha 0)
        // Pretas (⚫) atingem o fundo (linha 7)
        if (board[r2][c2].equals("⚪") && r2 == 0) {
            board[r2][c2] = "⚪D";
        } else if (board[r2][c2].equals("⚫") && r2 == 7) {
            board[r2][c2] = "⚫D";
        }
    }
    
    private boolean isPathClear(int r1, int c1, int r2, int c2) {
        int dr = Integer.signum(r2 - r1);
        int dc = Integer.signum(c2 - c1);
        for (int i = 1; i < Math.abs(r2 - r1); i++) {
            if (!board[r1 + i * dr][c1 + i * dc].isEmpty()) return false;
        }
        return true;
    }
    
    private List<int[]> findAllCaptures(boolean isWhite) {
        List<int[]> allCaptures = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                String piece = board[r][c];
                if (!piece.isEmpty() && piece.contains("⚪") == isWhite) {
                    allCaptures.addAll(getCaptureMovements(piece, r, c));
                }
            }
        }
        return allCaptures;
    }
    
    private List<int[]> getCaptureMovements(String piece, int r, int c) {
        List<int[]> moves = new ArrayList<>();
        if (piece.isEmpty()) return moves;
        
        boolean isKing = piece.contains("D");
        boolean isWhite = piece.contains("⚪");
        int[] dirs = {-1, 1};
        
        for (int dr : dirs) {
            for (int dc : dirs) {
                if (isKing) {
                    int rr = r + dr;
                    int cc = c + dc;
                    boolean enemyFound = false;
                    
                    while (rr >= 0 && rr < 8 && cc >= 0 && cc < 8) {
                        if (!board[rr][cc].isEmpty()) {
                            boolean isSameColor = board[rr][cc].contains("⚪") == isWhite;
                            if (isSameColor) {
                                break;
                            }
                            if (enemyFound) {
                                break;
                            }
                            enemyFound = true;
                        } else if (enemyFound) {
                            moves.add(new int[]{rr, cc});
                        }
                        rr += dr;
                        cc += dc;
                    }
                } else {
                    int rm = r + dr;
                    int cm = c + dc;
                    int r2 = r + 2 * dr;
                    int c2 = c + 2 * dc;
                    
                    if (r2 >= 0 && r2 < 8 && c2 >= 0 && c2 < 8) {
                        boolean hasEnemy = !board[rm][cm].isEmpty() && 
                                         board[rm][cm].contains("⚪") != isWhite;
                        boolean targetEmpty = board[r2][c2].isEmpty();
                        
                        if (hasEnemy && targetEmpty) {
                            moves.add(new int[]{r2, c2});
                        }
                    }
                }
            }
        }
        return moves;
    }
    
    /**
     * Verifica se o jogo terminou
     * @return String com mensagem de vitória ou null se o jogo continua
     */
    public String checkGameOver() {
        boolean hasWhite = false;
        boolean hasBlack = false;
        boolean hasWhiteMove = false;
        boolean hasBlackMove = false;
        
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                String p = board[r][c];
                if (!p.isEmpty()) {
                    boolean isWhite = p.contains("⚪");
                    if (isWhite) {
                        hasWhite = true;
                        if (!getCaptureMovements(p, r, c).isEmpty() || hasSimpleMove(p, r, c)) {
                            hasWhiteMove = true;
                        }
                    } else {
                        hasBlack = true;
                        if (!getCaptureMovements(p, r, c).isEmpty() || hasSimpleMove(p, r, c)) {
                            hasBlackMove = true;
                        }
                    }
                }
            }
        }
        
        if (!hasWhite) return "Pretas venceram!";
        if (!hasBlack) return "Brancas venceram!";
        if (whiteTurn && !hasWhiteMove) return "Pretas venceram! (Brancas sem movimentos)";
        if (!whiteTurn && !hasBlackMove) return "Brancas venceram! (Pretas sem movimentos)";
        
        return null;
    }
    
    private boolean hasSimpleMove(String piece, int r, int c) {
        boolean isKing = piece.contains("D");
        boolean isWhite = piece.contains("⚪");
        int[] dirs = {-1, 1};
        
        if (!isKing) {
            // Peça normal: verifica 1 casa na direção permitida
            int dr = isWhite ? -1 : 1; // Brancas sobem, pretas descem
            for (int dc : dirs) {
                int r2 = r + dr;
                int c2 = c + dc;
                if (r2 >= 0 && r2 < 8 && c2 >= 0 && c2 < 8 && board[r2][c2].isEmpty()) {
                    return true;
                }
            }
        } else {
            // Dama: verifica todas as diagonais até encontrar obstáculo
            for (int dr : dirs) {
                for (int dc : dirs) {
                    int r2 = r + dr;
                    int c2 = c + dc;
                    // Move até o limite ou até encontrar uma peça
                    while (r2 >= 0 && r2 < 8 && c2 >= 0 && c2 < 8) {
                        if (board[r2][c2].isEmpty()) {
                            return true; // Encontrou movimento válido
                        }
                        break; // Bloqueado por peça
                    }
                }
            }
        }
        return false;
    }
    
    public String[][] getBoard() {
        return board;
    }
    
    public boolean isWhiteTurn() {
        return whiteTurn;
    }
    
    public void setBoard(String[][] board) {
        this.board = board;
    }
    
    public void setWhiteTurn(boolean whiteTurn) {
        this.whiteTurn = whiteTurn;
    }
}

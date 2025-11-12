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
     */
    private void initializeBoard() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board[r][c] = "";
                if ((r + c) % 2 == 1) {
                    if (r < 3) board[r][c] = "⚫";
                    if (r > 4) board[r][c] = "⚪";
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
        if (piece.isEmpty()) return false;
        
        boolean isWhite = piece.contains("⚪");
        if (isWhite != whiteTurn) return false;
        
        int dr = r2 - r1;
        int dc = c2 - c1;
        if (Math.abs(dr) != Math.abs(dc)) return false;
        
        boolean isKing = piece.contains("D");
        
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
        
        // Movimento simples
        if (mustCapture) return false;
        
        if (!board[r2][c2].isEmpty()) return false;
        
        if (!isKing) {
            if ((isWhite && dr == -1 && Math.abs(dc) == 1) ||
                (!isWhite && dr == 1 && Math.abs(dc) == 1)) {
                performSimpleMove(r1, c1, r2, c2);
                whiteTurn = !whiteTurn;
                return true;
            }
        } else {
            if (isPathClear(r1, c1, r2, c2)) {
                performSimpleMove(r1, c1, r2, c2);
                whiteTurn = !whiteTurn;
                return true;
            }
        }
        
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
        
        // Promove a dama
        if (board[r2][c2].equals("⚪") && r2 == 0) board[r2][c2] = "⚪D";
        if (board[r2][c2].equals("⚫") && r2 == 7) board[r2][c2] = "⚫D";
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
        
        for (int dc : dirs) {
            if (!isKing) {
                int dr = isWhite ? -1 : 1;
                int r2 = r + dr;
                int c2 = c + dc;
                if (r2 >= 0 && r2 < 8 && c2 >= 0 && c2 < 8 && board[r2][c2].isEmpty()) {
                    return true;
                }
            } else {
                for (int dr : dirs) {
                    int r2 = r + dr;
                    int c2 = c + dc;
                    if (r2 >= 0 && r2 < 8 && c2 >= 0 && c2 < 8 && board[r2][c2].isEmpty()) {
                        return true;
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

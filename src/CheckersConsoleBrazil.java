import java.util.*;

public class CheckersConsoleBrazil {
    static final int SIZE = 8;

    enum PieceType { MAN, KING }
    enum Player { WHITE, BLACK }

    static class Piece {
        Player player;
        PieceType type;
        Piece(Player p, PieceType t) { player = p; type = t; }
        public String toString() {
            if (player == Player.WHITE)
                return type == PieceType.MAN ? "w" : "W";
            else
                return type == PieceType.MAN ? "b" : "B";
        }
    }

    static Piece[][] board = new Piece[SIZE][SIZE];
    static Player currentPlayer = Player.WHITE;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        initBoard();

        while (true) {
            printBoard();
            if (isGameOver()) {
                System.out.println("🏁 Fim de jogo! Vencedor: " + (currentPlayer == Player.WHITE ? "Pretas" : "Brancas"));
                break;
            }

            System.out.println("Vez de " + (currentPlayer == Player.WHITE ? "Brancas (w/W)" : "Pretas (b/B)"));
            System.out.print("Digite movimento (ex: 2 3 3 4): ");
            int r1 = sc.nextInt(), c1 = sc.nextInt(), r2 = sc.nextInt(), c2 = sc.nextInt();

            if (!move(r1, c1, r2, c2)) {
                System.out.println("❌ Movimento inválido. Tente novamente.");
            } else {
                currentPlayer = (currentPlayer == Player.WHITE) ? Player.BLACK : Player.WHITE;
            }
        }
    }

    // ===================== Inicialização =====================
    static void initBoard() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if ((r + c) % 2 == 1) {
                    if (r < 3) board[r][c] = new Piece(Player.BLACK, PieceType.MAN);
                    else if (r > 4) board[r][c] = new Piece(Player.WHITE, PieceType.MAN);
                }
            }
        }
    }

    // ===================== Impressão =====================
    static void printBoard() {
        System.out.println("\n   0 1 2 3 4 5 6 7");
        for (int r = 0; r < SIZE; r++) {
            System.out.print(r + "  ");
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] == null) System.out.print(((r + c) % 2 == 1 ? "." : " ") + " ");
                else System.out.print(board[r][c] + " ");
            }
            System.out.println();
        }
    }

    // ===================== Movimento =====================
    static boolean move(int r1, int c1, int r2, int c2) {
        if (!inBounds(r1, c1) || !inBounds(r2, c2)) return false;
        Piece p = board[r1][c1];
        if (p == null || p.player != currentPlayer) return false;

        List<List<int[]>> captures = findAllCaptureSequences(currentPlayer);
        boolean mustCapture = !captures.isEmpty();

        // Tentativa de movimento
        boolean didCapture = tryCapture(r1, c1, r2, c2);
        if (didCapture) {
            promoteIfNeeded(r2, c2);
            return true;
        }

        if (mustCapture) return false; // é obrigatório capturar

        if (p.type == PieceType.MAN) {
            int dir = (p.player == Player.WHITE ? -1 : 1);
            if (r2 == r1 + dir && Math.abs(c2 - c1) == 1 && board[r2][c2] == null) {
                board[r2][c2] = p; board[r1][c1] = null;
                promoteIfNeeded(r2, c2);
                return true;
            }
        } else if (p.type == PieceType.KING) {
            if (Math.abs(r2 - r1) == Math.abs(c2 - c1)) {
                if (clearPath(r1, c1, r2, c2)) {
                    board[r2][c2] = p; board[r1][c1] = null;
                    return true;
                }
            }
        }
        return false;
    }

    // ===================== Capturas =====================
    static boolean tryCapture(int r1, int c1, int r2, int c2) {
        Piece p = board[r1][c1];
        if (p == null) return false;

        if (p.type == PieceType.MAN) {
            if (Math.abs(r2 - r1) == 2 && Math.abs(c2 - c1) == 2) {
                int rm = (r1 + r2) / 2, cm = (c1 + c2) / 2;
                Piece mid = board[rm][cm];
                if (mid != null && mid.player != p.player && board[r2][c2] == null) {
                    board[r2][c2] = p; board[r1][c1] = null; board[rm][cm] = null;
                    // Verifica se pode capturar novamente
                    if (hasFurtherCapture(r2, c2)) printBoard();
                    return true;
                }
            }
        } else if (p.type == PieceType.KING) {
            if (Math.abs(r2 - r1) == Math.abs(c2 - c1)) {
                int dr = Integer.signum(r2 - r1);
                int dc = Integer.signum(c2 - c1);
                int rr = r1 + dr, cc = c1 + dc;
                boolean captured = false;
                int cr = -1, ccapture = -1;
                while (inBounds(rr, cc)) {
                    if (board[rr][cc] != null) {
                        if (board[rr][cc].player == p.player) return false;
                        if (captured) return false;
                        captured = true;
                        cr = rr; ccapture = cc;
                    } else if (captured && rr == r2 && cc == c2) {
                        board[r2][c2] = p;
                        board[r1][c1] = null;
                        board[cr][ccapture] = null;
                        return true;
                    }
                    rr += dr; cc += dc;
                }
            }
        }
        return false;
    }

    static boolean hasFurtherCapture(int r, int c) {
        Piece p = board[r][c];
        if (p == null) return false;
        int[] dirs = {-1, 1};
        for (int dr : dirs)
            for (int dc : dirs) {
                if (p.type == PieceType.MAN) {
                    int r2 = r + dr * 2, c2 = c + dc * 2;
                    int rm = r + dr, cm = c + dc;
                    if (inBounds(r2, c2) && board[r2][c2] == null
                            && board[rm][cm] != null && board[rm][cm].player != p.player)
                        return true;
                } else {
                    // Dama
                    int rr = r + dr, cc = c + dc;
                    boolean enemyFound = false;
                    while (inBounds(rr, cc)) {
                        if (board[rr][cc] == null) {
                            if (enemyFound) return true;
                        } else {
                            if (board[rr][cc].player == p.player) break;
                            if (enemyFound) break;
                            enemyFound = true;
                        }
                        rr += dr; cc += dc;
                    }
                }
            }
        return false;
    }

    static List<List<int[]>> findAllCaptureSequences(Player player) {
        List<List<int[]>> out = new ArrayList<>();
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (board[r][c] != null && board[r][c].player == player)
                    out.addAll(findCaptureSequencesFrom(r, c));
        return out;
    }

    static List<List<int[]>> findCaptureSequencesFrom(int r, int c) {
        List<List<int[]>> out = new ArrayList<>();
        Piece startPiece = board[r][c];
        Piece[][] backup = copyBoard(board);

        class DFS {
            void rec(int rr, int cc, Piece p, List<int[]> path) {
                boolean extended = false;
                int[] dirs = {-1, 1};
                for (int dr : dirs)
                    for (int dc : dirs) {
                        if (p.type == PieceType.MAN) {
                            int r2 = rr + 2 * dr, c2 = cc + 2 * dc;
                            int rm = rr + dr, cm = cc + dc;
                            if (inBounds(r2, c2) && board[r2][c2] == null
                                    && board[rm][cm] != null && board[rm][cm].player != p.player) {
                                Piece captured = board[rm][cm];
                                board[r2][c2] = p; board[rr][cc] = null; board[rm][cm] = null;
                                List<int[]> np = new ArrayList<>(path);
                                np.add(new int[]{r2, c2});
                                rec(r2, c2, p, np);
                                board[rr][cc] = p; board[r2][c2] = null; board[rm][cm] = captured;
                                extended = true;
                            }
                        } else {
                            int step = 1;
                            boolean enemyFound = false;
                            int er = -1, ec = -1;
                            while (inBounds(rr + dr * step, cc + dc * step)) {
                                int nr = rr + dr * step, nc = cc + dc * step;
                                if (board[nr][nc] == null) {
                                    if (enemyFound) {
                                        board[rr][cc] = null;
                                        board[nr][nc] = p;
                                        board[er][ec] = null;
                                        List<int[]> np = new ArrayList<>(path);
                                        np.add(new int[]{nr, nc});
                                        rec(nr, nc, p, np);
                                        board[rr][cc] = p;
                                        board[nr][nc] = null;
                                        board[er][ec] = new Piece(
                                                (p.player == Player.WHITE ? Player.BLACK : Player.WHITE),
                                                PieceType.MAN);
                                        extended = true;
                                    }
                                } else {
                                    if (board[nr][nc].player == p.player) break;
                                    if (enemyFound) break;
                                    enemyFound = true;
                                    er = nr; ec = nc;
                                }
                                step++;
                            }
                        }
                    }
                if (!extended && path.size() > 1)
                    out.add(path);
            }
        }

        try {
            new DFS().rec(r, c, startPiece, new ArrayList<>(List.of(new int[]{r, c})));
        } finally {
            for (int i = 0; i < SIZE; i++)
                for (int j = 0; j < SIZE; j++)
                    board[i][j] = backup[i][j];
        }
        return out;
    }

    // ===================== Auxiliares =====================
    static Piece[][] copyBoard(Piece[][] b) {
        Piece[][] c = new Piece[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (b[i][j] != null)
                    c[i][j] = new Piece(b[i][j].player, b[i][j].type);
        return c;
    }

    static boolean clearPath(int r1, int c1, int r2, int c2) {
        int dr = Integer.signum(r2 - r1);
        int dc = Integer.signum(c2 - c1);
        int rr = r1 + dr, cc = c1 + dc;
        while (rr != r2 || cc != c2) {
            if (board[rr][cc] != null) return false;
            rr += dr; cc += dc;
        }
        return true;
    }

    static void promoteIfNeeded(int r, int c) {
        Piece p = board[r][c];
        if (p != null && p.type == PieceType.MAN) {
            if ((p.player == Player.WHITE && r == 0) ||
                    (p.player == Player.BLACK && r == SIZE - 1)) {
                p.type = PieceType.KING;
            }
        }
    }

    static boolean inBounds(int r, int c) {
        return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
    }

    static boolean isGameOver() {
        boolean whiteExists = false, blackExists = false;
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++) {
                Piece p = board[r][c];
                if (p != null) {
                    if (p.player == Player.WHITE) whiteExists = true;
                    else blackExists = true;
                }
            }
        return !(whiteExists && blackExists);
    }
}
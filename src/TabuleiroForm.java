import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TabuleiroForm {
    private JPanel mainPanel;
    private JPanel tabuleiroPanel;
    // Novos componentes de rede
    private JTextField ipTextField;
    private JButton conectarButton;
    private JLabel statusLabel;

    private final JButton[][] casas = new JButton[8][8];
    // Strings internas: "⚫", "⚪", "⚫D", "⚪D"
    private final String[][] pecas = new String[8][8];

    private boolean vezBrancas = true;
    private int selecR = -1, selecC = -1;
    private boolean precisaCapturar = false;

    public TabuleiroForm() {
        mainPanel = new JPanel(new BorderLayout());
        tabuleiroPanel = new JPanel(new GridLayout(8, 8));

        // 1. Inicializa e adiciona o Painel de Rede no topo
        JPanel painelRede = criarPainelRede();
        mainPanel.add(painelRede, BorderLayout.NORTH);

        mainPanel.add(tabuleiroPanel, BorderLayout.CENTER);
        inicializarTabuleiro();
        desenharTabuleiro();
    }

    /**
     * Cria e configura o painel com o campo de IP e o botão de Conectar.
     */
    private JPanel criarPainelRede() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        painel.setBackground(new Color(60, 0, 90)); // Fundo roxo escuro

        ipTextField = new JTextField("127.0.0.1", 15);
        conectarButton = new JButton("Conectar ao Oponente");
        statusLabel = new JLabel("Status: Desconectado");

        ipTextField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        conectarButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

        conectarButton.addActionListener(e -> {
            String ip = ipTextField.getText().trim();
            // **Aqui, você implementaria a lógica de conexão de rede real (Sockets)**
            statusLabel.setText("Tentando conectar a: " + ip + "...");
            // Exemplo de lógica de conexão simulada:
            simularConexao(ip);
        });

        painel.add(new JLabel("IP do Oponente:"));
        painel.add(ipTextField);
        painel.add(conectarButton);
        painel.add(statusLabel);

        return painel;
    }

    // Simulação temporária da lógica de conexão
    private void simularConexao(String ip) {
        // Esta é apenas uma simulação. A implementação real requer java.net.Socket.
        if (ip.equals("127.0.0.1") || ip.startsWith("192.")) {
            // Simula uma conexão bem-sucedida
            statusLabel.setText("Status: Conectado a " + ip);
            statusLabel.setForeground(Color.GREEN);
            JOptionPane.showMessageDialog(mainPanel, "Conexão estabelecida com sucesso! IP: " + ip);
        } else {
            // Simula falha
            statusLabel.setText("Status: Falha na conexão com " + ip);
            statusLabel.setForeground(Color.RED);
        }
    }

    // --- LÓGICA DE TABULEIRO E PEÇAS ABAIXO ---

    private void inicializarTabuleiro() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                pecas[r][c] = "";
                if ((r + c) % 2 == 1) {
                    if (r < 3) pecas[r][c] = "⚫";
                    if (r > 4) pecas[r][c] = "⚪";
                }
            }
        }
    }

    private String getIconePeca(String peca) {
        if (peca.equals("⚪D")) {
            return "♕"; // Dama Branca (Rainha Branca)
        }
        if (peca.equals("⚫D")) {
            return "♛"; // Dama Preta (Rainha Preta)
        }
        if (peca.equals("⚪")) {
            return "\u25CB"; // Círculo vazio
        }
        if (peca.equals("⚫")) {
            return "\u25CF"; // Círculo cheio
        }
        return "";
    }

    private Color getCorForeground(String peca) {
        if (peca.contains("⚪")) {
            return Color.WHITE;
        }
        if (peca.contains("⚫")) {
            return Color.BLACK;
        }
        return Color.BLACK;
    }

    private void desenharTabuleiro() {
        final Color COR_CLARA = Color.WHITE;
        final Color COR_ESCURA = new Color(139, 69, 19);

        tabuleiroPanel.removeAll();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                String peca = pecas[r][c];
                String icone = getIconePeca(peca);

                JButton casa = new JButton(icone);

                casa.setForeground(getCorForeground(peca));

                int fontSize = peca.contains("D") ? 30 : 24;
                casa.setFont(new Font("Segoe UI Emoji", Font.BOLD, fontSize));

                Color corFundo = (r + c) % 2 == 0 ? COR_CLARA : COR_ESCURA;

                if (r == selecR && c == selecC) {
                    corFundo = Color.YELLOW;
                }

                casa.setBackground(corFundo);

                casa.setBorderPainted(false);
                casa.setFocusPainted(false);

                int finalR = r, finalC = c;
                casa.addActionListener(e -> cliqueCasa(finalR, finalC));
                casas[r][c] = casa;
                tabuleiroPanel.add(casa);
            }
        }
        tabuleiroPanel.revalidate();
        tabuleiroPanel.repaint();
    }

    private void cliqueCasa(int r, int c) {
        // Desmarca visualmente a peça anterior
        if (selecR != -1) {
            Color corFundo = (selecR + selecC) % 2 == 0 ? Color.WHITE : new Color(139, 69, 19);
            casas[selecR][selecC].setBackground(corFundo);
        }

        if (selecR == -1 && !pecas[r][c].isEmpty()) {
            boolean ehBranca = pecas[r][c].contains("⚪");
            if (ehBranca == vezBrancas) {
                List<int[]> todasCapturas = encontrarTodasCapturas(vezBrancas);
                boolean deveCapturar = !todasCapturas.isEmpty();

                List<int[]> capturasDaPeca = movimentosDeCaptura(pecas[r][c], r, c);
                boolean estaPecaPodeCapturar = !capturasDaPeca.isEmpty();

                if (!deveCapturar || estaPecaPodeCapturar) {
                    selecR = r;
                    selecC = c;
                    casas[r][c].setBackground(Color.YELLOW);
                } else {
                    JOptionPane.showMessageDialog(mainPanel, "Você deve mover a peça que pode capturar!");
                }
            } else {
                JOptionPane.showMessageDialog(mainPanel, "Não é a sua vez!");
            }
            return;
        }

        if (selecR != -1) {
            moverOuCapturar(selecR, selecC, r, c);
            desenharTabuleiro();
            selecR = -1;
            selecC = -1;
        }
    }

    private void moverOuCapturar(int r1, int c1, int r2, int c2) {
        String peca = pecas[r1][c1];
        if (peca.isEmpty() || (r1 + c1) % 2 == 0 || !pecas[r2][c2].isEmpty()) return;

        int dr = r2 - r1;
        int dc = c2 - c1;
        if (Math.abs(dr) != Math.abs(dc)) return;

        boolean ehDama = peca.contains("D");
        boolean ehBranca = peca.contains("⚪");

        List<int[]> capturasObrigatorias = encontrarTodasCapturas(vezBrancas);
        boolean deveCapturar = !capturasObrigatorias.isEmpty();

        // 1. Tentar Captura
        if (Math.abs(dr) >= 2) {
            List<int[]> capturasDaPeca = movimentosDeCaptura(peca, r1, c1);
            boolean movimentoDeCapturaValido = false;
            for (int[] move : capturasDaPeca) {
                if (move[0] == r2 && move[1] == c2) {
                    movimentoDeCapturaValido = true;
                    break;
                }
            }

            if (movimentoDeCapturaValido) {
                if (realizarCaptura(r1, c1, r2, c2)) {
                    checarCapturasSequenciais(r2, c2);
                    return;
                }
            }
        }

        // 2. Tentar Movimento Simples
        if (deveCapturar) {
            JOptionPane.showMessageDialog(mainPanel, "Captura é obrigatória!");
            return;
        }

        // Movimento simples
        if (!ehDama) {
            if ((ehBranca && dr == -1 && Math.abs(dc) == 1) ||
                    (!ehBranca && dr == 1 && Math.abs(dc) == 1)) {
                realizarMovimento(r1, c1, r2, c2);
            }
        } else {
            if (caminhoLivre(r1, c1, r2, c2)) {
                realizarMovimento(r1, c1, r2, c2);
            }
        }
    }

    private boolean realizarCaptura(int r1, int c1, int r2, int c2) {
        String peca = pecas[r1][c1];
        boolean ehDama = peca.contains("D");
        boolean ehBranca = peca.contains("⚪");
        int dr = r2 - r1;
        int dc = c2 - c1;
        int stepR = Integer.signum(dr);
        int stepC = Integer.signum(dc);

        int enemyR = -1, enemyC = -1;
        boolean capturou = false;

        if (ehDama) {
            for (int i = 1; i < Math.abs(dr); i++) {
                int rr = r1 + i * stepR;
                int cc = c1 + i * stepC;
                if (!pecas[rr][cc].isEmpty()) {
                    if (pecas[rr][cc].contains("⚪") != ehBranca) {
                        if (!capturou) {
                            capturou = true;
                            enemyR = rr;
                            enemyC = cc;
                        } else return false;
                    } else return false;
                }
            }
        } else {
            int rm = (r1 + r2) / 2;
            int cm = (c1 + c2) / 2;
            if (Math.abs(dr) == 2 && !pecas[rm][cm].isEmpty()) {
                if (pecas[rm][cm].contains("⚪") != ehBranca) {
                    capturou = true;
                    enemyR = rm;
                    enemyC = cm;
                }
            }
        }

        if (capturou) {
            pecas[enemyR][enemyC] = "";
            realizarMovimento(r1, c1, r2, c2);
            return true;
        }
        return false;
    }

    private boolean caminhoLivre(int r1, int c1, int r2, int c2) {
        int dr = Integer.signum(r2 - r1);
        int dc = Integer.signum(c2 - c1);
        for (int i = 1; i < Math.abs(r2 - r1); i++) {
            if (!pecas[r1 + i * dr][c1 + i * dc].isEmpty()) return false;
        }
        return true;
    }

    private void realizarMovimento(int r1, int c1, int r2, int c2) {
        pecas[r2][c2] = pecas[r1][c1];
        pecas[r1][c1] = "";

        if (pecas[r2][c2].equals("⚪") && r2 == 0) pecas[r2][c2] = "⚪D";
        if (pecas[r2][c2].equals("⚫") && r2 == 7) pecas[r2][c2] = "⚫D";

        vezBrancas = !vezBrancas;
        verificarVitoria();
    }

    private void checarCapturasSequenciais(int r, int c) {
        List<int[]> capturas = movimentosDeCaptura(pecas[r][c], r, c);
        if (!capturas.isEmpty()) {
            vezBrancas = !vezBrancas;
            selecR = r;
            selecC = c;

            desenharTabuleiro();
            JOptionPane.showMessageDialog(mainPanel, "Captura sequencial obrigatória! Mova a peça selecionada.");
        }
    }

    private List<int[]> encontrarTodasCapturas(boolean ehBranca) {
        List<int[]> todasCapturas = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                String peca = pecas[r][c];
                if (!peca.isEmpty() && peca.contains("⚪") == ehBranca) {
                    todasCapturas.addAll(movimentosDeCaptura(peca, r, c));
                }
            }
        }
        return todasCapturas;
    }

    private List<int[]> movimentosDeCaptura(String peca, int r, int c) {
        List<int[]> moves = new ArrayList<>();
        if (peca.isEmpty()) return moves;

        boolean ehDama = peca.contains("D");
        boolean ehBranca = peca.contains("⚪");
        int[] dirs = {-1, 1};

        for (int dr : dirs) {
            for (int dc : dirs) {
                if (ehDama) {
                    int rr = r + dr, cc = c + dc;
                    boolean inimigoEncontrado = false;

                    while (rr >= 0 && rr < 8 && cc >= 0 && cc < 8) {
                        if (!pecas[rr][cc].isEmpty()) {
                            if (pecas[rr][cc].contains("⚪") == ehBranca) break;

                            if (inimigoEncontrado) break;

                            inimigoEncontrado = true;
                        } else if (inimigoEncontrado) {
                            moves.add(new int[]{rr, cc});
                        }

                        rr += dr;
                        cc += dc;
                    }
                } else {
                    int rm = r + dr, cm = c + dc;
                    int r2 = r + 2 * dr, c2 = c + 2 * dc;

                    if (r2 >= 0 && r2 < 8 && c2 >= 0 && c2 < 8) {
                        if (!pecas[rm][cm].isEmpty() &&
                                pecas[rm][cm].contains("⚪") != ehBranca &&
                                pecas[r2][c2].isEmpty()) {
                            moves.add(new int[]{r2, c2});
                        }
                    }
                }
            }
        }
        return moves;
    }

    private List<int[]> movimentosSimples(String peca, int r, int c) {
        List<int[]> moves = new ArrayList<>();
        boolean ehDama = peca.contains("D");
        boolean ehBranca = peca.contains("⚪");
        int[] dirs = {-1, 1};

        for (int dc : dirs) {
            if (!ehDama) {
                int dr = ehBranca ? -1 : 1;
                int r2 = r + dr, c2 = c + dc;
                if (r2 >= 0 && r2 < 8 && c2 >= 0 && c2 < 8 && pecas[r2][c2].isEmpty()) {
                    moves.add(new int[]{r2, c2});
                }
            } else {
                for (int dr : dirs) {
                    int r2 = r + dr, c2 = c + dc;
                    if (r2 >= 0 && r2 < 8 && c2 >= 0 && c2 < 8 && pecas[r2][c2].isEmpty()) {
                        moves.add(new int[]{r2, c2});
                    }
                }
            }
        }
        return moves;
    }

    private void verificarVitoria() {
        boolean temBranca = false, temPreta = false;
        boolean temMovimentoBranca = false;
        boolean temMovimentoPreta = false;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                String p = pecas[r][c];
                if (!p.isEmpty()) {
                    boolean ehBranca = p.contains("⚪");
                    if (ehBranca) {
                        temBranca = true;
                        if (!movimentosDeCaptura(p, r, c).isEmpty() || movimentosSimples(p, r, c).size() > 0) {
                            temMovimentoBranca = true;
                        }
                    } else {
                        temPreta = true;
                        if (!movimentosDeCaptura(p, r, c).isEmpty() || movimentosSimples(p, r, c).size() > 0) {
                            temMovimentoPreta = true;
                        }
                    }
                }
            }
        }

        if (!temBranca) {
            JOptionPane.showMessageDialog(mainPanel, "Pretas venceram! (Todas as peças brancas capturadas)");
            System.exit(0);
        }
        if (!temPreta) {
            JOptionPane.showMessageDialog(mainPanel, "Brancas venceram! (Todas as peças pretas capturadas)");
            System.exit(0);
        }

        if (vezBrancas && temBranca && !temMovimentoBranca) {
            JOptionPane.showMessageDialog(mainPanel, "Pretas venceram! (Brancas afogadas/sem movimentos)");
            System.exit(0);
        }
        if (!vezBrancas && temPreta && !temMovimentoPreta) {
            JOptionPane.showMessageDialog(mainPanel, "Brancas venceram! (Pretas afogadas/sem movimentos)");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Damas Brasileiras - Jogo de Rede");
        frame.setContentPane(new TabuleiroForm().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(650, 720); // Aumenta um pouco a altura para o painel de rede
        frame.setVisible(true);
    }
}
package form;

import network.GameProtocol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TabuleiroForm extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private static final String AGUARDANDO_OPONENTE = "Aguardando oponente...";
    
    private JPanel mainPanel;
    private JPanel tabuleiroPanel;
    
    // Componentes de rede
    private transient JTextField ipTextField;
    private transient JTextField nomeTextField;
    private transient JButton conectarButton;
    private transient JLabel statusLabel;
    private transient JLabel turnoLabel;

    private final transient JButton[][] casas = new JButton[8][8];
    // Strings internas: "⚫", "⚪", "⚫D", "⚪D"
    private final String[][] pecas = new String[8][8];

    private boolean vezBrancas = true;
    private int selecR = -1;
    private int selecC = -1;
    
    // Estado da rede
    private boolean conectado = false;
    private boolean ehJogadorBranco;
    private boolean minhavez = false;
    private transient Socket socket;
    private transient BufferedReader in;
    private transient PrintWriter out;
    private transient Thread receiveThread;

    public TabuleiroForm() {
        super("Damas Online - Cliente");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout(10, 10));
        tabuleiroPanel = new JPanel(new GridLayout(8, 8));

        // Painel de rede no topo
        JPanel painelRede = criarPainelRede();
        mainPanel.add(painelRede, BorderLayout.NORTH);

        mainPanel.add(tabuleiroPanel, BorderLayout.CENTER);
        
        // Painel de status na parte inferior
        JPanel painelStatus = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelStatus.setBackground(new Color(40, 40, 40));
        turnoLabel = new JLabel("Aguardando conexão...");
        turnoLabel.setForeground(Color.WHITE);
        turnoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        painelStatus.add(turnoLabel);
        mainPanel.add(painelStatus, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        inicializarTabuleiro();
        desenharTabuleiro();
        
        setSize(650, 750);
        setLocationRelativeTo(null);
        
        // Listener para fechar conexão ao sair
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                desconectar();
            }
        });
    }

    /**
     * Cria e configura o painel com o campo de IP e o botão de Conectar.
     */
    private JPanel criarPainelRede() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        painel.setBackground(new Color(60, 0, 90));

        JLabel ipLabel = new JLabel("IP do Servidor:");
        ipLabel.setForeground(Color.WHITE);
        ipLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        ipTextField = new JTextField("127.0.0.1", 12);
        ipTextField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        JLabel nomeLabel = new JLabel("Seu Nome:");
        nomeLabel.setForeground(Color.WHITE);
        nomeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        nomeTextField = new JTextField("Jogador", 10);
        nomeTextField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        conectarButton = new JButton("Conectar");
        conectarButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        conectarButton.addActionListener(e -> conectarAoServidor());

        statusLabel = new JLabel("Status: Desconectado");
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        painel.add(ipLabel);
        painel.add(ipTextField);
        painel.add(nomeLabel);
        painel.add(nomeTextField);
        painel.add(conectarButton);
        painel.add(statusLabel);

        return painel;
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
        final Color corClara = Color.WHITE;
        final Color corEscura = new Color(139, 69, 19);

        tabuleiroPanel.removeAll();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                String peca = pecas[r][c];
                String icone = getIconePeca(peca);

                JButton casa = new JButton(icone);

                casa.setForeground(getCorForeground(peca));

                int fontSize = peca.contains("D") ? 30 : 24;
                casa.setFont(new Font("Segoe UI Emoji", Font.BOLD, fontSize));

                Color corFundo = (r + c) % 2 == 0 ? corClara : corEscura;

                if (r == selecR && c == selecC) {
                    corFundo = Color.YELLOW;
                }

                casa.setBackground(corFundo);

                casa.setBorderPainted(false);
                casa.setFocusPainted(false);

                int finalR = r;
                int finalC = c;
                casa.addActionListener(e -> cliqueCasa(finalR, finalC));
                casas[r][c] = casa;
                tabuleiroPanel.add(casa);
            }
        }
        tabuleiroPanel.revalidate();
        tabuleiroPanel.repaint();
    }

    private void cliqueCasa(int r, int c) {
        // Debug
        System.out.println("[CLIENTE] Clique em (" + r + "," + c + ") - conectado=" + conectado + " minhavez=" + minhavez);
        
        // Verifica se está conectado e se é sua vez
        if (!conectado || !minhavez) {
            if (conectado) {
                JOptionPane.showMessageDialog(this, "Não é sua vez!");
            }
            return;
        }
        
        // Desmarca visualmente a peça anterior
        if (selecR != -1) {
            Color corFundo = (selecR + selecC) % 2 == 0 ? Color.WHITE : new Color(139, 69, 19);
            casas[selecR][selecC].setBackground(corFundo);
        }

        if (selecR == -1 && !pecas[r][c].isEmpty()) {
            boolean ehBranca = pecas[r][c].contains("⚪");
            if (ehBranca == ehJogadorBranco) {
                List<int[]> todasCapturas = encontrarTodasCapturas(vezBrancas);
                boolean deveCapturar = !todasCapturas.isEmpty();

                List<int[]> capturasDaPeca = movimentosDeCaptura(pecas[r][c], r, c);
                boolean estaPecaPodeCapturar = !capturasDaPeca.isEmpty();

                if (!deveCapturar || estaPecaPodeCapturar) {
                    selecR = r;
                    selecC = c;
                    casas[r][c].setBackground(Color.YELLOW);
                } else {
                    JOptionPane.showMessageDialog(this, "Você deve mover a peça que pode capturar!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Esta peça não é sua!");
            }
            return;
        }

        if (selecR != -1) {
            enviarMovimento(selecR, selecC, r, c);
            selecR = -1;
            selecC = -1;
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
                    int rr = r + dr;
                    int cc = c + dc;
                    boolean inimigoEncontrado = false;

                    while (rr >= 0 && rr < 8 && cc >= 0 && cc < 8) {
                        if (!pecas[rr][cc].isEmpty()) {
                            boolean mesmaColor = pecas[rr][cc].contains("⚪") == ehBranca;
                            if (mesmaColor || inimigoEncontrado) {
                                break;
                            }
                            inimigoEncontrado = true;
                        } else if (inimigoEncontrado) {
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
                        boolean temInimigo = !pecas[rm][cm].isEmpty() &&
                                pecas[rm][cm].contains("⚪") != ehBranca;
                        boolean casaVazia = pecas[r2][c2].isEmpty();
                        
                        if (temInimigo && casaVazia) {
                            moves.add(new int[]{r2, c2});
                        }
                    }
                }
            }
        }
        return moves;
    }
    
    // ==================== MÉTODOS DE REDE ====================
    
    private void conectarAoServidor() {
        if (conectado) {
            JOptionPane.showMessageDialog(this, "Você já está conectado!");
            return;
        }
        
        String serverIp = ipTextField.getText().trim();
        String nomeJogador = nomeTextField.getText().trim();
        
        if (serverIp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite o IP do servidor!");
            return;
        }
        
        if (nomeJogador.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite seu nome!");
            return;
        }
        
        conectarButton.setEnabled(false);
        statusLabel.setText("Conectando...");
        statusLabel.setForeground(Color.YELLOW);
        
        new Thread(() -> {
            try {
                socket = new Socket(serverIp, 5000);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                // Envia mensagem de conexão
                out.println(GameProtocol.createConnectMessage(nomeJogador));
                
                // Aguarda confirmação
                String response = in.readLine();
                if (response != null && response.equals(GameProtocol.CONNECTED)) {
                    conectado = true;
                    
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Conectado!");
                        statusLabel.setForeground(Color.GREEN);
                        turnoLabel.setText(AGUARDANDO_OPONENTE);
                        ipTextField.setEnabled(false);
                        nomeTextField.setEnabled(false);
                    });
                    
                    // Inicia thread de recepção
                    receiveThread = new Thread(this::receberMensagens);
                    receiveThread.start();
                } else {
                    throw new IOException("Falha na conexão");
                }
                
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Erro na conexão!");
                    statusLabel.setForeground(Color.RED);
                    conectarButton.setEnabled(true);
                    JOptionPane.showMessageDialog(TabuleiroForm.this, 
                        "Não foi possível conectar ao servidor!\n" + e.getMessage());
                });
            }
        }).start();
    }
    
    private void receberMensagens() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                String msgType = GameProtocol.getMessageType(message);
                String content = GameProtocol.getMessageContent(message);
                
                // Debug
                System.out.println("[CLIENTE] Recebeu: " + msgType + " | " + content);
                
                switch (msgType) {
                    case GameProtocol.WAIT_FOR_PLAYER:
                        SwingUtilities.invokeLater(() -> 
                            turnoLabel.setText(AGUARDANDO_OPONENTE));
                        break;
                        
                    case GameProtocol.START:
                        ehJogadorBranco = content.equals("WHITE");
                        SwingUtilities.invokeLater(() -> {
                            String cor = ehJogadorBranco ? "BRANCAS (⚪)" : "PRETAS (⚫)";
                            turnoLabel.setText("Você joga com: " + cor);
                            System.out.println("[CLIENTE] Sou jogador: " + (ehJogadorBranco ? "BRANCO" : "PRETO"));
                        });
                        break;
                        
                    case GameProtocol.YOUR_TURN:
                        minhavez = true;
                        System.out.println("[CLIENTE] É minha vez agora! minhavez=" + minhavez);
                        SwingUtilities.invokeLater(() -> {
                            turnoLabel.setText("SUA VEZ!");
                            turnoLabel.setForeground(Color.GREEN);
                        });
                        break;
                        
                    case GameProtocol.MOVE_OK:
                        minhavez = false;
                        SwingUtilities.invokeLater(() -> {
                            turnoLabel.setText(AGUARDANDO_OPONENTE);
                            turnoLabel.setForeground(Color.WHITE);
                        });
                        break;
                        
                    case GameProtocol.MOVE_INVALID:
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(TabuleiroForm.this, 
                                "Movimento inválido!");
                            desenharTabuleiro();
                        });
                        break;
                        
                    case GameProtocol.OPPONENT_MOVE:
                        int[] move = GameProtocol.parseMove(content);
                        if (move != null && move.length == 4) {
                            SwingUtilities.invokeLater(() -> 
                                aplicarMovimento(move[0], move[1], move[2], move[3]));
                        }
                        break;
                        
                    case GameProtocol.GAME_OVER:
                        SwingUtilities.invokeLater(() -> {
                            turnoLabel.setText("Jogo Finalizado!");
                            JOptionPane.showMessageDialog(TabuleiroForm.this, 
                                "Fim de Jogo!\n" + content);
                            desconectar();
                        });
                        break;
                        
                    case GameProtocol.ERROR:
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(TabuleiroForm.this, 
                                "Erro: " + content));
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            if (conectado) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(TabuleiroForm.this, 
                        "Conexão perdida com o servidor!");
                    desconectar();
                });
            }
        }
    }
    
    private void enviarMovimento(int r1, int c1, int r2, int c2) {
        if (out != null) {
            out.println(GameProtocol.createMoveMessage(r1, c1, r2, c2));
            aplicarMovimento(r1, c1, r2, c2);
        }
    }
    
    private void aplicarMovimento(int r1, int c1, int r2, int c2) {
        int dr = r2 - r1;
        int dc = c2 - c1;
        
        // Captura peça(s) no caminho
        if (Math.abs(dr) >= 2) {
            int stepR = Integer.signum(dr);
            int stepC = Integer.signum(dc);
            
            for (int i = 1; i < Math.abs(dr); i++) {
                int rr = r1 + i * stepR;
                int cc = c1 + i * stepC;
                if (!pecas[rr][cc].isEmpty()) {
                    pecas[rr][cc] = "";
                }
            }
        }
        
        // Move a peça
        pecas[r2][c2] = pecas[r1][c1];
        pecas[r1][c1] = "";
        
        // Promove a dama
        if (pecas[r2][c2].equals("⚪") && r2 == 0) pecas[r2][c2] = "⚪D";
        if (pecas[r2][c2].equals("⚫") && r2 == 7) pecas[r2][c2] = "⚫D";
        
        desenharTabuleiro();
    }
    
    private void desconectar() {
        conectado = false;
        minhavez = false;
        
        try {
            if (out != null) {
                out.println(GameProtocol.DISCONNECT);
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (receiveThread != null) {
                receiveThread.interrupt();
            }
        } catch (IOException e) {
            // Ignora erros ao desconectar
        }
        
        statusLabel.setText("Desconectado");
        statusLabel.setForeground(Color.RED);
        turnoLabel.setText("Desconectado");
        conectarButton.setEnabled(true);
        ipTextField.setEnabled(true);
        nomeTextField.setEnabled(true);
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            TabuleiroForm tabuleiro = new TabuleiroForm();
            tabuleiro.setVisible(true);
        });
    }
}
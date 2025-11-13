# Jogo de Damas Multiplayer - TCP/IP

Sistema completo de jogo de damas online utilizando comunicação TCP/IP entre clientes e servidor.

## 📋 Estrutura do Projeto

```
src/
├── network/
│   └── GameProtocol.java      # Protocolo de comunicação
├── model/
│   └── GameState.java          # Lógica do jogo (validação server-side)
├── server/
│   └── CheckersServer.java     # Servidor de partidas
└── form/
    └── TabuleiroForm.java      # Cliente com interface gráfica
```

## 🚀 Como Usar

### 1. Compilar o Projeto

Execute o script de compilação:

#### Windows:
```powershell
.\compile.bat
```

Ou manualmente:
```powershell
cd "d:\UTFPR\APS\trabalho iii\Damas"
javac -d bin -encoding UTF-8 src\network\*.java src\model\*.java src\server\*.java src\form\*.java
```

### 2. Executar o Servidor

Abra um terminal e execute:

#### Usando script:
```powershell
.\run-server.bat
```

#### Manualmente:
```powershell
java -cp bin server.CheckersServer
```

O servidor iniciará na porta **5000** e aguardará conexões.

### 3. Executar os Clientes

Abra **dois terminais separados** (um para cada jogador):

#### Usando script:
```powershell
.\run-client.bat
```

#### Manualmente:
```powershell
java -cp bin form.TabuleiroForm
```

### 4. Conectar e Jogar

1. Na interface de cada cliente:
   - **IP do Servidor**: Digite `127.0.0.1` (ou o IP do servidor)
   - **Seu Nome**: Digite um nome para identificação
   - Clique em **"Conectar"**

2. Aguarde o segundo jogador conectar

3. O jogo iniciará automaticamente:
   - **Jogador 1** joga com as **BRANCAS (⚪)**
   - **Jogador 2** joga com as **PRETAS (⚫)**

4. Clique nas peças e casas para jogar

## 🎮 Regras do Jogo

### Movimentos
- Peças normais movem-se **uma casa na diagonal** para frente
- **Damas** (⚪D, ⚫D) movem-se em qualquer direção diagonal

### Capturas
- Capturas são **obrigatórias**
- Capture pulando sobre a peça adversária
- Capturas múltiplas são possíveis e obrigatórias

### Vitória
- Capture todas as peças do adversário
- Bloqueie todos os movimentos do adversário

## 🔧 Requisitos

- **Java 8** ou superior
- Sistema operacional: Windows, Linux ou macOS
- Porta **5000** disponível para o servidor

## 📡 Protocolo de Comunicação

### Mensagens Cliente → Servidor
- `CONNECT|nomeJogador` - Conecta ao servidor
- `MOVE|r1,c1,r2,c2` - Realiza um movimento
- `DISCONNECT` - Desconecta

### Mensagens Servidor → Cliente
- `CONNECTED` - Confirmação de conexão
- `WAIT_FOR_PLAYER` - Aguardando outro jogador
- `START|WHITE` ou `START|BLACK` - Início do jogo com cor definida
- `YOUR_TURN` - É sua vez de jogar
- `MOVE_INVALID` - Movimento inválido
- `OPPONENT_MOVE|r1,c1,r2,c2` - Movimento do oponente (enviado para ambos os jogadores)
- `GAME_OVER|mensagem` - Fim do jogo
- `ERROR|mensagem` - Erro ocorrido

## 🏗️ Arquitetura

### Servidor (`CheckersServer`)
- Gerencia múltiplas partidas simultâneas
- Utiliza **ThreadPool** para processar conexões
- Valida todos os movimentos usando `GameState`
- Mantém salas de espera para emparceiramento

### Cliente (`TabuleiroForm`)
- Interface gráfica Swing completa
- Conexão TCP assíncrona
- Thread dedicada para recepção de mensagens
- Sincronização automática do tabuleiro
- Validação local básica de movimentos

### Modelo (`GameState`)
- Lógica completa do jogo de damas
- Validação de movimentos (server-side)
- Detecção de capturas obrigatórias
- Verificação de fim de jogo

## 🐛 Solução de Problemas

### Erro "Address already in use"
- Outro processo está usando a porta 5000
- Finalize o processo ou altere a porta no código

### Erro de conexão
- Verifique se o servidor está rodando
- Confirme o IP e porta corretos
- Verifique firewall

### Jogo não inicia
- Certifique-se de que **dois clientes** estão conectados
- Verifique os logs do servidor

## 📝 Notas

- O servidor pode gerenciar **múltiplas partidas** simultaneamente
- Cada partida é independente
- A desconexão de um jogador encerra a partida
- Todos os movimentos são validados no servidor

## 👥 Autores

Desenvolvido para a disciplina de APS - UTFPR

## 📄 Licença

Projeto acadêmico - UTFPR

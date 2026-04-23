package main;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public final class MainInterfaceGrafica extends JFrame {

    private final int TAMANHO = 6;
    private final CasaBotao[][] tabuleiroInterface = new CasaBotao[TAMANHO][TAMANHO];
    
    private final Tabuleiro tabuleiroLogico; 
    private int linhaOrigem = -1, colOrigem = -1;
    private int vez = 1;
    private boolean sequenciaCaptura = false;
    
    // CONTROLES DA IA
    private boolean vezIA = true;
    private int corIA = 2; // Por padrão IA joga de Pretas (2)
    private int profundidadeIA = 9; // Dificuldade padrão

    public MainInterfaceGrafica() {
        tabuleiroLogico = new Tabuleiro();

        setTitle("DISCIPLINA - IA - MINI JOGO DE DAMA");
        setSize(800, 800);
        setLayout(new GridLayout(TAMANHO, TAMANHO));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Pergunta as configurações antes de renderizar
        perguntarConfiguracoes();

        inicializarComponentes();
        sincronizarInterface(); 

        setVisible(true);

        // Se o humano escolheu as Pretas, a IA é a Branca e deve dar o primeiro passo
        if (vezIA && corIA == 1) {
            jogarTurnoIA();
        }
    }

    private void perguntarConfiguracoes() {
        // 1. Pergunta a cor
        Object[] options = {"Brancas (Começam)", "Pretas"};
        int escolha = JOptionPane.showOptionDialog(this,
                "Com qual cor você deseja jogar?",
                "Configuração da Partida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (escolha == 1) {
            corIA = 1; // Humano quer as Pretas, então IA fica com as Brancas
        } else {
            corIA = 2; // Humano quer as Brancas, então IA fica com as Pretas
        }

        // 2. Pergunta o nível de dificuldade (1 a 9) 
        boolean inputValido = false;
        while (!inputValido) {
            String nivelStr = JOptionPane.showInputDialog(this, 
                "Digite o nível de inteligência da IA (Profundidade de 1 a 9):", "8");
            
            if (nivelStr == null) {
                System.exit(0); // Usuário cancelou
            }
            
            try {
                int nivel = Integer.parseInt(nivelStr);
                if (nivel >= 1 && nivel <= 9) {
                    profundidadeIA = nivel;
                    inputValido = true;
                } else {
                    JOptionPane.showMessageDialog(this, "Por favor, digite um número entre 1 e 9.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Entrada inválida. Digite um número.");
            }
        }
    }

    private void inicializarComponentes() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                tabuleiroInterface[i][j] = new CasaBotao();

                if ((i + j) % 2 == 0) {
                    tabuleiroInterface[i][j].setBackground(new Color(235, 235, 208)); 
                } else {
                    tabuleiroInterface[i][j].setBackground(new Color(119, 149, 86));  
                }

                final int linha = i;
                final int coluna = j;
                tabuleiroInterface[i][j].addActionListener(e -> tratarClique(linha, coluna));
                add(tabuleiroInterface[i][j]);
            }
        }
    }

    private void tratarClique(int linha, int col) {
        if (vezIA && vez == corIA) return;

        
        int comboMaximoGeral = Regras.getMaiorComboDoJogador(tabuleiroLogico, vez);
        boolean checkComer = comboMaximoGeral > 0;
        
        if (linhaOrigem == -1) {
            char pecaClicada = tabuleiroLogico.getMatriz()[linha][col];
            if ((pecaClicada != '0') && (pecaClicada != 'b') && (pecaClicada % 2 == vez % 2)) {
                
                // BLOQUEIO DA LEI DA MAIORIA
                if (checkComer) {
                    int comboDestaPeca = Regras.getMaiorComboDaPeca(tabuleiroLogico, linha, col, sequenciaCaptura);
                    if (comboDestaPeca < comboMaximoGeral) {
                        JOptionPane.showMessageDialog(this, "Lei da Maioria: Você tem outra peça que realiza um combo maior!");
                        return; 
                    }
                }

                linhaOrigem = linha;
                colOrigem = col;
                tabuleiroInterface[linha][col].setBackground(Color.YELLOW);
            }
        } 
        else {
            if (linhaOrigem == linha && colOrigem == col) {
                if(!sequenciaCaptura) cancelarSelecao();
                return;
            }

            char peca = tabuleiroLogico.getMatriz()[linhaOrigem][colOrigem];
            int distLinha = Math.abs(linha - linhaOrigem);
            int distCol = Math.abs(col - colOrigem);
            
            boolean sucesso = false;
            boolean comeuAlguem = false;

            if (!checkComer) {
                if (peca <= '2' && distLinha == 1 && distCol == 1) {
                    if ((peca == '1' && linha < linhaOrigem) || (peca == '2' && linha > linhaOrigem)) {
                        sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                    }
                } else if (peca > '2' && distLinha == distCol) {
                    if (Regras.caminhoVazio(tabuleiroLogico, linhaOrigem, colOrigem, linha, col)) {
                        sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                    }
                }
            } 
            else {
                if (distLinha == distCol) { 
                    if (peca <= '2' && distLinha == 2) {
                        boolean sentidoValido = true;
                        if (!sequenciaCaptura) {
                            if (peca == '1' && linha > linhaOrigem) sentidoValido = false;
                            if (peca == '2' && linha < linhaOrigem) sentidoValido = false;
                        }

                        if (sentidoValido) {
                            int linhaMeio = (linha + linhaOrigem) / 2;
                            int colMeio = (col + colOrigem) / 2;
                            char pecaMeio = tabuleiroLogico.getMatriz()[linhaMeio][colMeio];

                            if (pecaMeio != '0' && pecaMeio != 'b' && (pecaMeio % 2 != peca % 2)) {
                                sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                                if (sucesso) {
                                    tabuleiroLogico.getMatriz()[linhaMeio][colMeio] = '0'; 
                                    comeuAlguem = true;
                                }
                            }
                        }
                    } else if (peca > '2') {
                        int[] posInimiga = Regras.tentarCapturaDama(tabuleiroLogico, linhaOrigem, colOrigem, linha, col);
                        if(posInimiga != null) {
                            sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                            if(sucesso) {
                                tabuleiroLogico.getMatriz()[posInimiga[0]][posInimiga[1]] = '0';
                                comeuAlguem = true;
                            }
                        }
                    }
                }
            }

            if (sucesso) {
                sincronizarInterface();
                verificarFimDeJogo();

                if (comeuAlguem && Regras.podeComer(tabuleiroLogico, linha, col, true)) {
                    tabuleiroInterface[linhaOrigem][colOrigem].setBackground(new Color(119, 149, 86));
                    sequenciaCaptura = true;
                    linhaOrigem = linha;
                    colOrigem = col;
                    tabuleiroInterface[linha][col].setBackground(Color.YELLOW);
                } else {
                    vez = (vez == 1) ? 2 : 1;
                    sequenciaCaptura = false;
                    cancelarSelecao();

                    verificarFimDeJogo(); // Verifica novamente antes da IA jogar

                    if (vezIA && vez == corIA) {
                        jogarTurnoIA();
                    }
                }
            } else {
                if(!sequenciaCaptura) cancelarSelecao();
            }
        }
    }

    private void jogarTurnoIA() {
        setTitle("A IA está pensando...");

        new Thread(() -> {
            boolean iaUsaBrancas = (corIA == 1);

            // --- INÍCIO DO CRONÔMETRO E CONTAGEM ---
            long tempoInicio = System.currentTimeMillis();
            
            Arvore arvore = new Arvore(tabuleiroLogico, iaUsaBrancas, profundidadeIA);
            
            // --- FIM DO CRONÔMETRO ---
            long tempoFim = System.currentTimeMillis();
            
            // IMPRIME O RELATÓRIO NO TERMINAL DO VS CODE
            System.out.println("--- TURNO DA IA ---");
            System.out.println("Nível de Dificuldade: " + profundidadeIA);
            System.out.println("Futuros calculados (Nós): " + Arvore.nosAvaliados);
            System.out.println("Tempo de processamento: " + (tempoFim - tempoInicio) + " ms\n");

            // Busca os melhores lances e aplica o fator aleatório em caso de empate de notas
            ArrayList<Node> opcoes = arvore.getRaiz().getChild();
            Node melhorJogada = null;

            if (!opcoes.isEmpty()) {
                // 1. Acha qual é a maior nota (melhor futuro)
                int maxScore = Integer.MIN_VALUE;
                for (Node filho : opcoes) {
                    if (filho.getMinMax() > maxScore) {
                        maxScore = filho.getMinMax();
                    }
                }

                // 2. Coleta todas as jogadas que tiraram a nota máxima
                ArrayList<Node> melhoresOpcoes = new ArrayList<>();
                for (Node filho : opcoes) {
                    if (filho.getMinMax() == maxScore) {
                        melhoresOpcoes.add(filho);
                    }
                }

                // 3. Sorteia aleatoriamente entre as melhores opções (Inteligência simulada)
                int sorteio = new Random().nextInt(melhoresOpcoes.size());
                melhorJogada = melhoresOpcoes.get(sorteio);
            }

            // Volta para a interface gráfica para atualizar a tela
            Node jogadaEscolhida = melhorJogada;
            SwingUtilities.invokeLater(() -> {
                if (jogadaEscolhida != null) {
                    tabuleiroLogico.setMatriz(jogadaEscolhida.getMatrix());
                    sincronizarInterface();
                    
                    vez = (corIA == 1) ? 2 : 1;
                    verificarFimDeJogo();
                }
                setTitle("DISCIPLINA - IA - MINI JOGO DE DAMA");
            });

        }).start(); 
    }

    private void cancelarSelecao() {
        if (linhaOrigem != -1) {
            tabuleiroInterface[linhaOrigem][colOrigem].setBackground(new Color(119, 149, 86));
        }
        linhaOrigem = -1;
        colOrigem = -1;
    }

    private boolean moverPecaLogica(int r1, int c1, int r2, int c2) {
        if (tabuleiroLogico.getMatriz()[r2][c2] == '0') {
            tabuleiroLogico.getMatriz()[r2][c2] = tabuleiroLogico.getMatriz()[r1][c1];
            tabuleiroLogico.getMatriz()[r1][c1] = '0';

            if (tabuleiroLogico.getMatriz()[r2][c2] == '2' && r2 == (TAMANHO - 1)) {
                tabuleiroLogico.getMatriz()[r2][c2] = '4';
            }
            if (tabuleiroLogico.getMatriz()[r2][c2] == '1' && r2 == 0) {
                tabuleiroLogico.getMatriz()[r2][c2] = '3';
            }
            return true;
        }
        return false;
    }

    private void verificarFimDeJogo() {
        // 1. Checagem de Asfixia (Jogador não tem movimentos disponíveis)
        if (!Regras.temMovimentoDisponivel(tabuleiroLogico, vez)) {
            String perdedor = (vez == 1) ? "Brancas" : "Pretas";
            String vencedor = (vez == 1) ? "Pretas" : "Brancas";
            JOptionPane.showMessageDialog(this, "FIM DE JOGO! As " + vencedor + " venceram! (" + perdedor + " bloqueado)");
            System.exit(0);
        }

        // 2. Checagem de Eliminação (Sem peças)
        int brancas = 0, pretas = 0;
        int damasBrancas = 0, damasPretas = 0;

        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                char p = tabuleiroLogico.getMatriz()[i][j];
                if (p == '1') brancas++;
                if (p == '3') { brancas++; damasBrancas++; }
                if (p == '2') pretas++;
                if (p == '4') { pretas++; damasPretas++; }
            }
        }
        
        if (brancas == 0) {
            JOptionPane.showMessageDialog(this, "FIM DE JOGO! As Pretas venceram por eliminação!");
            System.exit(0);
        } else if (pretas == 0) {
            JOptionPane.showMessageDialog(this, "FIM DE JOGO! As Brancas venceram por eliminação!");
            System.exit(0);
        }

        // 3. Regra de Empate: Somente 2 damas e ninguém pode comer
        if (brancas == 1 && damasBrancas == 1 && pretas == 1 && damasPretas == 1) {
            if (!Regras.alguemPodeComer(tabuleiroLogico, 1, false) && !Regras.alguemPodeComer(tabuleiroLogico, 2, false)) {
                JOptionPane.showMessageDialog(this, "EMPATE! Apenas duas Damas restantes no tabuleiro.");
                System.exit(0);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainInterfaceGrafica::new);
    }
    
    public void sincronizarInterface() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                char peca = tabuleiroLogico.getMatriz()[i][j];
                tabuleiroInterface[i][j].setTipoPeca(peca);
            }
        }
    }

    private class CasaBotao extends JButton {
        private char tipoPeca = '0';

        public void setTipoPeca(char tipo) {
            this.tipoPeca = tipo;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int margem = 10;
            if (tipoPeca == '1' || tipoPeca == '3') { 
                g2.setColor(Color.WHITE);
                g2.fillOval(margem, margem, getWidth() - 2 * margem, getHeight() - 2 * margem);
                g2.setColor(Color.BLACK);
                g2.drawOval(margem, margem, getWidth() - 2 * margem, getHeight() - 2 * margem);
            } else if (tipoPeca == '2' || tipoPeca == '4') { 
                g2.setColor(Color.BLACK);
                g2.fillOval(margem, margem, getWidth() - 2 * margem, getHeight() - 2 * margem);
            }

            if (tipoPeca > '2' && tipoPeca != 'b') { 
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(margem + 5, margem + 5, getWidth() - 2 * margem - 10, getHeight() - 2 * margem - 10);
            }
        }
    }
}
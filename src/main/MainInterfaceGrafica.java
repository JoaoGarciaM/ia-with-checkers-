package main;

import javax.swing.*;
import java.awt.*;

public final class MainInterfaceGrafica extends JFrame {

    private final int TAMANHO = 6;
    private final CasaBotao[][] tabuleiroInterface = new CasaBotao[TAMANHO][TAMANHO];
    
    private final Tabuleiro tabuleiroLogico; 
    private int linhaOrigem = -1, colOrigem = -1;
    private int vez = 1;
    private boolean sequenciaCaptura = false;

    public MainInterfaceGrafica() {
        tabuleiroLogico = new Tabuleiro();

        setTitle("DISCIPLINA - IA - MINI JOGO DE DAMA");
        setSize(800, 800);
        setLayout(new GridLayout(TAMANHO, TAMANHO));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        inicializarComponentes();
        sincronizarInterface(); 

        setVisible(true);

        // --- TESTE DA ÁRVORE ---
        System.out.println("\n=== LIGANDO O MOTOR DA IA ===");
        long tempoInicio = System.currentTimeMillis();
        
        // Simula o jogo 4 turnos no futuro, começando com as Pretas (false)
        Arvore arvoreDeTeste = new Arvore(tabuleiroLogico, false, 4); 
        
        long tempoFim = System.currentTimeMillis();
        
        // Quantas opções de jogada a IA tem AGORA no primeiro turno?
        int opcoesIniciais = arvoreDeTeste.getRaiz().getChild().size();
        
        System.out.println("A IA encontrou " + opcoesIniciais + " opções de jogada no turno 1.");
        System.out.println("Tempo para calcular o multiverso: " + (tempoFim - tempoInicio) + " milissegundos.");
        System.out.println("===============================\n");
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

        // PERGUNTA PARA A CLASSE REGRAS SE ALGUÉM É OBRIGADO A COMER
        boolean checkComer = Regras.alguemPodeComer(tabuleiroLogico, vez, sequenciaCaptura);
        
        // Caso 1: Nenhuma peça selecionada ainda
        if (linhaOrigem == -1) {
            char pecaClicada = tabuleiroLogico.getMatriz()[linha][col];
            
            if ((pecaClicada != '0') && (pecaClicada != 'b') && (pecaClicada % 2 == vez % 2)) {
                
                // PERGUNTA PARA AS REGRAS SE ESSA PEÇA ESPECÍFICA PODE COMER
                if(checkComer && !Regras.podeComer(tabuleiroLogico, linha, col, sequenciaCaptura)){
                    return; // Bloqueia o clique
                }
                
                linhaOrigem = linha;
                colOrigem = col;
                tabuleiroInterface[linha][col].setBackground(Color.YELLOW);
            }
        } 
        // Caso 2: Já existe uma peça selecionada, tentando mover
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

            // MOVIMENTO NORMAL
            if (!checkComer) {
                if (peca <= '2' && distLinha == 1 && distCol == 1) {
                    if ((peca == '1' && linha < linhaOrigem) || (peca == '2' && linha > linhaOrigem)) {
                        sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                    }
                } else if (peca > '2' && distLinha == distCol) {
                    
                    // PERGUNTA PARA AS REGRAS SE O CAMINHO ESTÁ VAZIO
                    if (Regras.caminhoVazio(tabuleiroLogico, linhaOrigem, colOrigem, linha, col)) {
                        sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                    }
                }
            } 
            // MOVIMENTO DE CAPTURA
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
                        // PERGUNTA PARA AS REGRAS SE A DAMA PODE CAPTURAR (Retorna a posição do inimigo morto)
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

            // AVALIA O RESULTADO DO MOVIMENTO
            if (sucesso) {
                sincronizarInterface();
                verificarFimDeJogo();

                // PERGUNTA PARA AS REGRAS SE O COMBO CONTINUA
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
                }
            } else {
                if(!sequenciaCaptura) cancelarSelecao();
            }
        }
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
        int brancas = 0, pretas = 0;
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                char p = tabuleiroLogico.getMatriz()[i][j];
                if (p == '1' || p == '3') brancas++;
                if (p == '2' || p == '4') pretas++;
            }
        }
        if (brancas == 0) {
            JOptionPane.showMessageDialog(this, "FIM DE JOGO! As Pretas venceram!");
        } else if (pretas == 0) {
            JOptionPane.showMessageDialog(this, "FIM DE JOGO! As Brancas venceram!");
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
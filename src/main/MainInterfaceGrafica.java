package main;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Douglas
 */
public final class MainInterfaceGrafica extends JFrame {

    private final int TAMANHO = 6;
    private final CasaBotao[][] tabuleiroInterface = new CasaBotao[TAMANHO][TAMANHO];
    
    /*
        Vazio: 0
        Brancas: 1
        Pretas: 2
        Damas: 3 (branca) ou 4 (preta)
        
        -> REGRAS
            -DEFINIR QUEM IRÁ UTILIZAR AS PEÇAS BRANCAS 
            -OBRIGATORIO COMER A PEÇA
            -NÃO É PERMITIDO COMER PARA TRAS
            -UMA PEÇA PODE COMER PRA MULTIPLAS PEÇAS EM QUALQUER DIREÇÃO DEDE QUEA PRIMEIRA SEJA PRA FRENTE
            -A DAMA PODE ANDAR INFINITAS CASAS
            -A DAMA PODE COMER PRA TRAS
            ->A DAMA PODE COMER MULTIPLAS PEÇAS
            -A ULTIMA PEÇA A SER COMIDA PELA DAMA INDICA A POSIÇÃO FINAL QUE A DAMA DEVERÁ PARAR
            -SE O JOGADOR NAO CONSEGUIR EFETUAR UMA JOGADA, ELE PERDE
            */


    private final Tabuleiro tabuleiroLogico; 

    private int linhaOrigem = -1, colOrigem = -1;

    private int vez = 1;

    private boolean sequenciaCaptura = false;

    public MainInterfaceGrafica() {
        
        /*
            TABULEIRO DO JOGO
        */
        tabuleiroLogico = new Tabuleiro();

        setTitle("DISCIPLINA - IA - MINI JOGO DE DAMA");
        setSize(800, 800);
        setLayout(new GridLayout(TAMANHO, TAMANHO));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        inicializarComponentes();
        sincronizarInterface(); 

        setVisible(true);
    }

    private void inicializarComponentes() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                tabuleiroInterface[i][j] = new CasaBotao();

                // Cores do tabuleiro
                if ((i + j) % 2 == 0) {
                    tabuleiroInterface[i][j].setBackground(new Color(235, 235, 208)); // Bege
                } else {
                    tabuleiroInterface[i][j].setBackground(new Color(119, 149, 86));  // Verde
                }

                final int linha = i;
                final int coluna = j;
                tabuleiroInterface[i][j].addActionListener(e -> tratarClique(linha, coluna));
                add(tabuleiroInterface[i][j]);
            }
        }
    }

    private void tratarClique(int linha, int col) {

        boolean checkComer = false;

        // Verifica se qualquer peça da cor atual é OBRIGADA a comer
        for(int i = 0; i < TAMANHO; i++){
            for(int j = 0; j < TAMANHO; j++){
                char pecaNoTabuleiro = tabuleiroLogico.getMatriz()[i][j];
                // Importante: verificar se não é 'b' antes do % 2, pois o char 'b' é um número par na tabela ASCII
                if((pecaNoTabuleiro != 0) && (pecaNoTabuleiro != 'b') && (pecaNoTabuleiro % 2 == vez % 2)) {
                    if(PossoComer(i, j, sequenciaCaptura)){ 
                        checkComer = true;
                        break;
                    }
                }
            }
            if(checkComer) break;
        }
        
        // Caso 1: Nenhuma peça selecionada ainda
        if (linhaOrigem == -1) {
            
            char pecaClicada = tabuleiroLogico.getMatriz()[linha][col];
            // Verifica se a casa clicada contém QUALQUER peça da vez
            if ((pecaClicada != 0) && (pecaClicada != 'b') && (pecaClicada % 2 == vez % 2)) {
                
                // Regra: OBRIGATÓRIO COMER. Se tem captura no tabuleiro, mas a peça clicada não pode comer, bloqueia.
                if(checkComer && !PossoComer(linha, col, sequenciaCaptura)){
                    return;
                }
                
                linhaOrigem = linha;
                colOrigem = col;
                tabuleiroInterface[linha][col].setBackground(Color.YELLOW); // Destaque do clique
            }
        } 
        // Caso 2: Já existe uma peça selecionada, tentando mover
        else {
            
            // Se clicar na mesma peça, cancela a seleção (a menos que esteja no meio de uma sequência)
            if (linhaOrigem == linha && colOrigem == col) {
                if(!sequenciaCaptura) cancelarSelecao();
                return;
            }

            int peca = tabuleiroLogico.getMatriz()[linhaOrigem][colOrigem];
            int distLinha = Math.abs(linha - linhaOrigem);
            int distCol = Math.abs(col - colOrigem);
            
            boolean sucesso = false;
            boolean comeuAlguem = false;

            // MOVIMENTO NORMAL (só pode se não for obrigado a comer)
            if (!checkComer) {
                if (peca <= 2 && distLinha == 1 && distCol == 1) {
                    // Peça normal: Valida se está indo pra frente
                    if ((peca == 1 && linha < linhaOrigem) || (peca == 2 && linha > linhaOrigem)) {
                        sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                    }
                } else if (peca > 2 && distLinha == distCol) {
                    // Dama: Valida se o caminho está limpo
                    if (caminhoVazio(linhaOrigem, colOrigem, linha, col)) {
                        sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                    }
                }
            } 
            // MOVIMENTO DE CAPTURA
            else {
                if (distLinha == distCol) { // Movimento na diagonal
                    if (peca <= 2 && distLinha == 2) {
                        // Peça Normal comendo
                        boolean sentidoValido = true;
                        
                        // "A primeira captura tem que ser pra frente". Se for sequência, permite pra trás.
                        if (!sequenciaCaptura) {
                            if (peca == 1 && linha > linhaOrigem) sentidoValido = false;
                            if (peca == 2 && linha < linhaOrigem) sentidoValido = false;
                        }

                        if (sentidoValido) {
                            int linhaMeio = (linha + linhaOrigem) / 2;
                            int colMeio = (col + colOrigem) / 2;
                            char pecaMeio = tabuleiroLogico.getMatriz()[linhaMeio][colMeio];

                            if (pecaMeio != 0 && pecaMeio != 'b' && (pecaMeio % 2 != peca % 2)) {
                                sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                                if (sucesso) {
                                    tabuleiroLogico.getMatriz()[linhaMeio][colMeio] = 0; // Remove o inimigo
                                    comeuAlguem = true;
                                }
                            }
                        }
                    } else if (peca > 2) {
                        // Dama comendo
                        sucesso = tentarCapturaDama(linhaOrigem, colOrigem, linha, col);
                        if(sucesso) comeuAlguem = true;
                    }
                }
            }

            // AVALIA O RESULTADO DO MOVIMENTO
            if (sucesso) {
                sincronizarInterface();
                verificarFimDeJogo();

                // Verifica sequência de captura
                if (comeuAlguem && PossoComer(linha, col, true)) {
                    tabuleiroInterface[linhaOrigem][colOrigem].setBackground(new Color(119, 149, 86));
                
                    sequenciaCaptura = true;
                    linhaOrigem = linha;
                    colOrigem = col;
                    tabuleiroInterface[linha][col].setBackground(Color.YELLOW);
                } else {
                    // Passa a vez
                    if(vez == 1){
                        vez = 2;
                    }else{
                        vez = 1;
                    }
                    sequenciaCaptura = false;
                    cancelarSelecao();
                }
            } else {
                // Movimento inválido
                if(!sequenciaCaptura) cancelarSelecao();
            }
        }
    }

    private void cancelarSelecao() {
        if (linhaOrigem != -1) {
            // Restaura a cor original
            tabuleiroInterface[linhaOrigem][colOrigem].setBackground(new Color(119, 149, 86));
        }
        linhaOrigem = -1;
        colOrigem = -1;
    }

    private boolean moverPecaLogica(int r1, int c1, int r2, int c2) {
        
        // A casa de destino deve estar vazia
        if (tabuleiroLogico.getMatriz()[r2][c2] == 0) {
            
            // Transfere o valor (seja 1, 2, 3 ou 4) para a nova posição
            tabuleiroLogico.getMatriz()[r2][c2] = tabuleiroLogico.getMatriz()[r1][c1];
            tabuleiroLogico.getMatriz()[r1][c1] = 0;

            // Promoção simples para Dama 
            if (tabuleiroLogico.getMatriz()[r2][c2] == 2 && r2 == (TAMANHO - 1)) {
                tabuleiroLogico.getMatriz()[r2][c2] = 4;
            }
            if (tabuleiroLogico.getMatriz()[r2][c2] == 1 && r2 == 0) {
                tabuleiroLogico.getMatriz()[r2][c2] = 3;
            }

            return true;
        }
        return false;
    }

    // Seu método PossoComer completado
    private boolean PossoComer(int linha, int col, boolean Sequencia){
        int peca = tabuleiroLogico.getMatriz()[linha][col];
        if (peca == 0 || peca == 'b') return false;

        int[] dirLinha;
        int[] dirCol = new int[]{-1, 1}; // Sempre olha pra esquerda e direita

        // Se for dama OU for sequência de peça normal, pode olhar pra trás e pra frente
        if((peca > 2) || Sequencia){
            dirLinha = new int[]{-1,1};
        } else {
            // Peça normal fora de sequência: só frente
            dirLinha = (peca == 1) ? new int[]{-1} : new int[]{1};
        }

        for (int dl : dirLinha) {
            for (int dc : dirCol) {
                
                // Validação de captura para peças normais
                if (peca <= 2) {
                    int lMeio = linha + dl;
                    int cMeio = col + dc;
                    int lDestino = linha + (dl * 2);
                    int cDestino = col + (dc * 2);

                    // Checa limites do tabuleiro
                    if (lDestino >= 0 && lDestino < TAMANHO && cDestino >= 0 && cDestino < TAMANHO) {
                        char pecaMeio = tabuleiroLogico.getMatriz()[lMeio][cMeio];
                        char pecaDestino = tabuleiroLogico.getMatriz()[lDestino][cDestino];

                        if (pecaMeio != 0 && pecaMeio != 'b' && (pecaMeio % 2 != peca % 2) && pecaDestino == 0) {
                            return true;
                        }
                    }
                } 
                // Validação de captura para Damas
                else {
                    for (int i = 1; i < TAMANHO; i++) {
                        int lInimigo = linha + (dl * i);
                        int cInimigo = col + (dc * i);
                        int lDestino = lInimigo + dl;
                        int cDestino = cInimigo + dc;

                        if (lDestino < 0 || lDestino >= TAMANHO || cDestino < 0 || cDestino >= TAMANHO) break;

                        char pecaCaminho = tabuleiroLogico.getMatriz()[lInimigo][cInimigo];
                        if (pecaCaminho != 0 && pecaCaminho != 'b') {
                            if (pecaCaminho % 2 == peca % 2) break; // Bateu numa peça amiga
                            if (tabuleiroLogico.getMatriz()[lDestino][cDestino] == 0) return true; // Inimigo com espaço vazio atrás
                            break; // Duas peças coladas
                        }
                    }
                }
            }
        }
        return false;
    }
    
    // Métodos complementares baseados nas regras do seu amigo, adaptados para o seu char[][]
    private boolean caminhoVazio(int r1, int c1, int r2, int c2) {
        int dirLinha = (r2 > r1) ? 1 : -1;
        int dirCol = (c2 > c1) ? 1 : -1;
        int r = r1 + dirLinha;
        int c = c1 + dirCol;

        while (r != r2 && c != c2) {
            if (tabuleiroLogico.getMatriz()[r][c] != 0 && tabuleiroLogico.getMatriz()[r][c] != 'b') {
                return false; 
            }
            r += dirLinha;
            c += dirCol;
        }
        return true;
    }

    private boolean tentarCapturaDama(int r1, int c1, int r2, int c2) {
        int dirLinha = (r2 > r1) ? 1 : -1;
        int dirCol = (c2 > c1) ? 1 : -1;
        int pecaInimigaLinha = -1;
        int pecaInimigaCol = -1;
        int contadorInimigos = 0;

        int r = r1 + dirLinha;
        int c = c1 + dirCol;

        while (r != r2) {
            char pecaNoCaminho = tabuleiroLogico.getMatriz()[r][c];
            if (pecaNoCaminho != 0 && pecaNoCaminho != 'b') {
                // Verifico se é peça do próprio time
                if (pecaNoCaminho % 2 == tabuleiroLogico.getMatriz()[r1][c1] % 2) return false;
                
                contadorInimigos++;
                pecaInimigaLinha = r;
                pecaInimigaCol = c;
            }
            r += dirLinha;
            c += dirCol;
        }

        // Tem que ter exatamente UMA peça inimiga no caminho desse pulo específico
        if (contadorInimigos == 1) {
            int rAposInimiga = pecaInimigaLinha + dirLinha;
            int cAposInimiga = pecaInimigaCol + dirCol;

            // Se o destino é valido após a peça (a Dama pode parar em qualquer lugar após comer)
            if (r2 >= rAposInimiga || r2 <= rAposInimiga) {
                if (moverPecaLogica(r1, c1, r2, c2)) {
                    tabuleiroLogico.getMatriz()[pecaInimigaLinha][pecaInimigaCol] = 0;
                    return true;
                }
            }
        }
        return false;
    }

    private void verificarFimDeJogo() {
        int brancas = 0, pretas = 0;
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                char p = tabuleiroLogico.getMatriz()[i][j];
                if (p == 1 || p == 3) brancas++;
                if (p == 2 || p == 4) pretas++;
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
    
    /*
     * Atualiza a interface gráfica com base na matriz lógica do Tabuleiro. Este
     * método será chamado após cada jogada da IA.
     */
    public void sincronizarInterface() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                int peca = tabuleiroLogico.getMatriz()[i][j];
                tabuleiroInterface[i][j].setTipoPeca(peca);
            }
        }
    }

    private class CasaBotao extends JButton {

        private int tipoPeca = 0;

        public void setTipoPeca(int tipo) {
            this.tipoPeca = tipo;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int margem = 10;
            // Brancas
            if (tipoPeca == 1 || tipoPeca == 3) { 
                g2.setColor(Color.WHITE);
                g2.fillOval(margem, margem, getWidth() - 2 * margem, getHeight() - 2 * margem);
                g2.setColor(Color.BLACK);
                g2.drawOval(margem, margem, getWidth() - 2 * margem, getHeight() - 2 * margem);
            // Pretas
            } else if (tipoPeca == 2 || tipoPeca == 4) { 
                g2.setColor(Color.BLACK);
                g2.fillOval(margem, margem, getWidth() - 2 * margem, getHeight() - 2 * margem);
            }

            // Representação de Dama (uma borda dourada)
            if (tipoPeca > 2 && tipoPeca != 'b') { 
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(margem + 5, margem + 5, getWidth() - 2 * margem - 10, getHeight() - 2 * margem - 10);
            }
        }
    }
}
package main;

import java.util.ArrayList;

public class Arvore {

    private Node raiz;
    private int profundidadeMaxima;
    private static final int TAMANHO = 6;

    // Mapa para traduzir linha/coluna para as letras exigidas pelo seu Node
    private static final char[][] MAPA_LETRAS = {
            { 0, 'A', 0, 'B', 0, 'C' },
            { 'D', 0, 'E', 0, 'F', 0 },
            { 0, 'G', 0, 'H', 0, 'I' },
            { 'J', 0, 'K', 0, 'L', 0 },
            { 0, 'M', 0, 'N', 0, 'O' },
            { 'P', 0, 'Q', 0, 'R', 0 }
    };

    public Arvore(Tabuleiro tabuleiro, boolean turnoBrancas) {
        this(tabuleiro, turnoBrancas, 10); 
    }

    public Arvore(Tabuleiro tabuleiro, boolean turnoBrancas, int profundidadeMaxima) {
        this.profundidadeMaxima = profundidadeMaxima;
        this.raiz = new Node();
        
        this.raiz.setMatrix(copiarMatriz(tabuleiro.getMatriz()));
        this.raiz.setTurn(turnoBrancas);
        
        int vez = turnoBrancas ? 1 : 2;
        
        // Pega as opções do presente (Nível 1)
        ArrayList<int[]> jogadasDaRaiz = gerarJogadasPossiveis(tabuleiro, vez);

        int bestValue = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        // Instancia na memória APENAS os filhos diretos
        for (int[] jogada : jogadasDaRaiz) {
            int lOrig = jogada[0], cOrig = jogada[1], lDest = jogada[2], cDest = jogada[3];

            // Simula o movimento
            Tabuleiro tFilho = tabuleiro.clone();
            aplicarMovimentoSimulado(tFilho, lOrig, cOrig, lDest, cDest);

            // Cria o nó
            Node filho = new Node();
            filho.setOrigin(MAPA_LETRAS[lOrig][cOrig]);
            filho.setDest(MAPA_LETRAS[lDest][cDest]);
            filho.setMatrix(copiarMatriz(tFilho.getMatriz()));
            filho.setTurn(!turnoBrancas);

            // Manda a IA avaliar o futuro DENTRO da memória RAM usando poda Alpha-Beta
            int score = calcularAlphaBeta(tFilho, 1, alpha, beta, !turnoBrancas);
            filho.setMinMax(score);
            
            this.raiz.addChild(filho);

            // A Raiz sempre quer o MAIOR valor possível para o turno dela
            if (score > bestValue) {
                bestValue = score;
                // SALVA A MEMÓRIA: Guarda qual foi a jogada que deu a nota máxima!
                this.raiz.setMelhorFilho(filho); 
            }
            alpha = Math.max(alpha, bestValue);
        }
        
        this.raiz.setMinMax(bestValue);
    }

    // Prevê o futuro sem gastar a memória instanciando Nodes
    private int calcularAlphaBeta(Tabuleiro tabAtual, int profundidade, int alpha, int beta, boolean turnoBrancas) {
        // Condição de parada (Limite de visão)
        if (profundidade >= profundidadeMaxima) {
            return avaliarTabuleiro(tabAtual.getMatriz());
        }

        int vez = turnoBrancas ? 1 : 2;
        ArrayList<int[]> jogadas = gerarJogadasPossiveis(tabAtual, vez);

        // Se o jogador está travado sem jogadas
        if (jogadas.isEmpty()) {
            return turnoBrancas ? -10000 : 10000;
        }

        if (turnoBrancas == this.raiz.isTurn()) { 
            int maxEval = Integer.MIN_VALUE;
            for (int[] jogada : jogadas) {
                Tabuleiro clone = tabAtual.clone();
                aplicarMovimentoSimulado(clone, jogada[0], jogada[1], jogada[2], jogada[3]);

                int eval = calcularAlphaBeta(clone, profundidade + 1, alpha, beta, !turnoBrancas);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                
                if (beta <= alpha) break; // Poda (corta a árvore)
            }
            return maxEval;
        } 
        else { 
            int minEval = Integer.MAX_VALUE;
            for (int[] jogada : jogadas) {
                Tabuleiro clone = tabAtual.clone();
                aplicarMovimentoSimulado(clone, jogada[0], jogada[1], jogada[2], jogada[3]);

                int eval = calcularAlphaBeta(clone, profundidade + 1, alpha, beta, !turnoBrancas);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                
                if (beta <= alpha) break; // Poda Alpha-Beta
            }
            return minEval;
        }
    }

    // Heurística simples: Conta as peças e dá peso maior para as damas
    private int avaliarTabuleiro(char[][] m) {
        int pontuacao = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                char peca = m[i][j];
                if (peca == '2') pontuacao += 10;       // Peça normal da IA
                else if (peca == '4') pontuacao += 30;  // Dama da IA
                else if (peca == '1') pontuacao -= 10;  // Peça normal do Jogador
                else if (peca == '3') pontuacao -= 30;  // Dama do Jogador
            }
        }
        return pontuacao;
    }

    private ArrayList<int[]> gerarJogadasPossiveis(Tabuleiro tab, int vez) {
        ArrayList<int[]> movimentos = new ArrayList<>();
        ArrayList<int[]> capturas = new ArrayList<>();
        char[][] m = tab.getMatriz();

        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                char peca = m[i][j];
                if (peca != '0' && peca != 'b' && (peca % 2 == vez % 2)) {
                    buscarCapturas(m, i, j, peca, capturas);
                    if (capturas.isEmpty()) {
                        buscarMovimentosNormais(m, i, j, peca, movimentos);
                    }
                }
            }
        }
        return capturas.isEmpty() ? movimentos : capturas;
    }

    private void buscarMovimentosNormais(char[][] m, int l, int c, char peca, ArrayList<int[]> movs) {
        if (peca <= '2') { // Peça normal
            int dir = (peca == '1') ? -1 : 1;
            for (int dc : new int[]{-1, 1}) {
                int nl = l + dir, nc = c + dc;
                if (dentro(nl, nc) && m[nl][nc] == '0') {
                    movs.add(new int[]{l, c, nl, nc});
                }
            }
        } else { // Dama
            for (int dl : new int[]{-1, 1}) {
                for (int dc : new int[]{-1, 1}) {
                    int nl = l + dl, nc = c + dc;
                    while (dentro(nl, nc) && m[nl][nc] == '0') {
                        movs.add(new int[]{l, c, nl, nc});
                        nl += dl; nc += dc;
                    }
                }
            }
        }
    }

    private void buscarCapturas(char[][] m, int l, int c, char peca, ArrayList<int[]> caps) {
        if (peca <= '2') {
            int[] sentidos = (peca == '1') ? new int[]{-1} : new int[]{1};
            for (int dl : sentidos) { 
                for (int dc : new int[]{-1, 1}) {
                    int ml = l + dl, mc = c + dc;
                    int dl2 = l + (dl * 2), dc2 = c + (dc * 2);
                    if (dentro(dl2, dc2) && m[ml][mc] != '0' && m[ml][mc] != 'b' && m[ml][mc] % 2 != peca % 2 && m[dl2][dc2] == '0') {
                        caps.add(new int[]{l, c, dl2, dc2});
                    }
                }
            }
        } else { // Dama captura
            for (int dl : new int[]{-1, 1}) {
                for (int dc : new int[]{-1, 1}) {
                    int nl = l + dl, nc = c + dc;
                    while(dentro(nl, nc)) {
                        char pCam = m[nl][nc];
                        if (pCam != '0' && pCam != 'b') {
                            if (pCam % 2 == peca % 2) break; // Bateu em peça aliada
                            int dl2 = nl + dl, dc2 = nc + dc;
                            if (dentro(dl2, dc2) && m[dl2][dc2] == '0') {
                                caps.add(new int[]{l, c, dl2, dc2});
                            }
                            break;
                        }
                        nl += dl; nc += dc;
                    }
                }
            }
        }
    }

    private void aplicarMovimentoSimulado(Tabuleiro tab, int lOrig, int cOrig, int lDest, int cDest) {
        char[][] m = tab.getMatriz();
        m[lDest][cDest] = m[lOrig][cOrig];
        m[lOrig][cOrig] = '0';

        // Se pulou 2 ou mais casas de distância (captura), remove a peça inimiga do caminho
        if (Math.abs(lDest - lOrig) >= 2) {
            int dirL = Integer.signum(lDest - lOrig);
            int dirC = Integer.signum(cDest - cOrig);
            int nl = lOrig + dirL, nc = cOrig + dirC;
            while (nl != lDest && nc != cDest) {
                m[nl][nc] = '0'; // Limpa a casa do inimigo
                nl += dirL; nc += dirC;
            }
        }

        // Regra de Promoção à Dama
        if (m[lDest][cDest] == '1' && lDest == 0) m[lDest][cDest] = '3';
        if (m[lDest][cDest] == '2' && lDest == TAMANHO - 1) m[lDest][cDest] = '4';
    }

    private boolean dentro(int l, int c) {
        return l >= 0 && l < TAMANHO && c >= 0 && c < TAMANHO;
    }

    // Garante que os nós não vão compartilhar a mesma referência de matriz de memória
    private char[][] copiarMatriz(char[][] original) {
        char[][] copia = new char[TAMANHO][TAMANHO];
        for (int i = 0; i < TAMANHO; i++) {
            System.arraycopy(original[i], 0, copia[i], 0, TAMANHO);
        }
        return copia;
    }

    public Node getRaiz() {
        return raiz;
    }
}
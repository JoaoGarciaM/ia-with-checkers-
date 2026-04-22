package main;

import java.util.ArrayList;

public class Arvore {

    private Node raiz;
    private int profundidadeMaxima;
    private static final int TAMANHO = 6;
    
    private boolean iaUsaBrancas; 

    public static int nosAvaliados = 0;

    private static final char[][] MAPA_LETRAS = {
            { 0, 'A', 0, 'B', 0, 'C' },
            { 'D', 0, 'E', 0, 'F', 0 },
            { 0, 'G', 0, 'H', 0, 'I' },
            { 'J', 0, 'K', 0, 'L', 0 },
            { 0, 'M', 0, 'N', 0, 'O' },
            { 'P', 0, 'Q', 0, 'R', 0 }
    };

    public Arvore(Tabuleiro tabuleiro, boolean turnoBrancas, int profundidadeMaxima) {
        nosAvaliados = 0;
        this.profundidadeMaxima = profundidadeMaxima;
        this.iaUsaBrancas = turnoBrancas; // Salva a identidade da IA
        this.raiz = new Node();
        
        this.raiz.setMatrix(copiarMatriz(tabuleiro.getMatriz()));
        this.raiz.setTurn(turnoBrancas);
        
        int vez = turnoBrancas ? 1 : 2;
        ArrayList<int[]> jogadasDaRaiz = gerarJogadasPossiveis(tabuleiro, vez);

        int bestValue = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (int[] jogada : jogadasDaRaiz) {
            int lOrig = jogada[0], cOrig = jogada[1], lDest = jogada[2], cDest = jogada[3];

            Tabuleiro tFilho = tabuleiro.clone();
            aplicarMovimentoSimulado(tFilho, lOrig, cOrig, lDest, cDest);

            Node filho = new Node();
            filho.setOrigin(MAPA_LETRAS[lOrig][cOrig]);
            filho.setDest(MAPA_LETRAS[lDest][cDest]);
            filho.setMatrix(copiarMatriz(tFilho.getMatriz()));
            filho.setTurn(!turnoBrancas);

            int score = calcularAlphaBeta(tFilho, 1, alpha, beta, !turnoBrancas);
            filho.setMinMax(score);
            
            this.raiz.addChild(filho);

            if (score > bestValue) {
                bestValue = score;
                this.raiz.setMelhorFilho(filho); 
            }
            alpha = Math.max(alpha, bestValue);
        }
        
        this.raiz.setMinMax(bestValue);
    }

    private int calcularAlphaBeta(Tabuleiro tabAtual, int profundidade, int alpha, int beta, boolean turnoBrancas) {
        nosAvaliados++;
        if (profundidade >= profundidadeMaxima) {
            return avaliarTabuleiro(tabAtual.getMatriz());
        }

        int vez = turnoBrancas ? 1 : 2;
        ArrayList<int[]> jogadas = gerarJogadasPossiveis(tabAtual, vez);

        // CORREÇÃO DO BUG SUICIDA: 
        if (jogadas.isEmpty()) {
            // Se quem não tem jogada é a IA, isso é péssimo (-10000). Se for o Humano, é vitória da IA (+10000)
            return (turnoBrancas == iaUsaBrancas) ? -10000 : 10000;
        }

        if (turnoBrancas == iaUsaBrancas) { // Turno da IA (Maximizando)
            int maxEval = Integer.MIN_VALUE;
            for (int[] jogada : jogadas) {
                Tabuleiro clone = tabAtual.clone();
                aplicarMovimentoSimulado(clone, jogada[0], jogada[1], jogada[2], jogada[3]);

                int eval = calcularAlphaBeta(clone, profundidade + 1, alpha, beta, !turnoBrancas);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                
                if (beta <= alpha) break; 
            }
            return maxEval;
        } 
        else { // Turno do Humano (Minimizando)
            int minEval = Integer.MAX_VALUE;
            for (int[] jogada : jogadas) {
                Tabuleiro clone = tabAtual.clone();
                aplicarMovimentoSimulado(clone, jogada[0], jogada[1], jogada[2], jogada[3]);

                int eval = calcularAlphaBeta(clone, profundidade + 1, alpha, beta, !turnoBrancas);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                
                if (beta <= alpha) break; 
            }
            return minEval;
        }
    }

    private int avaliarTabuleiro(char[][] m) {
        int pontuacao = 0;
        
        // Define quem é quem de forma dinâmica
        char minhaPeca = iaUsaBrancas ? '1' : '2';
        char minhaDama = iaUsaBrancas ? '3' : '4';

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                char peca = m[i][j];
                
                if (peca == '0' || peca == 'b') continue;

                int valorPeca = 0;
                boolean ehMinha = (peca == minhaPeca || peca == minhaDama);
                boolean ehDama = (peca == '3' || peca == '4');

                // 1. VIDA (Dama vale mais que peça normal)
                valorPeca += ehDama ? 300 : 100;

                // 2. POSIÇÃO (Centro e Bordas)
                if ((i == 2 || i == 3) && (j == 2 || j == 3)) valorPeca += 15;
                if (j == 0 || j == 5) valorPeca += 10;

                // 3. DEFESA DE BASE DEPENDENDO DA COR
                if (peca == '2' || peca == '4') { // Pretas (Iniciam em cima e descem. Base é 0)
                    if (i == 0) valorPeca += 40;
                    if (i > 0 && j > 0 && (m[i-1][j-1] == '2' || m[i-1][j-1] == '4')) valorPeca += 15;
                    if (i > 0 && j < 5 && (m[i-1][j+1] == '2' || m[i-1][j+1] == '4')) valorPeca += 15;
                } else { // Brancas (Iniciam embaixo e sobem. Base é 5)
                    if (i == 5) valorPeca += 40;
                    if (i < 5 && j > 0 && (m[i+1][j-1] == '1' || m[i+1][j-1] == '3')) valorPeca += 15;
                    if (i < 5 && j < 5 && (m[i+1][j+1] == '1' || m[i+1][j+1] == '3')) valorPeca += 15;
                }

                // Se a peça for da IA, soma. Se for sua, subtrai!
                pontuacao += ehMinha ? valorPeca : -valorPeca;
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
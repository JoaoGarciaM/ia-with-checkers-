package main;

public class Regras {

    private static final int TAMANHO = 6;

    /**
     * Verifica se qualquer peça do jogador da vez tem uma captura obrigatória.
     */
    public static boolean alguemPodeComer(Tabuleiro tabuleiro, int vez, boolean sequenciaCaptura) {
        char[][] m = tabuleiro.getMatriz();
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                char peca = m[i][j];
                // Verifica se a peça pertence ao jogador da vez
                if ((peca != '0') && (peca != 'b') && (peca % 2 == vez % 2)) {
                    if (podeComer(tabuleiro, i, j, sequenciaCaptura)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Verifica se uma peça específica pode realizar uma captura.
     */
    public static boolean podeComer(Tabuleiro tabuleiro, int linha, int col, boolean sequencia) {
        char[][] m = tabuleiro.getMatriz();
        char peca = m[linha][col];
        if (peca == '0' || peca == 'b') return false;

        int[] dirLinha;
        int[] dirCol = new int[]{-1, 1}; 

        // Regra: Damas ou peças em combo podem olhar para trás para comer
        if ((peca > '2') || sequencia) {
            dirLinha = new int[]{-1, 1};
        } else {
            // Peças comuns no primeiro movimento só comem para frente
            dirLinha = (peca == '1') ? new int[]{-1} : new int[]{1};
        }

        for (int dl : dirLinha) {
            for (int dc : dirCol) {
                if (peca <= '2') {
                    // Lógica para Peça Comum (pula exatamente 2 casas)
                    int lMeio = linha + dl;
                    int cMeio = col + dc;
                    int lDestino = linha + (dl * 2);
                    int cDestino = col + (dc * 2);

                    if (lDestino >= 0 && lDestino < TAMANHO && cDestino >= 0 && cDestino < TAMANHO) {
                        char pecaMeio = m[lMeio][cMeio];
                        char pecaDestino = m[lDestino][cDestino];

                        if (pecaMeio != '0' && pecaMeio != 'b' && (pecaMeio % 2 != peca % 2) && pecaDestino == '0') {
                            return true;
                        }
                    }
                } else { 
                    // Lógica para Dama (varre a diagonal procurando inimigo com espaço atrás)
                    for (int i = 1; i < TAMANHO; i++) {
                        int lInimigo = linha + (dl * i);
                        int cInimigo = col + (dc * i);
                        int lDestino = lInimigo + dl;
                        int cDestino = cInimigo + dc;

                        if (lDestino < 0 || lDestino >= TAMANHO || cDestino < 0 || cDestino >= TAMANHO) break;

                        char pecaCaminho = m[lInimigo][cInimigo];
                        if (pecaCaminho != '0' && pecaCaminho != 'b') {
                            if (pecaCaminho % 2 == peca % 2) break; // Bloqueio por peça aliada
                            if (m[lDestino][cDestino] == '0') return true; 
                            break; 
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Verifica se o caminho diagonal está livre (usado para movimento da Dama).
     */
    public static boolean caminhoVazio(Tabuleiro tabuleiro, int r1, int c1, int r2, int c2) {
        char[][] m = tabuleiro.getMatriz();
        int dirLinha = (r2 > r1) ? 1 : -1;
        int dirCol = (c2 > c1) ? 1 : -1;
        int r = r1 + dirLinha;
        int c = c1 + dirCol;

        while (r != r2 && c != c2) {
            if (m[r][c] != '0' && m[r][c] != 'b') {
                return false;
            }
            r += dirLinha;
            c += dirCol;
        }
        return true;
    }

    /**
     * Tenta validar uma captura de Dama e retorna a posição do inimigo removido.
     */
    public static int[] tentarCapturaDama(Tabuleiro tabuleiro, int r1, int c1, int r2, int c2) {
        char[][] m = tabuleiro.getMatriz();
        int dirLinha = (r2 > r1) ? 1 : -1;
        int dirCol = (c2 > c1) ? 1 : -1;
        int pecaInimigaLinha = -1;
        int pecaInimigaCol = -1;
        int contadorInimigos = 0;

        if (Math.abs(r2 - r1) != Math.abs(c2 - c1)) return null;

        int r = r1 + dirLinha;
        int c = c1 + dirCol;

        while (r != r2) {
            char pecaNoCaminho = m[r][c];
            if (pecaNoCaminho != '0' && pecaNoCaminho != 'b') {
                if (pecaNoCaminho % 2 == m[r1][c1] % 2) return null; // Peça amiga no caminho
                contadorInimigos++;
                pecaInimigaLinha = r;
                pecaInimigaCol = c;
            }
            r += dirLinha;
            c += dirCol;
        }

        // Regra simplificada: Dama captura e para logo após a peça inimiga
        if (contadorInimigos == 1) {
            int rApos = pecaInimigaLinha + dirLinha;
            int cApos = pecaInimigaCol + dirCol;
            if (r2 == rApos && c2 == cApos) {
                return new int[]{pecaInimigaLinha, pecaInimigaCol};
            }
        }
        return null;
    }

    public static boolean temMovimentoDisponivel(Tabuleiro tabuleiro, int vez) {
        char[][] m = tabuleiro.getMatriz();
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                char peca = m[i][j];
                if (peca != '0' && peca != 'b' && (peca % 2 == vez % 2)) {
                    // 1. Tem captura disponível?
                    if (podeComer(tabuleiro, i, j, false)) return true;

                    // 2. Tem movimento normal disponível?
                    if (peca <= '2') { // Comum
                        int dir = (peca == '1') ? -1 : 1;
                        for (int dc : new int[]{-1, 1}) {
                            int nl = i + dir, nc = j + dc;
                            if (nl >= 0 && nl < TAMANHO && nc >= 0 && nc < TAMANHO && m[nl][nc] == '0') return true;
                        }
                    } else { // Dama
                        for (int dl : new int[]{-1, 1}) {
                            for (int dc : new int[]{-1, 1}) {
                                int nl = i + dl, nc = j + dc;
                                if (nl >= 0 && nl < TAMANHO && nc >= 0 && nc < TAMANHO && m[nl][nc] == '0') return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static int getMaiorComboDoJogador(Tabuleiro tabuleiro, int vez) {
        int maxGeral = 0;
        char[][] m = tabuleiro.getMatriz();
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                char peca = m[i][j];
                if ((peca != '0') && (peca != 'b') && (peca % 2 == vez % 2)) {
                    int combo = getMaiorComboDaPeca(tabuleiro, i, j, false);
                    if (combo > maxGeral) {
                        maxGeral = combo;
                    }
                }
            }
        }
        return maxGeral;
    }


    public static int getMaiorComboDaPeca(Tabuleiro tab, int linha, int col, boolean sequencia) {
        if (!podeComer(tab, linha, col, sequencia)) return 0;
        
        char[][] m = tab.getMatriz();
        char peca = m[linha][col];
        int maxBranch = 0;

        int[] dirLinha = ((peca > '2') || sequencia) ? new int[]{-1, 1} : ((peca == '1') ? new int[]{-1} : new int[]{1});
        int[] dirCol = new int[]{-1, 1};

        for (int dl : dirLinha) {
            for (int dc : dirCol) {
                if (peca <= '2') { // Simulação para Peça Comum
                    int lMeio = linha + dl, cMeio = col + dc;
                    int lDest = linha + (dl * 2), cDest = col + (dc * 2);
                    
                    if (lDest >= 0 && lDest < TAMANHO && cDest >= 0 && cDest < TAMANHO) {
                        if (m[lMeio][cMeio] != '0' && m[lMeio][cMeio] != 'b' && m[lMeio][cMeio] % 2 != peca % 2 && m[lDest][cDest] == '0') {
                            Tabuleiro clone = tab.clone();
                            clone.getMatriz()[lDest][cDest] = clone.getMatriz()[linha][col];
                            clone.getMatriz()[linha][col] = '0';
                            clone.getMatriz()[lMeio][cMeio] = '0'; 
                            
                            int capturas = 1 + getMaiorComboDaPeca(clone, lDest, cDest, true);
                            if (capturas > maxBranch) maxBranch = capturas;
                        }
                    }
                } else { // Simulação para Dama
                    for (int i = 1; i < TAMANHO; i++) {
                        int lInimigo = linha + (dl * i), cInimigo = col + (dc * i);
                        int lDest = lInimigo + dl, cDest = cInimigo + dc;
                        
                        if (lDest < 0 || lDest >= TAMANHO || cDest < 0 || cDest >= TAMANHO) break;
                        
                        char pCaminho = m[lInimigo][cInimigo];
                        if (pCaminho != '0' && pCaminho != 'b') {
                            if (pCaminho % 2 == peca % 2) break; // Bloqueado por aliada
                            if (m[lDest][cDest] == '0') {
                                Tabuleiro clone = tab.clone();
                                clone.getMatriz()[lDest][cDest] = clone.getMatriz()[linha][col];
                                clone.getMatriz()[linha][col] = '0';
                                clone.getMatriz()[lInimigo][cInimigo] = '0'; 
                                
                                int capturas = 1 + getMaiorComboDaPeca(clone, lDest, cDest, true);
                                if (capturas > maxBranch) maxBranch = capturas;
                            }
                            break; 
                        }
                    }
                }
            }
        }
        return maxBranch;
    }
}
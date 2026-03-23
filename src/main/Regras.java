package main;

public class Regras {

    private static final int TAMANHO = 6;

    public static boolean alguemPodeComer(Tabuleiro tabuleiro, int vez, boolean sequenciaCaptura) {
        char[][] m = tabuleiro.getMatriz();
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                char peca = m[i][j];
                if ((peca != '0') && (peca != 'b') && (peca % 2 == vez % 2)) {
                    if (podeComer(tabuleiro, i, j, sequenciaCaptura)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean podeComer(Tabuleiro tabuleiro, int linha, int col, boolean sequencia) {
        char[][] m = tabuleiro.getMatriz();
        char peca = m[linha][col];
        if (peca == '0' || peca == 'b') return false;

        int[] dirLinha;
        int[] dirCol = new int[]{-1, 1}; 

        if ((peca > '2') || sequencia) {
            dirLinha = new int[]{-1, 1};
        } else {
            dirLinha = (peca == '1') ? new int[]{-1} : new int[]{1};
        }

        for (int dl : dirLinha) {
            for (int dc : dirCol) {
                if (peca <= '2') {
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
                    for (int i = 1; i < TAMANHO; i++) {
                        int lInimigo = linha + (dl * i);
                        int cInimigo = col + (dc * i);
                        int lDestino = lInimigo + dl;
                        int cDestino = cInimigo + dc;

                        if (lDestino < 0 || lDestino >= TAMANHO || cDestino < 0 || cDestino >= TAMANHO) break;

                        char pecaCaminho = m[lInimigo][cInimigo];
                        if (pecaCaminho != '0' && pecaCaminho != 'b') {
                            if (pecaCaminho % 2 == peca % 2) break; 
                            if (m[lDestino][cDestino] == '0') return true; 
                            break; 
                        }
                    }
                }
            }
        }
        return false;
    }

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

    public static int[] tentarCapturaDama(Tabuleiro tabuleiro, int r1, int c1, int r2, int c2) {
        char[][] m = tabuleiro.getMatriz();
        int dirLinha = (r2 > r1) ? 1 : -1;
        int dirCol = (c2 > c1) ? 1 : -1;
        int pecaInimigaLinha = -1;
        int pecaInimigaCol = -1;
        int contadorInimigos = 0;

        int distLinha = Math.abs(r2 - r1);
        int distCol = Math.abs(c2 - c1);
        if (distLinha != distCol) return null;

        int r = r1 + dirLinha;
        int c = c1 + dirCol;

        while (r != r2) {
            char pecaNoCaminho = m[r][c];
            if (pecaNoCaminho != '0' && pecaNoCaminho != 'b') {
                if (pecaNoCaminho % 2 == m[r1][c1] % 2) return null;
                contadorInimigos++;
                pecaInimigaLinha = r;
                pecaInimigaCol = c;
            }
            r += dirLinha;
            c += dirCol;
        }

        if (contadorInimigos == 1) {
            int distInimigoDestino = Math.abs(r2 - pecaInimigaLinha);
            if (distInimigoDestino == 1) {
                return new int[]{pecaInimigaLinha, pecaInimigaCol};
            }
        }
        return null;
    }
}
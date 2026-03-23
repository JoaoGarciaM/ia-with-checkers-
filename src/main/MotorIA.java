package main;

import java.util.ArrayList;

public class MotorIA {
    
    public static void minMaxJogoDama(Node node) {

        // Se o nó não tem filhos, chegamos no limite da profundidade ou no fim do jogo
        if (node.getChild().isEmpty()) {
            int nota = aplicarHeuristicaVerificacaoGanhador(node);
            node.setMinMax(nota);
        } else {
            for (int i = 0; i < node.getChild().size(); i++) {
                Node child = node.getChild().get(i);
                if (child.getMinMax() == Integer.MIN_VALUE) {
                    minMaxJogoDama(child);
                }
            }

            
            if (node.isTurn()) { 
                int min = minimo(node.getChild());
                node.setMinMax(min);
            } else { 
                int max = maximo(node.getChild());
                node.setMinMax(max);
            }
        }
    }

    
    private static int aplicarHeuristicaVerificacaoGanhador(Node node) {
        char[][] m = node.getMatrix();
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

    /**
     * Varre a lista de filhos e retorna a MENOR nota encontrada.
     */
    private static int minimo(ArrayList<Node> filhos) {
        int menor = Integer.MAX_VALUE;
        for (Node filho : filhos) {
            if (filho.getMinMax() < menor) {
                menor = filho.getMinMax();
            }
        }
        return menor;
    }

    /**
     * Varre a lista de filhos e retorna a MAIOR nota encontrada.
     */
    private static int maximo(ArrayList<Node> filhos) {
        int maior = Integer.MIN_VALUE;
        for (Node filho : filhos) {
            if (filho.getMinMax() > maior) {
                maior = filho.getMinMax();
            }
        }
        return maior;
    }
}
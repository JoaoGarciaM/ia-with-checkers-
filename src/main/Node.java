package main;

import java.util.ArrayList;

public class Node {
    
    private char origin;
    private char dest;
    private boolean turn; // TRUE - white; FALSE - black;
    private char[][] matrix;
    private int minMax;
    private ArrayList<Node> children;
    
    // A PEÇA CHAVE: Guarda a melhor jogada futura
    private Node melhorFilho; 
    
    public Node() {
        this.children = new ArrayList<>();
        this.minMax = Integer.MIN_VALUE;
        this.melhorFilho = null; // Inicializa vazio
    }

    // GETTERS E SETTERS 
   
    public Node getMelhorFilho() {
        return melhorFilho;
    }

    public void setMelhorFilho(Node melhorFilho) {
        this.melhorFilho = melhorFilho;
    }


    public int getMinMax() {
        return minMax;
    }

    public void setMinMax(int minMax) {
        this.minMax = minMax;
    }
    
    public ArrayList<Node> getChild() {
        return this.children;
    }
    
    public void addChild(Node child) {
        this.children.add(child);
    }
    
    public char getOrigin() {
        return origin;
    }

    public void setOrigin(char origin) {
        this.origin = origin;
    }

    public char getDest() {
        return dest;
    }

    public void setDest(char dest) {
        this.dest = dest;
    }

    public boolean isTurn() {
        return turn;
    }

    public void setTurn(boolean turn) {
        this.turn = turn;
    }

    public char[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(char[][] matrix) {
        this.matrix = matrix;
    }
}
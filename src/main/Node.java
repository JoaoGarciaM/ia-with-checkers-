/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.ArrayList;

/**
 *
 * @author Douglas
 */
public class Node {
    
    private char origin;
    private char dest;
    private boolean turn; //TRUE - white; FALSE - black;
    private char[][] matrix;
    private int minMax;
    private ArrayList<Node> children;
    
    public Node () {
        this.children = new ArrayList<>();
        this.minMax = Integer.MIN_VALUE;
    }

    public int getMinMax() {
        return minMax;
    }

    public void setMinMax(int minMax) {
        this.minMax = minMax;
    }
    
    
    
    public ArrayList<Node> getChild (){
        return this.children;
    }
    
    public void addChild (Node child){
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

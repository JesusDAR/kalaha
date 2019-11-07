/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

/**
 *
 * @author albam
 */
 public class GameTree
    {
        //ATTRIBUTES
        private int score = 1; //here we store de current score of this node
        private int move = 1;  //here we store de current move of this node
        private boolean outOfTime = false; //here we store if we have reach the 
                                           //max time for the deeping search
        private boolean endOfTree = false; //here we store if we have reach the end of the
                                           //tree and we already found a winner
        //CONSTRUCTORS
        public GameTree(int score, int move, boolean outOfTime, boolean endOfTree)
        {
           this.score = score;
           this.move = move;
           this.outOfTime = outOfTime;
           this.endOfTree = endOfTree;
        }
       
        //GETTERS
        public int getScore(){
            return score;
        }
        
        public int getMove(){
            return move;
        }
        
        public boolean getOutTime(){
            return outOfTime;
        }
        
        public boolean getEndTree(){
            return endOfTree;
        }
    }

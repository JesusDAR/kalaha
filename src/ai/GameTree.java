package ai;

public class GameTree
    {
            private int score; //Current score of this node
            private int move; //Current move of this node
            private boolean endTime; //If we have reach the  max time for the deeping search
            private boolean limitTree; //If we have reach the end of the tree and we've already found a winner

            public GameTree(int score, int move, boolean endTime, boolean limitTree)
            {
               this.score = score;
               this.move = move;
               this.endTime = endTime;
               this.limitTree = limitTree;
            }
        
        //GETTERS
        public int getScore(){return score;}
        
        public int getMove(){return move;}
        
        public boolean getEndTime(){return endTime;}
        
        public boolean getLimitTree(){return limitTree;}
    }
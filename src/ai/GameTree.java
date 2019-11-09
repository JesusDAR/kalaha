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
        private boolean endTime = false; //here we store if we have reach the  max time for the deeping search
        private boolean LimitTree = false; //here we store if we have reach the end of the tree and we've already found a winner

        //CONSTRUCTORS
        public GameTree(int score, int move, boolean endTime, boolean LimitTree)
        {
           this.score = score;
           this.move = move;
           this.endTime = endTime;
           this.LimitTree = LimitTree;
        }
       
        //GETTERS
        public int getScore(){return score;}
        
        public int getMove(){return move;}
        
        public boolean getEndTime(){return endTime;}
        
        public boolean getLimitTree(){return LimitTree;}
    }

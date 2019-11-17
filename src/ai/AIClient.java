package ai;

import ai.Global;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import kalaha.*;


/**
 * This is the main class for your Kalaha AI bot. Currently
 * it only makes a random, valid move each turn.
 * 
 * @author Johan HagelbÃ¤ck
 */
public class AIClient implements Runnable
{
    private int player;
    private JTextArea text;
    
    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;
    private boolean connected;
    
    	
    /**
     * Creates a new client.
     */
    public AIClient()
    {
        player = -1;
        connected = false;
        
        //This is some necessary client stuff. You don't need
        //to change anything here.
        initGUI();
	
        try
        {
            addText("Connecting to localhost:" + KalahaMain.port);
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            addText("Done");
            connected = true;
        }
        catch (Exception ex)
        {
            addText("Unable to connect to server");
            return;
        }
    }
    /**
     * Starts the client thread.
     */
    public void start()
    {
        //Don't change this
        if (connected)
        {
            thr = new Thread(this);
            thr.start();
        }
    }
    
    /**
     * Creates the GUI.
     */
    private void initGUI()
    {
        //Client GUI stuff. You don't need to change this.
        JFrame frame = new JFrame("My AI Client");
        frame.setLocation(Global.getClientXpos(), 445);
        frame.setSize(new Dimension(420,250));
        frame.getContentPane().setLayout(new FlowLayout());
        
        text = new JTextArea();
        JScrollPane pane = new JScrollPane(text);
        pane.setPreferredSize(new Dimension(400, 210));
        
        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setVisible(true);
    }
    
    /**
     * Adds a text string to the GUI textarea.
     * 
     * @param txt The text to add
     */
    public void addText(String txt)
    {
        //Don't change this
        text.append(txt + "\n");
        text.setCaretPosition(text.getDocument().getLength());
    }
    
    /**
     * Thread for server communication. Checks when it is this
     * client's turn to make a move.
     */
    public void run()
    {
        String reply;
        running = true;
        
        try
        {
            while (running)
            {
                //Checks which player you are. No need to change this.
                if (player == -1)
                {
                    out.println(Commands.HELLO);
                    reply = in.readLine();

                    String tokens[] = reply.split(" ");
                    player = Integer.parseInt(tokens[1]);
                    
                    addText("I am player " + player);
                }
                
                //Check if game has ended. No need to change this.
                out.println(Commands.WINNER);
                reply = in.readLine();
                if(reply.equals("1") || reply.equals("2") )
                {
                    int w = Integer.parseInt(reply);
                    if (w == player)
                    {
                        addText("I won!");;
                    }
                    else
                    {
                        addText("I lost...");
                    }
                    running = false;
                }
                if(reply.equals("0"))
                {
                    addText("Even game!");
                    running = false;
                }

                //Check if it is my turn. If so, do a move
                out.println(Commands.NEXT_PLAYER);
                reply = in.readLine();
                if (!reply.equals(Errors.GAME_NOT_FULL) && running)
                {
                    int nextPlayer = Integer.parseInt(reply);

                    if(nextPlayer == player)
                    {
                        out.println(Commands.BOARD);
                        String currentBoardStr = in.readLine();
                        boolean validMove = false;
                        while (!validMove)
                        {
                            long startT = System.currentTimeMillis();
                            //This is the call to the function for making a move.
                            //You only need to change the contents in the getMove()
                            //function.
                            GameState currentBoard = new GameState(currentBoardStr);
                            int cMove = getMove(currentBoard);
                            
                            //Timer stuff
                            long tot = System.currentTimeMillis() - startT;
                            double e = (double)tot / (double)1000;
                            
                            out.println(Commands.MOVE + " " + cMove + " " + player);
                            reply = in.readLine();
                            if (!reply.startsWith("ERROR"))
                            {
                                validMove = true;
                                addText("Made move " + cMove + " in " + e + " secs");
                            }
                        }
                    }
                }
                
                //Wait
                Thread.sleep(100);
            }
	}
        catch (Exception ex)
        {
            running = false;
        }
        
        
        try
        {
            socket.close();
            addText("Disconnected from server");
        }
        catch (Exception ex)
        {
            addText("Error closing connection: " + ex.getMessage());
        }
    }
    
    /**
     * This is the method that makes a move each time it is your turn.
     * Here you need to change the call to the random method to your
     * Minimax search.
     * 
     * @param currentBoard The current board state
     * @return Move to make (1-6)
     */
    public int getMove(GameState currentBoard)
    {
        long maxTime = 5;
        int bestMove = iterativeDeepening(currentBoard, maxTime);
        return bestMove;
    }
    
    /**
     * Returns a random ambo number (1-6) used when making
     * a random move.
     * 
     * @return Random ambo number
     */
    public int getRandom()
    {
        return 1 + (int)(Math.random() * 6);
    }
    
   /** 
    * returns how is the current player at the moment in the GameTree
    * we used to calculate information in the tree
     * @param max if is max turn or min turn
    * @return current player, if is Max turn, that means our AI is playing
    * if it is min turn, that means is the turn of our oponent
   **/
   public int checkCurrentPlayer(boolean max){
       int currentPlayer;
                    if (max){
                        currentPlayer = player;
                    }else{
                        if(player == 1){
                            currentPlayer = 2;
                        } else{
                            currentPlayer = 1;
                        }
                    }
        return currentPlayer;
   }
   
   /**check if the next turn is still a max's move or is now a min's move.
    * 
    * @param currentBoard the current state of the board
    * @param currentPlayer the player that now has the next move
    * @return if the next is Max or not. 
    */ 
   public boolean isMaxTurn(GameState currentBoard, int currentPlayer)
   {
       return currentBoard.getNextPlayer() == currentPlayer;
   }
   
   /**calculates how good or bad is a movement.
    * 
    * @param board the current state
    * @return the difference between the score that the player has and the score that the oponent has.
    */
   public int utilityFunction(GameState board){
       int oponent;
       if(player == 1){
           oponent = 2;
       }else{
           oponent = 1;
       }
       int scorePlayer = board.getScore(player);
       int scoreOponent = board.getScore(oponent);
       return scorePlayer - scoreOponent; 
   }

    /** search trough the whole Game True what is the best move we can have using
     * iterative deeping search.
     * @param currentBoard the current state of the board game
     * @param maxTime the time that we want to spend doing the search
     * @return bestMove for our AI
     */
    public int iterativeDeepening(GameState currentBoard, long  maxTime)
    {

        int move = 1;
        int level = 1;
        
        //calculate the times
        long startTime = System.currentTimeMillis();
        maxTime = maxTime * 1000; //to milliscconds
        
        while(System.currentTimeMillis() - startTime < maxTime)
        {
                GameTree result = miniMax(currentBoard, true, 0, level, startTime, maxTime, Integer.MIN_VALUE, Integer.MAX_VALUE);
                
                if (!result.getEndTime()) //this means that we didn't end search the whole tree
                    //just that the time for the iterative deeping (5 seconds) hasn't finished. So we update the move
                        move = result.getMove();
                        
               
                if (result.getLimitTree())  // if the iterative deeping has finished and we have seen the whole tree, so
                      //we finish searching the iterative deeping and return the move. 
                        break;
                //if we didn't found anything a we have reach the top of the iterative deeping,
                //we add one and start again
                level++;
        }
        return move;  
    }
    
    /**
     * minimax decision algorithm to decided what is the best possible move that we can have.
     * Our best move for us is the move that gives higher score to max and lower score to min. 
     * (that is the value attached to it)
     * @param currentBoard board state of the game
     * @param isMaxTurn if it is min or max turn
     * @param currentLevel what level are we searching with deeping search
     * @param maxLevel max level to do the search with the minimax
     * @param startTime at what time we have started do the search
     * @param maxTime max time to do the search
     * @param alpha alpha value to do the alpha-beta prunning
     * @param beta beta value to do the alpha-beta prunning 
     * @return the gameTree information, what is the best move until now, what score are we going to get with that move,
     * if we don't have more time for searching and if we have reach the top of the tree (level 0, end of the tree)
     */
    public GameTree miniMax(GameState currentBoard, boolean isMaxTurn, int currentLevel, int maxLevel, long startTime, long maxTime, int alpha, int beta)
    {
        int move = getRandom();
        int evaluateMove;
        int score;
        
        if (isMaxTurn){
            score = Integer.MIN_VALUE;
        } else {
            score = Integer.MAX_VALUE;
        }
        
        //if we have reach the top of the iterative deeping before try any move (like the first call), we 
        //just return how is going with the search (check if our AI is winning with that move or no).
        if (currentLevel == maxLevel){
                evaluateMove = utilityFunction(currentBoard);
                return new GameTree(evaluateMove, 0, false, false);
        }
        //if we are in max, we inizialite the score to the lowest value, because
        //later we want to maximizie it. And in the other way if we are in min's turn.


        boolean LimitTree = false;
        for (int i = 1; i <= 6; i++)
        {
                if (currentBoard.moveIsPossible(i))
                {
                    //check if the time has passed, if is true we have to 
                    //return all empty, because we don't have found nothing
                    if (System.currentTimeMillis() - startTime >= maxTime) 
                         return new GameTree(0, 0, true, LimitTree);
                    
                    //clone the board to make a new child
                    GameState childBoard = currentBoard.clone();  
                    childBoard.makeMove(i);
                    // Check who is the next player and call minimax
                    int currentPlayer = checkCurrentPlayer(isMaxTurn);
                    //if the next player is not our current player, change the turn for the boolean variable
                    if(!isMaxTurn(childBoard, currentPlayer)){
                        isMaxTurn = !isMaxTurn;
                    }
                    GameTree tree = miniMax(childBoard, isMaxTurn, currentLevel + 1, maxLevel, startTime, maxTime, alpha, beta);

                    //if we have found the winner, that means we have reach the end of the Game Tree 
                    //so we change that to true
                    if (childBoard.getWinner() == player)  
                            LimitTree = true;
                    //if the time has passed, the outOfTime variable will be true, so we return the things we have until that moment
                    if (tree.getEndTime())
                            return tree;
                    //if is max turn, we want the move that give it the higher score
                    if (isMaxTurn)
                    {
                            if (tree.getScore() > score)
                            {
                                    score = tree.getScore();
                                    move = i;
                            }
                            alpha = Math.max(alpha, score);
                            if (alpha >= beta)
                                    break;
                    }
                    else
                    //if is the oponent turn, we one the move that give it the LOWEST score
                    {
                            if (tree.getScore() < score)
                            {
                                    score = tree.getScore();
                                    move = i;
                            }
                            beta = Math.min(beta, score);
                            if (alpha >= beta)
                                    break;
                    }
                }
        }
        
        
        //check if the time has passed again after search for a good candidate, if is true we have to 
        //return all empty, because we don't have found nothing
        if (System.currentTimeMillis() - startTime >= maxTime) 
                return new GameTree(0, 0, true, LimitTree);
        
        // we return the score we  have found until that moment.
        return new GameTree(score, move,  false, LimitTree);
    }
}

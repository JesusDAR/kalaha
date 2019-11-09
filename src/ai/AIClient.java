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
 * @author Johan Hagelbäck
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
     * @param isMaxTurn if is max turn or min turn
    * @return current player, if is Max turn, that means our AI is playing
    * if it is min turn, that means is the turn of our oponent
   **/
   public int checkCurrentPlayer(boolean isMaxTurn)
   {
            if (isMaxTurn)
            {
                return player;
            }
            else
            {
                if(player == 1)
                    return  2;
                else
                    return 1;
            }
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
   public int utilityFunction(GameState board)
   {
       int oponent;
       if(player == 1)
           oponent = 2;
       else
           oponent = 1;

       int scorePlayer = board.getScore(player);
       int scoreOponent = board.getScore(oponent);
       return scorePlayer - scoreOponent; 
   }
   
   /**
    * search for the best move for max, and for the worst move for min.
     * @param tree current auxiliar tree with the best score and best move till now after doing the recursive call
     * @param isMax which turn is max or min
     * @param score score auxiliar variable
     * @param move move auxiliar variable
     * @param candidate possible move candidate for best move for do the prunning and reduce the number of nodes that we have to check.
     * @param alpha best option till the moment for MAX (highest value)
     * @param beta best option till the moment for MIN (lowest value)
    **/
   
   public void alphaBetaPrunning(GameTree tree, boolean isMax, int score, int move, int candidate, int alpha, int beta)
   {
            if(isMax)  //if it's max's turn, we want the move that give it the higher score
            {
                if (score < tree.getScore())
                {
                    move = candidate;  //change the possible move 
                    score = tree.getScore(); //update the score 
                }
                alpha = Math.max (alpha, score);
            } 
            else 
            {
                 if (score > tree.getScore()) //if it's min's turn, we one the move that give it the LOWEST score
                 {
                    move = candidate;  //change the possible move 
                    score = tree.getScore(); //update the score 
                }
                beta = Math.min (alpha, score);
            }
            if (alpha >= beta)
                return; //if we have reach this, we only can found values lower than alpha, so we can prunning the rest and end this function
    }
   

    /** search trough the whole Game True what is the best move we can have using
     * iterative deeping search.
     * @param currentBoard the current state of the board game
     * @param maxTime the time that we want to spend doing the search
     * @return bestMove for our AI
     */
    public int iterativeDeepening(GameState currentBoard, long  maxTime)
    {
        int possibleMove = 1;
        int depth = 1;

        long startTime = System.currentTimeMillis(); //calculate the times
        maxTime = maxTime * 1000; //change from seconds to milliseconds
        
        while(System.currentTimeMillis() - startTime < maxTime)
        {
                GameTree auxiliarTree = miniMax(currentBoard, true, 0, depth, startTime, maxTime, Integer.MIN_VALUE, Integer.MAX_VALUE);
                if (!auxiliarTree.getEndTime())  //the time for the iterative deeping (5 seconds) hasn't finished so we didn't stop searching.   //We update the possible move
                        possibleMove = auxiliarTree.getMove();
                
                if (auxiliarTree.getLimitTree())  // if the iterative deeping has finished and we have seen the whole tree, that means //we finish searching and found a winner and we can return a best move.
                        break;

                depth++; //if we didn't found anything a we have reach the top of the iterative deeping, we add one and start again
        }
        return possibleMove;  
    }
    
    /**
     * minimax decision algorithm to decided what is the best possible move that we can have.
     * Our best move for us is the move that gives higher score to max and lower score to min. 
     * (that is the value attached to it)
     * @param currentBoard board state of the game
     * @param isMax if it is max or min turn
     * @param currentLevel what level are we searching with deeping search
     * @param maxLevel max level to do the search with the minimax
     * @param startTime at what time we have started do the search
     * @param maxTime max time to do the search
     * @param alpha alpha value to do the alpha-beta prunning
     * @param beta beta value to do the alpha-beta prunning 
     * @return the gameTree information, what is the best move until now, what score are we going to get with that move,
     * if we don't have more time for searching and if we have reach the top of the tree (level 0, end of the tree)
     */
    public GameTree miniMax(GameState currentBoard, boolean isMax, int currentLevel, int maxLevel, long startTime, long maxTime, int alpha, int beta)
    {
        int move = getRandom();
        int evaluateMove;
        boolean limitTree = false;
        boolean endTime = false;

        if (currentLevel == maxLevel) //if we have reach the limit of the iterative deeping before try any move (like the first call), we just return the heuristic value of the node.
        {
                evaluateMove = utilityFunction(currentBoard);
                return new GameTree(evaluateMove, 0, endTime, limitTree);
        }

        int score;
        if (isMax)  //if we are in max, we inizialite the score to the lowest value (worst case), because later we want to maximizie it. And in the other way if we are in min's turn.
            score = Integer.MIN_VALUE;
        else 
            score = Integer.MAX_VALUE;

        for (int i = 1; i <= 6; i++)  //for every possible move, create a new node.
        {
                if (currentBoard.moveIsPossible(i))
                {
                    if (System.currentTimeMillis() - startTime >= maxTime)  //check if the time has passed, if is true we have to  return all empty, because we don't have found nothing
                    {
                         endTime = true;
                         return new GameTree(0, 0, endTime, limitTree);
                    }
                    
                    GameState childBoard = currentBoard.clone();   //clone the board to make a new child
                    childBoard.makeMove(i);
                    
                   
                    int currentPlayer = checkCurrentPlayer(isMax);  // Check who is the next player in this new child and call minimax
                    
                    if(!isMaxTurn(childBoard, currentPlayer)) //if the next player is not our current player, change the turn for the boolean variable
                        isMax  = !isMax;
                   
                    GameTree tree = miniMax(childBoard, isMax, currentLevel + 1, maxLevel, startTime, maxTime, alpha, beta);  //recursive call

                    boolean haveWinner = childBoard.getWinner() == player;  //if we have found the winner, that means we have reach the end of the Game Tree  so we change that to true
                    if (haveWinner)  
                            limitTree = true;

                    if (tree.getEndTime()) //if we have reach the max time so we return the things we have until that moment
                            return tree;

                    alphaBetaPrunning(tree, isMax, score, move, i, alpha, beta);  //here you search for the best move if is max's turn, and for the  worst move if is min's turn, using the alphaBeta prunning to be more fast.  
                }
        }

        if (System.currentTimeMillis() - startTime >= maxTime) //check if the time has passed again after search for a good candidate, if is true we have to return all empty, because we have to end the search and just stay which we have found until that moment
        {
                endTime = true;
                return new GameTree(0, 0, endTime, limitTree);
        }
        return new GameTree(score, move,  false, limitTree); // we return the score and the move we  have found until that moment.
    }
 
 }

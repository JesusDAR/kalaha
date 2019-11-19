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
                        addText("I won!");
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
        long startTime = System.currentTimeMillis(); 
        int bestMove = iterativeDeepening(currentBoard.clone(), startTime);
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
    
    /** search trough the whole Game True what is the best move we can have using
     * iterative deeping search.
     * @param currentBoard the current state of the board game
     * @param maxTime the time that we want to spend doing the search
     * @return bestMove for our AI
     */
    private int iterativeDeepening(GameState currentBoard, long startTime)
    {

        int levels [] = new int [] {Global.startCurrentLevel, Global.startMaxLevel};
        int move = 1;
        while(System.currentTimeMillis() - startTime < Global.maxTime * 1000)
        {
                GameTree tree = miniMax(currentBoard, true, levels , Integer.MIN_VALUE, Integer.MAX_VALUE, startTime);
               
                if (!tree.getEndTime()) //Time is not over, we update the move.
                        move = tree.getMove();
                if (tree.getLimitTree()) // if the iterative deeping has finished and we have seen the whole tree. We finish searching the iterative deeping and return the move. 
                        break;
                levels[1]++; //Increase the max depth
        }
            return move;
    }
     
    /**
     * minimax decision algorithm to decided what is the best possible move that we can have.
     * Our best move for us is the move that gives higher score to max and lower score to min. 
     * (that is the value attached to it)
     * @param currentBoard board state of the game
     * @param isMaxTurn if it is min or max turn
     * @param levels The current level in the minmax tree (pos 0) and the  max depth of the minmax tree (pos 1)
     * @param alpha alpha value to do the alpha-beta prunning
     * @param beta beta value to do the alpha-beta prunning 
     * @param startTime at what time we have started do the search
     * @return the gameTree information, what is the best move until now, what score are we going to get with that move,
     * if we don't have more time for searching and if we have reach the top of the tree (level 0, end of the tree)
     */
    private GameTree miniMax(GameState currentBoard, boolean isMaxTurn, int [] levels ,int alpha, int beta, long startTime)
    {
            int score;
            int move = 1;
            boolean limitTree = false;
            
            if (levels[0] == levels[1]) //we reach the bottom
            {
                    int scoreDiference = utilityFunction(currentBoard);
                    return new GameTree(scoreDiference, 0,false, false);
            }
            if (isMaxTurn) //if we are in max, we inizialite the score to the lowest value.  And in the other way if we are in min's turn.
                 score = Integer.MIN_VALUE;
             else 
                 score = Integer.MAX_VALUE;

            for (int i = 1; i < 7; i++)
            {
                        if (currentBoard.moveIsPossible(i))
                        {
                                if (System.currentTimeMillis() - startTime >= Global.maxTime * 1000) //check if time is over
                                    return new GameTree(0,0, true, limitTree);

                                GameState childBoard = currentBoard.clone();  //Clone the board to make a new child
                                childBoard.makeMove(i);
                                int currentPlayer = checkCurrentPlayer(isMaxTurn);
                                boolean isNextTurnMaxTurn;
                                if (childBoard.getNextPlayer() == currentPlayer) //Check if in the next move the next player is different from the current one. if so change  isMaxTurn
                                    isNextTurnMaxTurn = isMaxTurn;
                                else
                                    isNextTurnMaxTurn = !isMaxTurn;
                                GameTree tree = miniMax(childBoard,isNextTurnMaxTurn, new int [] {levels[0] + 1, levels[1]}, alpha, beta,startTime);

                                if (System.currentTimeMillis() - startTime >= Global.maxTime * 1000) //check if time is over
                                     return new GameTree(0, 0, true, limitTree);

                                if (childBoard.getWinner() == player) //if we have found the winner, that means we have reach the end of the GameTree 
                                        limitTree = true;
                                if (tree.getEndTime()) //if the time has passed, the outOfTime variable will be true, so we return the things we have until that moment
                                        return tree;

                                if (isMaxTurn) //if is max turn, we want the move that give it the higher score
                                {
                                            if (tree.getScore() > score)
                                            {
                                                    score = tree.getScore();
                                                    move = i;
                                            }
                                            alpha = Math.max(alpha, score);
                                }
                                else //if is the oponent turn, we one the move that give it the lowest score
                                {
                                            if (tree.getScore() < score)
                                            {
                                                    score = tree.getScore();
                                                    move = i;
                                            }
                                            beta = Math.min(beta, score);
                                }
                                if (alpha >= beta)
                                        break;
                        }
            }

            if (System.currentTimeMillis() - startTime >= Global.maxTime * 1000) //check if time is over
                                return new GameTree(0, 0, true, limitTree);

            return new GameTree(score, move, false, limitTree); // we return the GameTree with  the score updated
    }
    
   /**calculates how good or bad is a movement.
    * 
    * @param currentBoard the current state
    * @return the difference between the score that the player has and the score that the oponent has.
    */
    private int utilityFunction(GameState currentBoard)
    {
        int oponent;
        if(player == 1)
            oponent = 2;
        else
            oponent = 1;
       int scorePlayer = currentBoard.getScore(player);
       int scoreOponent = currentBoard.getScore(oponent);
       return scorePlayer - scoreOponent;
    }
    
   /** 
    * returns how is the current player at the moment in the GameTree
    * we used to calculate information in the tree
     * @param isMaxTurn if is max turn or min turn
    * @return current player, if is Max turn, that means our AI is playing if it is min turn, that means is the turn of our oponent
   **/
   private int checkCurrentPlayer(boolean isMaxTurn)
   {
       int currentPlayer;
        if (isMaxTurn)
        {
            currentPlayer = player;
        }
        else
        {
            if(player == 1)
                currentPlayer = 2;
            else
                currentPlayer = 1;
        }
        return currentPlayer;
   }
}
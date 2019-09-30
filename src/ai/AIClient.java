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
        int AICliente_Score = currentBoard.getScore(player);
        int bestMove = getRandom();
        for(int i=1; i<= currentBoard.getNoValidMoves(player); i++)
        {
            GameState newState = currentBoard.clone(); //copy the original game state.
            newState.makeMove(i);
            double searchTimeLimit = 5; //put the max time to spend looking at each move
            int score = IterativeDeepeningSearch(newState, searchTimeLimit);
            if(newState.getWinner()==player) //if the deepening search find a good movement/good score for our AI, make the move
                return i;
            if(score > AICliente_Score)
            {
              AICliente_Score = score;
              bestMove = i;
            }
        }
        return bestMove;
    }
    //do iterative deepening searching for the score, calling the minimax to find it.
    private int IterativeDeepeningSearch(GameState state, double maxTime )
    {
		double startTime = System.currentTimeMillis()/1000;
		int depth = 0; //before was depth = 4 for the C grade, and we decrease;
		int score = 0;
		while (startTime<=maxTime)
		{
            int result = Minimax_AlphaBeta(state, depth, Integer.MAX_VALUE, Integer.MIN_VALUE, startTime, maxTime);
		    if(state.getWinner()==player) //if the minimax find a result where our AI win, stop searching
		        return result;
            depth++; //if not is a good result for our ID, increase the depth
		}
		return score;
    }

	    //perform minimax search with alpha-beta pruning. 
    private int Minimax_AlphaBeta(GameState currentState, int depth, int alpha, int beta, double startTime, double endTime)
    {
		double currentTime = System.currentTimeMillis()/1000;
		double leftTime = (currentTime - startTime);
		int savedScore = currentState.getScore(player);
		if(leftTime<=endTime)
		{
            //If is a terminal node or the game ends, return score
            if ((depth==0)|| currentState.gameEnded() )
                return savedScore;
            else
            {
                //if there are still movements for our AI client
                if (currentState.getNoValidMoves(player)!=0)
                {
                    for(int i=1; i<= currentState.getNoValidMoves(player); i++)
                    {
                        GameState childState = currentState.clone();
                        if(childState.makeMove(i))
                            childState.makeMove(i);
                        alpha = Math.max(alpha, Minimax_AlphaBeta(childState, depth-1, alpha, beta, startTime, endTime));
                        if (beta<=alpha)
                            break;
                        return alpha;
                    }
                //if there aren't any movements for or AI, we would find the min.
                }
                else
                {
                    int oponent = currentState.getNextPlayer();
                    for(int i=1; i<= currentState.getNoValidMoves(oponent); i++)
                    {
                        GameState childState = currentState.clone();
                        childState.makeMove(i);
                        beta = Math.min(beta, Minimax_AlphaBeta(childState, depth-1, alpha, beta, startTime, endTime ));
                        if(beta<=alpha)
                            break;
                        return beta;
                    }
                }
            }
		}
		else
        {
		    return savedScore;
		}
		return savedScore; 
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
}

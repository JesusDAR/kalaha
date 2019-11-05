package ai;

import ai.Global;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.text.html.MinimalHTMLWriter;

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
    
    private PrintWriter openingBook;
    private int winner = -1;
    private String fileString = "";
    private ArrayList<ArrayList<Integer>> array;
    	
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
        
        try {
        	fileString = new String(Files.readAllBytes(Paths.get("openingBook.data")), StandardCharsets.UTF_8);
            
        	/*openingBook = new PrintWriter("openingBook.data", "UTF-8");
            openingBook.append(fileString);
            openingBook.close();*/
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        array = new ArrayList<ArrayList<Integer>>();
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
                        winner = player;
                    }
                    else
                    {
                        addText("I lost...");
                        winner = player==1?2:1;
                    }
                    running = false;
                }
                if(reply.equals("0"))
                {
                    addText("Even game!");
                    running = false;
                    winner = 0;
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
        
        // FOR CREATING THE OPENING BOOK
    	/*try {
			openingBook = new PrintWriter("openingBook.data", "UTF-8");
			openingBook.append(fileString+(winner==0?"draw":(winner==player?"ai":"opponent"))+"\n#");
	        openingBook.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}*/
        
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
        //int myMove = nextMoveMinimaxDFS(currentBoard, true, 0, 4);
        int myMove = nextMoveIterativeDeepening(currentBoard, 5);
        return myMove;
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
    
   
    private int evaluation(GameState board, boolean max)
    {
    	int oponent = (player == 1? 2 : 1);
    	return board.getScore(player) - board.getScore(oponent);
    }
    
    private class MinMaxReturn
    {
    	private int score_move = 1;
    	private boolean outOfTree = false;
    	private boolean endOfTree = false;
    	
    	public MinMaxReturn(int score_move, boolean outOfTree, boolean endOfTree)
    	{
    	   this.score_move = score_move;
    	   this.outOfTree = outOfTree;
    	   this.endOfTree = endOfTree;
    	}
    }
    
   
   
    
    private int nextMoveIterativeDeepening(GameState currentBoard, int time_seconds)
    {

    	int move = 1;
    	int level = 1;
    	
    	long startTime = System.currentTimeMillis();
    	long time_in_millis = (long)(time_seconds * 1000);
    	while(System.currentTimeMillis() - startTime < time_in_millis)
    	{
    		MinMaxReturn result = nextMoveMinimaxDFS(currentBoard, true, 0, level, startTime, time_in_millis, Integer.MIN_VALUE, Integer.MAX_VALUE);
    		// The time have run out, return and use the move from the one level above the tree. 
    		if (!result.outOfTree)
    			move = result.score_move;
    		// The tree can not grow anymore.
    		if (result.endOfTree)
    			break;
    		level++;
    	}
    	//saveMove(move, currentBoard);
    	return move;
    }
    
    /**
     * Use a minmax tree with Depth-First search
     * 
     * @param currentBoard The current game state
     * @param max If the player is the AI or the opponent
     * @param level The current level in the minmax tree
     * @param toLevel Max depth of the minmax tree
     * @return The move the AI should use
     */
    private MinMaxReturn nextMoveMinimaxDFS(GameState currentBoard, boolean max, int level, int toLevel, long startTime,
    		long time_in_millis, int alpha_score, int beta_score)
    {
    	if (level == toLevel)
    		return new MinMaxReturn(evaluation(currentBoard, max), false, false);
    	
    	int score = (max? Integer.MIN_VALUE : Integer.MAX_VALUE);
    	int move = getRandom();
    	
    	boolean endOfTree = false;
    	for (int ambo = 1; ambo <= 6; ambo++)
        {
    		if (currentBoard.moveIsPossible(ambo))
    		{
    			if (System.currentTimeMillis() - startTime >= time_in_millis) {
    	    		return new MinMaxReturn(0, true, endOfTree);
    	    	}
    			
    			// Check if the player gets an extra move and call minmax method.
    			GameState board = currentBoard.clone();
    			board.makeMove(ambo);
    			int currentPlayer = (max? player : (player == 1? 2 : 1));
    			MinMaxReturn result = nextMoveMinimaxDFS(board, (board.getNextPlayer() == currentPlayer? max : !max), 
    					level + 1, toLevel, startTime, time_in_millis, alpha_score, beta_score);
    			// Return from branch with "endOfTree" set to true. If there are no better moves go out of the tree and stop the while loop.
    			if (board.getWinner() == player)
    				endOfTree = true;
    			if (result.outOfTree)
    				return result;
    			
    			if (max)
    			{
    				if (result.score_move > score)
    				{
    					score = result.score_move;
    					move = ambo;
    				}
    				alpha_score = Math.max(alpha_score, score);
    				if (alpha_score >= beta_score)
    					break;
    			}
    			else if (!max)
    			{
    				if (result.score_move < score)
    				{
    					score = result.score_move;
    					move = ambo;
    				}
    				beta_score = Math.min(beta_score, score);
    				if (alpha_score >= beta_score)
    					break;
    			}
    		}
    	}
    	
    	if (System.currentTimeMillis() - startTime >= time_in_millis) {
    		return new MinMaxReturn(0, true, endOfTree);
    	}
    	
    	if (level == 0){
    		return new MinMaxReturn(move, false, endOfTree);
    	}
    	return new MinMaxReturn(score, false, endOfTree);
    }
}
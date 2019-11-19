package ai;

/**
 * Contains some global constants to be used by the
 * minimax game tree.
 * 
 * @author Johan Hagelbäck
 */
public class Global 
{
    private static boolean firstTaken = false;
    public static long maxTime  = 5;
    public static int startCurrentLevel  = 0;
    public static int startMaxLevel  = 1;
    
    /**
     * Used by the GUI to find where to place a new
     * client window.
     * 
     * @return X position for the new client window. 
     */
    public static int getClientXpos()
    {
        if (!firstTaken)
        {
            firstTaken = true;
            return 0;
        }
        return 425;
    }
}

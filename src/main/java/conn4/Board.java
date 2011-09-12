/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package conn4;

import com.mongodb.BasicDBObject;

/**
 * Created: Dec 6, 2010  6:59:12 PM
 *
 * @author Joseph Shraibman
 * @version $Revision: 1.1 $  $Date:  $ $Author: jks $
 */
public interface Board {

    boolean colIsFul(int col);

    /** Start index is 1, not 0 */
    Checker getCheckerAt(int col, int row);

    /**
     * Get turns until this player can force a win
     */
    UntilWin getTurnsUntilWin();

    UntilWin getTurnsUntilWin(int colidx);

    Checker getWhoseTurn();

    boolean isWinning();

    Board move(int col);
    
    public java.util.List<Board> getAllMoves();
    
    public BasicDBObject toBSONObject();

}

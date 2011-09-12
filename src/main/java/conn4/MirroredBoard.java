/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package conn4;

import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.List;

/** <p>To save on processing, don't generate boards that are mirror images of each other. Just use this wrapper</p>
 *
 * Created: Nov 30, 2010  8:40:27 PM
 *
 * @author Joseph Shraibman
 */
public class MirroredBoard implements Board {
    BoardImp board;

    private MirroredBoard(BoardImp board) {
        this.board = board;
    }
    static Board getMirroredBoard(Board board) {
        if (board == null)
            return null;
        if (board instanceof MirroredBoard){
            return ((MirroredBoard)board).board;
        }
        return new MirroredBoard( (BoardImp) board);
    }
    

    @Override
    public boolean colIsFul(int col) {
        return board.colIsFul(board.getOppositeCol(col));
    }

    @Override
    public Checker getCheckerAt(int col, int row) {
        return board.getCheckerAt(board.getOppositeCol(col), row);
    }

    public List<Board> getAllMoves() {
        List<Board> orig = board.getAllMoves();
        List<Board> ans = new ArrayList<Board>(orig.size());
        for(int i = orig.size() - 1; i >= 0 ; i--){
            ans.add(getMirroredBoard(orig.get(i)  ));
        }
        return ans;
    }

    public BasicDBObject toBSONObject() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

  

    @Override
    public UntilWin getTurnsUntilWin() {
        return board.getTurnsUntilWin();
    }

    @Override
    public UntilWin getTurnsUntilWin(int colidx) {
        return board.getTurnsUntilWin(board.getOppositeCol(colidx));
    }

    @Override
    public Checker getWhoseTurn() {
        return board.getWhoseTurn();
    }

    @Override
    public boolean isWinning() {
        return board.isWinning();
    }

    @Override
    public Board move(int col) {
        Board ans = board.move(board.getOppositeCol(col));
        if (ans instanceof  MirroredBoard)
            return ((MirroredBoard)ans).board;
        return getMirroredBoard((BoardImp)ans);
    }

    @Override
    public String toString() {
        return board.toString(true);
    }

}

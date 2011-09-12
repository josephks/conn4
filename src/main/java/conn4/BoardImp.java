
package conn4;

import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.bson.BSONObject;

/**
 * Created: Nov 30, 2010  7:42:53 PM
 *
 * @author Joseph Shraibman
 * @version $Revision: 1.1 $  $Date:  $ $Author: jks $
 */
public class BoardImp implements Board {

    final static String BOARD_FIELD_NAME = "board";
    final static String BEST_MOVES_FIELD_NAME = "bestmoves";
    final static String BEST_MOVE_FIELD_NAME = "bestmove";//column #. Best move if everyone plays perfectly
    /** {@literal win/lose <numturns>}. examples: <ul><li>{"lose" : 1} <li>{"win":3} <li>"tie"</ol>  tie needs no number */
    final static String BEST_RESULT_FIELD_NAME = "bestresult";
    final static String BEST_RESULTS_FIELD_NAME = "bestresults";//array of what the best result for each move is
    final static String WINS_FIELD_NAME = "wins";     //If this is a winning board value is "red" or "black"
    final static String NUM_CHECKERS_FIELD_NAME = "numcheckers";
//       private final static String _FIELD_NAME = "";
//       private final static String _FIELD_NAME = "";
    private int board_width;
    private int half_len;
    private int board_height;
    private Checker turn = Checker.RED;

    //outer list is columns, going from left to right
    //inner lists are positions in columns, starting at the bottom
    private List<List<Checker>> board;
    //Number of Checkers on the board
    Number num_checkers = Integer.valueOf(0);
    //list of best moves for this board
    private List<UntilWin> best_moves = null;
    
    //delete
    private java.util.Set<BoardImp> move_cache = new HashSet<BoardImp>();

    public static BoardImp getBoard(int height, BSONObject raw_obj){
        List blist = (List) raw_obj.get(BOARD_FIELD_NAME);
        BoardImp ans = new BoardImp(blist.size(), height);
        ans.num_checkers = (Number) raw_obj.get(NUM_CHECKERS_FIELD_NAME);
        if (ans.num_checkers == null){
            //todo: insert code here to calculate
        }else{
            if (ans.num_checkers.intValue() % 2 == 0)
                ans.turn = Checker.RED;
            else
                ans.turn = Checker.BLACK;
        }
        for(int i = 0; i < ans.board_width ; i++){
            ans.board.set(i, fromStringList( (List) blist.get(i)));
        }
        ans.best_moves = (List<UntilWin>) raw_obj.get(BEST_MOVES_FIELD_NAME);
        if (ans.best_moves != null){
            for(int i = 0; i < ans.board_width; i++)
                ans.addBestKnownMove(i+1, ans.best_moves.get(i));
        }
        return ans;
    }
    private static List<Checker> fromStringList(List<String> list){
        List<Checker> ans = new ArrayList<Checker>(list.size());
        for(String s : list)
            ans.add(Checker.fromString(s));
        return ans;
    }
    public BasicDBObject toBSONObject() {
        BasicDBObject ans = new BasicDBObject();
        List<List<String>> board4serial = new ArrayList<List<String>>(board_width);
        for (List<Checker> coll : board) {
            List<String> forIns = new ArrayList<String>(coll.size());
            for (Checker c : coll)
                forIns.add(c.toString());
            board4serial.add(forIns);
        }
        ans.put(BOARD_FIELD_NAME, board4serial); //will probably have to convert board to BSONList
        if (this.isWinning())
            ans.put(WINS_FIELD_NAME, getWhoseTurn().opposite().toString());
        else if (isFull())
            ans.put(WINS_FIELD_NAME, "tie");
        ans.put("_id", toString());
        ans.put(NUM_CHECKERS_FIELD_NAME, getNumCheckers());
        if (false && best_moves != null) {
            List<List> ll = new ArrayList<List>(best_moves.size());
            for (int i = 0; i < board_width; i++) {
                UntilWin uw = best_moves.get(i);
                if (uw != null)
                    ll.set(i, uw.toList());
            }
            ans.put(BEST_MOVES_FIELD_NAME, ll);
            ans.put(BEST_MOVE_FIELD_NAME, getBestKnownMove().toList());
        }
        return ans;
    }
    private UntilWin getBestKnownMove(){
        if (best_moves == null || best_known_move_index < 0)
            return null;
        return best_moves.get(best_known_move_index);
    }
    private int best_known_move_index = -1;

    private void addBestKnownMove(int move, UntilWin untilwin){
        //finish
    }

    public BoardImp(int length, int height) {
        this.board_width = length;
        half_len = length / 2;
        this.board_height = height;
        board = new ArrayList<List<Checker>>(length);
        for(int i = 0; i < length ; i++)
            board.add(new ArrayList<Checker>(height));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        final BoardImp other = (BoardImp) obj;
        if (this.turn != other.turn)
            return false;
        if (this.board != other.board && !this.board.equals(other.board))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return board.hashCode();
    }

    public Checker getWhoseTurn() {
        return turn;
    }
    private int getNumCheckers(){
        int ans = 0;
        for(int colNum = 1; colNum <= board_width ; colNum++){
            List<Checker> collist = board.get(colNum-1);
            ans += collist.size();
        }
        return ans;
    }

    @Override
    public String toString() {
        return toString(false);
    }
    public String toString(boolean backward) {
        StringBuilder sb = new StringBuilder(board_height  * (board_width+1));
        for(int row = board_height ; row >= 1 ; row--){
            if (backward){
                for(int col = board_width; col >= 1  ; col--){
                    sb.append(getCheckerChar(col, row));
                }
            }else{
                for(int col = 1; col <= board_width ; col++){
                    sb.append(getCheckerChar(col, row));
                }
            }
            sb.append('\n');
        }//for row

        return sb.toString();
    }
    private char getCheckerChar(int col, int row){
        Checker bp = getCheckerAt(col, row);
        if (bp == null || bp == Checker.EMPTY)
            return ' ';
        else if (bp == Checker.RED)
            return 'R';
        else
            return 'B';
    }

    //start index is 1
    public Checker getCheckerAt(int col, int row){
        return getCheckerAt0(col, row);
    }
    private Checker getCheckerAt0(int col, int row){
        //Utils.setThreadName("Board.gca", "Board.getCheckerAt(col"+ col+", row"+ row+")");
        //System.err.println("Board.getCheckerAt(col "+ col+", row "+ row+") starting");
        try{
            if (col <= 0 || col > board_width || row <= 0 || row > board_height)
                throw new IllegalArgumentException("getCheckerAt("+col+","+row+") on a "+board_width +" X "+board_height+" board");
            List<Checker> collist = board.get(col-1);
            int rowidx = row - 1;
            if (collist.size() <= rowidx)
                return Checker.EMPTY;
            //System.err.println("Board.getCheckerAt(col "+ col+", row "+ row+") ");
            return collist.get(rowidx);
        }finally{
            Utils.resetThreadName("Board.gca");
        }

    }
    //col is base 1
    protected static int getOppositeCol(int col, int len){
        return len - (col - 1);
    }
    protected  int getOppositeCol(int col){
        return getOppositeCol(col, this.board_width);
    }
    /** Get boards representing all possible moves */
    public List<Board> getAllMoves(){
        List<Board> ans = new ArrayList<Board>(board_width);
        for(int i = 1 ; i <= board_width ; i++){
             Board mi = move(i);
             if (mi != null)
                 ans.add(mi);
        }
        return ans;
    }
    /** For boards that were generated from one of their children, the move that
     * would produce that child */
    public int getChildIsMove(){
        return childIsMove;
    }
    private int childIsMove = -1; //for generated parent boards
    public List<BoardImp> getAllParents(){
        List<BoardImp> ans = new ArrayList<BoardImp>(board_width);
        Checker whoWent = getWhoseTurn().opposite();
        for(int colNum = 1; colNum <= board_width ; colNum++){
            List<Checker> collist = board.get(colNum-1);
            if ( (!collist.isEmpty()) && collist.get(collist.size() - 1) == whoWent){
                BoardImp parent = new BoardImp(board_width, board_height);
                parent.turn = whoWent;
                parent.board = new ArrayList<List<Checker>>(board);
                List<Checker> newcol = new ArrayList<Checker>(this.board.get(colNum - 1));
                newcol.remove(newcol.size() - 1);
                parent.board.set(colNum-1,  Collections.unmodifiableList(newcol));
                parent.board = Collections.unmodifiableList(parent.board);
                parent.childIsMove = colNum;
                ans.add(parent);
            }
        }
        return ans;
    }
    
    public Board move(int col){
        if (col < 1)
            throw new IllegalArgumentException("move ("+col+") column numbers start at 1");
        if (col > board_width)
            throw new IllegalArgumentException("move ("+col+") cannot be greater than length ("+board_width+")");
        if(false && isSemetrical() && col <= half_len){
            System.err.println("move ("+col+") returning mirror ");
            return MirroredBoard.getMirroredBoard( (BoardImp)move(getOppositeCol(col)) );
        }
        List<Checker> collist = board.get(col-1);
        if (collist.size() >= board_height)
            return null;
        BoardImp ans = new BoardImp(board_width, board_height);
        ans.turn = turn.opposite();
        ans.board = new ArrayList<List<Checker>>(board);
        List<Checker> newcol = new ArrayList<Checker>(collist);
        newcol.add(turn);
        ans.board.set(col-1,  Collections.unmodifiableList(newcol));
        ans.board = Collections.unmodifiableList(ans.board);
        if (move_cache.contains(ans))
            return ans;
        ans.move_cache = this.move_cache;
        move_cache.add(ans);
        return ans;
    }
    public boolean colIsFul(int col){
        List<Checker> collist = board.get(col-1);
        return collist.size() == board_height;
    }
    public boolean isFull(){
        for(List<Checker> col : board){
            if (col.size() != board_height )
                return false;
        }
        return true;
    }
    public boolean isTie(){
        return isFull() && !isWinning() ;
    }
    private Boolean winning = null;
    public boolean isWinning(){
        if(winning != null)
            return winning;
        Checker target = turn.opposite();
        for (List<Checker> col : board){
            int inrow = 0;
            for (int i = 0, len = col.size(); i < len 
                     //&&( (len - i) >= (4 - inrow) )
                     ; i++) {
                Checker coli = col.get(i);
                if (coli == target){
                    if (++inrow >= 4)
                        return (winning = Boolean.TRUE).booleanValue();
                }else if (coli == Checker.EMPTY)
                    break;
                else
                    inrow = 0;
            }            
        }
        //now check across
        for(int rowidx = 1 ; rowidx <= board_height ; rowidx++){
            int inrow = 0;
            for (int colidx = 1; colidx <= board_width
                     &&( (board_width - colidx + 1) >= (4 - inrow) )
                     ; colidx++) {
                Checker checker = getCheckerAt0(colidx, rowidx);
                if (checker == target){
                    if (++inrow >= 4)
                        return (winning = Boolean.TRUE).booleanValue();
                } else
                    inrow = 0;
            }//for colidx
            
        }
        //now check diag up to right
        for (int startcol = 1, lastcol = board_width - 4 + 1 ; startcol <= lastcol
                 ; startcol++) {
            int inrow = 0;
            for(int x = startcol , y = 1; x <= board_width && y <= board_height
                    //TODO: come up with equivalent here &&( (length - colidx + 1) >= (4 - inrow) )
                    ;){
                Checker checker = getCheckerAt0(x, y);
                if (checker == target){
                    if (++inrow >= 4)
                        return (winning = Boolean.TRUE).booleanValue();
                } else{
                    inrow = 0;
                }
                //System.err.println("xy: "+x+","+y+" checker: "+checker+" target: "+target+" inrow: "+inrow);
                x++;
                y++;
            }
        }
        //now check diag up to left
        for (int startcol = 4 ; startcol <= board_width
                 ; startcol++) {
            int inrow = 0;
            for(int x = startcol , y = 1; x > 0 && y <= board_height
                    //TODO: come up with equivalent here &&( (length - colidx + 1) >= (4 - inrow) )
                    ;){
                Checker checker = getCheckerAt0(x, y);
                if (checker == target){
                    if (++inrow >= 4)
                        return (winning = Boolean.TRUE).booleanValue();
                } else
                    inrow = 0;
                    
                x--;
                y++;
            }
             
        }
        return (winning = Boolean.FALSE).booleanValue();
    }
    private Boolean semetrical;
    private boolean isSemetrical(){
        if (semetrical != null)
            return semetrical.booleanValue();
        for(int i = 0; i < half_len ; i++ ){
            if (! board.get(i).equals( board.get(board_width -1 - i)) ){
                //System.err.println("isSemetrical() board.get("+i+") != board.get("+(length -1 - i)+")");
                return (semetrical = Boolean.FALSE).booleanValue();
            }
        }
        return (semetrical = Boolean.TRUE).booleanValue();
    }
    UntilWin gTUW_cache = null;
    /** Get turns until this player can force a win */
    public UntilWin getTurnsUntilWin(){
        if (gTUW_cache == null){
           throw new UnsupportedOperationException("Not supported yet.");
        }
        return gTUW_cache;
    }
    UntilWin get(int col, List<UntilWin> cache){
        if (cache.size() < col)
            return null;
        return cache.get(col - 1);
    }
    private ArrayList<UntilWin> gTUW_c_cache = null;
    public UntilWin getTurnsUntilWin(int colidx){
          throw new UnsupportedOperationException("Not supported yet.");
    }
     private UntilWin getTurnsUntilWin0(int colidx){
        Board thatmove = move(colidx);
        if (thatmove == null)
            return null; //illegal move
        if (thatmove.isWinning())
            return UntilWin.get(getWhoseTurn(), 0); //WIN!
        //iterative step
        //if all my children have my oponnent winning, return that I lose in 1
        UntilWin best = null;
        
        for(int col = 1 ; col <= board_width ; col++){
            Board board_i = move(col);
            if (board_i == null)
                continue;
                UntilWin uw_i = board_i.getTurnsUntilWin();
                if(isBetterThan(best, uw_i)){

                    best = uw_i;
                }
            }//for
        if (best == null) //the board is full
           return UntilWin.getTie();
        if (best.isTie())
            return best;

        if (best.whowins == getWhoseTurn())
            return best.getPlusOne();
        return best;
        
    }
private boolean isBetterThan(UntilWin best, UntilWin uw_i){
    if (uw_i == null)
        return false;
                if (best == null ||
                        //If I was losing before but this gets me to win chose it
                        (uw_i.whowins == getWhoseTurn() && (best.isTie() || best.whowins != getWhoseTurn())  )||
                        //If I was winning before but this gets me to win faster choose it
                        (uw_i.whowins == getWhoseTurn() && best.num_until_forced_win > uw_i.num_until_forced_win) ||
                        //If I was losing before
                        (  best.whowins != getWhoseTurn() &&
                            //this forces a tie  -OR- this make me lose slower
                           (uw_i.isTie() || best.num_until_forced_win < uw_i.num_until_forced_win )
                        ) ){
                    return true;
                }
                return false;
}
}

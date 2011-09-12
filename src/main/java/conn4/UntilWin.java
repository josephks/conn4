/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package conn4;

import org.bson.types.BasicBSONList;

/**
 * Created: Jan 16, 2011  10:37:45 PM
 *
 * @author Joseph Shraibman
 * @version $Revision: 1.1 $  $Date:  $ $Author: jks $
 */
public class UntilWin {

    static UntilWin TIE = get(null, -1);

    Checker whowins;//empty = draw
        int num_until_forced_win = -1; //zero means win this turn

        //for draw means number of moves until I can force a draw
    public int getNum_until_forced_win() {
        return num_until_forced_win;
    }

    public Checker getWhowins() {
        return whowins;
    }
public BasicBSONList toList(){
    BasicBSONList ans = new BasicBSONList();
    switch(whowins){
        case RED:
           ans.add("red"); break;
            case BLACK:
                ans.add("black"); break;
                case EMPTY:
                    ans.add("tie");
    }
    ans.add(getNum_until_forced_win());
    return ans;
}
        

        static UntilWin get(Checker whowins, int num){
            UntilWin ans = new UntilWin();
            ans.whowins  = whowins;
            ans.num_until_forced_win = num;
            return ans;
        }
        static UntilWin getTie(){
            return TIE;
        }
        public boolean isTie(){
            return num_until_forced_win < 0;
        }
        private UntilWin gOP0_cache = null;
        UntilWin getOppositePlusOne(){
            if (gOP0_cache == null){
            gOP0_cache = new UntilWin();
            gOP0_cache.whowins = whowins;
            gOP0_cache.num_until_forced_win = num_until_forced_win +1;
            }
            return gOP0_cache;
        }
        private UntilWin gP0_cache = null;
        UntilWin getPlusOne(){
            if (gP0_cache == null){
            gP0_cache = new UntilWin();
            gP0_cache.whowins = whowins;
            gP0_cache.num_until_forced_win = num_until_forced_win +1;
            }
            return gP0_cache;
        }
}

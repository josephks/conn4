
package conn4;

/**
 *
 * @author jks
 */
public enum Checker {
    RED, BLACK, EMPTY;

    public Checker opposite() {
        switch(this){
            case RED: return BLACK;
            case BLACK: return RED;
        }
        return this;
    }
    public static Checker fromString(String v){
        if (v == null)
            return null;
        for(Checker c : Checker.values())
            if (v.equals(c.toString()))
                return c;
        throw new IllegalArgumentException(v);
    }
}

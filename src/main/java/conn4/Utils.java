/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package conn4;

import java.util.ArrayList;

/**
 * Created: Dec 6, 2010  11:38:19 AM
 *
 * @author Joseph Shraibman
 * @version $Revision: 1.1 $  $Date:  $ $Author: jks $
 */
public class Utils {
 //---------------

    /** Remove any elements in this list after the first <size> */
    public final static void trimList(java.util.List in, final int size){
        if (in == null) return;
        int lsize = in.size();
        for(int i = lsize -1; i >= size ; i--)
            in.remove(i);
    }
    private final static boolean debug_tn = false;
    //have toString()s in these classes bec. Tomcat will write them to the error log if
    //it finds them stuck in a ThreadLocal
    //SEVERE: The web application [] created a ThreadLocal with key of type [java.lang.ThreadLocal] (value [java.lang.ThreadLocal@11b8a4c]) and a value of type [com.xtenit.util.Misc.Tn_state] (value [com.xtenit.util.Misc$Tn_state@114bc90]) but failed to remove it when the web application was stopped. This is very likely to create a memory leak.
    private static class Tn_state {
        String orig_name ;
        String current_domain;
        ArrayList al = new ArrayList();
        @Override
        public String toString(){
            return al.toString();
        }
    }
    private static class Kv {
        String k,v;
        Kv(String a, String b){ k = a; v = b; }
        @Override
        public String toString(){
            return "{"+k+","+v+"}";
        }
    }
    /* see comment in resetThreadName() about garbage collection problem with ThreadLocals */
    private final static ThreadLocal<Tn_state> tn_states = new ThreadLocal<Tn_state>();
    private static boolean cant_set_name = false;
    /** Append the passed in String to the original name of the current thread
        (before any previous calls to setThreadName()) so repeated calls don't make
        the name get longer and longer.
        <P> Usage:
         <code>
            public void method(){
                final String tn_domain = "method()";
                Misc.setThreadName(tn_domain,"doing a()");
                a();
                Misc.setThreadName(tn_domain,"doing b()");
                b():
                Misc.resetThreadName(tn_domain); //good idea to put this in a finally block
            }
         </code>
       <P> The domain keeps any calls from different layers of methods from
        overriding each other. If for example <code>a()</code> used <code>setThreadName()</code>
        the thread name would be set to "original : doing a() : whatever a set".  If
        <code>a()</code> didn't call
        <code>resetThreadName()</code> the next time we call <code>setThreadName()</code>
         with our domain <code>tn_domain</code> anything that <code>a()</code>
          left over will be discarded
    */
    public final static boolean setThreadName(String d, String v){
        if (cant_set_name)
            return false;

        Thread curr_thread = Thread.currentThread();

        Tn_state state = (Tn_state) tn_states.get();

        if (state == null){
            state = new Tn_state();
            state.orig_name = curr_thread.getName();
            tn_states.set(state);
        }

        if (! d.equals(state.current_domain)){
            for(int i = 0, len = state.al.size() ; i < len ; i++){
                if (((Kv)state.al.get(i)).k.equals(d)){
                    trimList(state.al, i);
                    break;
                }
            }

            state.al.add(new Kv(d,v));
            state.current_domain = d; //redundant
        }else try{
            ((Kv)state.al.get(state.al.size() - 1)).v = v;
        }catch(ArrayIndexOutOfBoundsException e){
            //String mo = "Misc.setThreadName("+Objects.getQuotString(d)+", "+Objects.getQuotString(v)+")";
            throw e;
        }

        return calcThreadName(state);
    }
    /** For XtenitRunnables to be able to return the added part of the thread name */
    public final static String getXRName(){
        if (cant_set_name)
            return "";
        Tn_state state = (Tn_state) tn_states.get();
        if (state == null || state.al.isEmpty())
            return "";
        String curr_name = Thread.currentThread().getName();
        return curr_name.substring(state.orig_name.length());
    }
    private final static boolean calcThreadName(Tn_state state){
        StringBuilder sb = new StringBuilder(128);
        sb.append(state.orig_name);
        state.current_domain = null;
        for(int i = 0, len = state.al.size() ; i < len ; i++){
            Kv kv = (Kv)state.al.get(i);
            sb.append(": ").append(kv.v);
            state.current_domain = kv.k; //ends up being set to the last one
        }
        try{
            Thread curr_thread = Thread.currentThread();
            curr_thread.setName(sb.toString());
        }catch(SecurityException e){
            e.printStackTrace();
            cant_set_name = true;
            return false;
        }
        return true;
    }
    /** Set the current thread name to what is was before any calls to <code>setThreadName()</code>
     for this domain */
    public final static void resetThreadName(String d){
        if (cant_set_name)
            return;

        Tn_state state = (Tn_state) tn_states.get();
        if (state == null)
            return;

        for(int i = 0, len = state.al.size() ; i < len ; i++){
            if (((Kv)state.al.get(i)).k.equals(d)){
                trimList(state.al, i);
                break;
            }
        }
        calcThreadName(state);
        //Remove the tn_state from the ThreadLocal if we don't need it anymore. I'm
        //doing this in case the unused tn_state is keeping a refrence to its classloader
        //which is preventing it from being garbage collected inside Tomcat which is
        //leading to "OutOfMemoryError: PermGen space"
        if (state.al.isEmpty())
            tn_states.remove();
    }
}

package net.jmb19905.util;

import java.util.ArrayList;
import java.util.List;

public class ShutdownManager {

    private static final List<CleanUpAction> cleanUpActions = new ArrayList<>();
    private static final List<Thread> threads = new ArrayList<>();

    public static void addCleanUp(CleanUpAction cleanUpAction){
        cleanUpActions.add(cleanUpAction);
    }

    public static void addThread(Thread thread){
        threads.add(thread);
    }

    public static void shutdown(int code){
        Logger.info("Stopping...");
        for(CleanUpAction action : cleanUpActions){
            action.cleanUp();
        }
        for(Thread thread : threads){
            while (thread.isAlive()){
                //Do nothing
            }
        }
        Logger.info("Cleaned up");
        System.exit(code);
    }

    public interface CleanUpAction{
        void cleanUp();
    }
}

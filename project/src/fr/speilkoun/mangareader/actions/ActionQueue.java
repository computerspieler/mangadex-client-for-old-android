package fr.speilkoun.mangareader.actions;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.util.Log;

public class ActionQueue implements Runnable {
    static String TAG = "ActionQueue";

    protected LinkedBlockingQueue<Action> queue;
    public ActionQueue() {
        queue = new LinkedBlockingQueue<Action>();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Action action = this.queue.poll(100, TimeUnit.MILLISECONDS);
                if(action == null) 
                    continue;

                action.process();
            } catch (InterruptedException e) {
                Log.e(TAG, "Got an error while polling actions: ", e);
                continue;
            }
        }
    }

    static ActionQueue INSTANCE = null;
    public static ActionQueue getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ActionQueue();
            Thread thread = new Thread(INSTANCE, "message_queue");
            thread.start();
        }
        return INSTANCE;
    }

    public static void sendAction(Action a)
    {
        try {
            getInstance().queue.put(a);
        } catch (InterruptedException e) {
            Log.e(TAG, "", e);
        }
    }
}

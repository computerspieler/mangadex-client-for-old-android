package fr.speilkoun.mangareader.actions;

import android.app.IntentService;
import android.content.Intent;

public interface Action {
	public abstract void run(IntentService service, Intent intent);
}

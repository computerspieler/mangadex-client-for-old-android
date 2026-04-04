package fr.speilkoun.mangareader.utils;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class Parameter<T> {
    String name;

    public Parameter(String name) {
        this.name = name;
    }

    public abstract void set(SharedPreferences.Editor editor, T value)
        throws InvalidValue;
    public abstract T get(SharedPreferences pref, Context ctx);


    public static final Parameter<String> DESTINATION = new Parameter<String>("destination") {
        @Override
        public void set(SharedPreferences.Editor editor, String value)
            throws InvalidValue
        {
            //TODO: Check path validity
            editor.putString(this.name, value);
        }

        @Override
        public String get(SharedPreferences pref, Context ctx)
        {
            return pref.getString(
                this.name, 
                ctx.getFileStreamPath("").getAbsolutePath()
            );
        }
    };
    public static final Parameter<Integer> DEFAULT_ZOOM = new Parameter<Integer>("default_zoom") {
        @Override
        public void set(SharedPreferences.Editor editor, Integer value)
            throws InvalidValue
        {
            if(value < 100)
                throw new InvalidValue(this);
            editor.putInt(this.name, value);
        }

        @Override
        public Integer get(SharedPreferences pref, Context ctx)
        { return pref.getInt(this.name, 100); }
    };
}


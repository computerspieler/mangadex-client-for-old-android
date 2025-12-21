package fr.speilkoun.mangareader.utils;

import android.content.Context;
import android.content.SharedPreferences;

public enum Parameters {
    DESTINATION("destination") {
        @Override
        public void setValue(SharedPreferences.Editor editor, String value)
            throws InvalidValueType, InvalidValue
        {
            //TODO: Check path validity
            editor.putString(this.name, value);
        }

        @Override
        public String getString(SharedPreferences pref, Context ctx)
            throws InvalidValueType
        {
            return pref.getString(
                this.name, 
                ctx.getFileStreamPath("").getAbsolutePath()
            );
        }
    },

    DEFAULT_ZOOM("default_zoom") {
        @Override
        public void setValue(SharedPreferences.Editor editor, int value)
            throws InvalidValueType, InvalidValue
        {
            if(value < 100)
                throw new InvalidValue(this);
            editor.putInt(this.name, value);
        }

        @Override
        public int getInt(SharedPreferences pref, Context ctx)
            throws InvalidValueType
        { return pref.getInt(this.name, 100); }
    };

    
    class ParameterException extends Exception {
        public final Parameters parameter;
        public ParameterException(Parameters parameter) {
            this.parameter = parameter;
        }
    }

    public class InvalidValueType extends ParameterException {
        public InvalidValueType(Parameters parameter)
        { super(parameter); }
    }
    public class InvalidValue extends ParameterException {
        public InvalidValue(Parameters parameter)
        { super(parameter); }
    }

    String name;
    Parameters(String name) {
        this.name = name;
    }

    public void setValue(SharedPreferences.Editor editor, String value)
        throws InvalidValueType, InvalidValue
    { throw new InvalidValueType(this); }

    public void setValue(SharedPreferences.Editor editor, int value)
        throws InvalidValueType, InvalidValue
    { throw new InvalidValueType(this); }

    public String getString(SharedPreferences pref, Context ctx)
        throws InvalidValueType
    { throw new InvalidValueType(this); }

    public int getInt(SharedPreferences pref, Context ctx)
        throws InvalidValueType
    { throw new InvalidValueType(this); }
}
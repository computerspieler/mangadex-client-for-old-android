package fr.speilkoun.mangareader.utils;

public class InvalidValue extends Exception {
    public final Parameter<?> parameter;
    public InvalidValue(Parameter<?> parameter)
    {
        this.parameter = parameter;
    }
}

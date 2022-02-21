package main.engine.structures.features;

import main.engine.Main;
import main.engine.ObjectCounter;

public interface Serializable
{
    int getId();
    default int giveId()
    {
        return ObjectCounter.objectNumber++;
    }
}

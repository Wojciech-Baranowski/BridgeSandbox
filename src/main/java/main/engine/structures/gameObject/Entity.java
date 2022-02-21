package main.engine.structures.gameObject;

import lombok.Getter;
import lombok.Setter;
import main.engine.structures.features.Measurable;

@Getter
@Setter
public class Entity implements Measurable
{
    protected Position pos;
    protected Dimensions dim;

    public Entity(Position pos, Dimensions dim)
    {
        this.pos = pos;
        this.dim = dim;
    }
}
package main.game.cardChoosePanel;

import main.engine.display.renderer.Renderer;
import main.engine.structures.gameObject.GameObject;
import main.engine.structures.gameObject.Position;
import main.game.Card;
import main.game.table.card.CardColor;
import main.game.table.card.CardFigure;

public class ChoiceCard extends Card
{
    public ChoiceCard(Position pos, GameObject parent, CardFigure figure, CardColor color) {
        super(pos, parent, figure, color);
    }

    @Override
    public void onClick() {
        incState();
    }

    public void render(Renderer r)
    {
        spriteRender(r);
        childrenRender(r);
        hoverRender(r, hovered, id);
        chosenRender(r);
    }

    protected void chosenRender(Renderer r)
    {
        if(currentState == 1)
            r.drawRectangle(pos, dim, INACTIVE_COLOR, 1, id);
    }
}
package main.game.table.buttons;

import main.engine.ProgramContainer;
import main.engine.structures.Button;
import main.engine.structures.gameObject.Dimensions;
import main.engine.structures.gameObject.GameObject;
import main.engine.structures.drawable.Rectangle;
import main.engine.structures.drawable.Text;
import main.engine.structures.gameObject.Position;
import main.engine.structures.observer.Observer;

import java.util.LinkedList;

import static main.game.GameConstants.*;

public class CardChooseButton extends Button
{
    private static final int DEFAULT_CARD_CHOOSE_BUTTON_WIDTH = 150;
    private static final int DEFAULT_CARD_CHOOSE_BUTTON_HEIGHT = 80;
    private static final int DEFAULT_CARD_CHOOSE_BUTTON_X = 198;
    private static final int DEFAULT_CARD_CHOOSE_BUTTON_Y = 442;
    private LinkedList<Observer> observers;

    public CardChooseButton(GameObject parent)
    {
        super(new Position(DEFAULT_CARD_CHOOSE_BUTTON_X, DEFAULT_CARD_CHOOSE_BUTTON_Y),
                new Dimensions(DEFAULT_CARD_CHOOSE_BUTTON_WIDTH, DEFAULT_CARD_CHOOSE_BUTTON_HEIGHT), parent);
        initializeSpriteList();
        observers = new LinkedList<>();
    }

    private void initializeSpriteList()
    {
        spriteList.add(new Rectangle(new Position(), dim, CYAN, BROWN, 1));
        spriteList.add(new Text("choose cards", new Position(8, 22), 28, GRAY, 1));
    }

    @Override
    public void attach(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observers);
    }

    @Override
    public void notifyObservers() {
        observers.forEach(Observer::update);
    }

    @Override
    public void onClick() {
        ProgramContainer.getProgramContainer().switchSceneToCardChoosePanel();
    }

    @Override
    public void onRelease() {

    }

    @Override
    public void onHold() {

    }
}
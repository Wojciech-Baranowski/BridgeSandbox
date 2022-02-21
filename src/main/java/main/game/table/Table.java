package main.game.table;

import lombok.Getter;
import lombok.Setter;
import main.engine.ProgramContainer;
import main.engine.display.Window;
import main.engine.structures.IntPair;
import main.engine.structures.gameObject.Dimensions;
import main.engine.structures.gameObject.GameObject;
import main.engine.structures.Scene;
import main.engine.structures.drawable.Rectangle;
import main.engine.structures.drawable.Text;
import main.engine.structures.gameObject.Position;
import main.engine.structures.observer.Observer;
import main.game.GameConstants;
import main.game.contractChoosePanel.ContractChooseButton;
import main.game.contractChoosePanel.ContractChoosePanel;
import main.game.table.solver.Solver;
import main.game.table.card.Card;
import main.game.table.card.CardColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static main.game.GameConstants.*;

public class Table extends GameObject implements Scene, Observer
{
    public static final char[] WRITTEN_COLORS = {'C', 'D', 'H', 'S', 'N'};//temporary
    @Getter
    @Setter
    private int contractId;
    @Getter
    @Setter
    private PlayerSide currentPlayer;
    @Getter
    private PlayerSide lastWinner;
    @Getter
    private IntPair taken;
    @Getter
    private Hand[] hand;
    @Getter
    private Card[] chosenCards;
    @Getter
    private Solver solver;
    private SolverButton solverButton;
    private ContractButton contractButton;

    public Table()
    {
        super(new Position(), new Dimensions(Window.WIDTH, Window.HEIGHT), null);
        initializeTable();
        initializeGame();
        initializeSpriteList();
        manageActivity();
        solver = new Solver(this);
        initializeButtons();
    }

    private void initializeTable() {
        taken = new IntPair();
        hand = new Hand[GameConstants.PLAYER_COUNT];
        chosenCards = new Card[GameConstants.PLAYER_COUNT];
    }

    private void initializeGame() {
        this.lastWinner = PlayerSide.N;
        this.currentPlayer = PlayerSide.N;
        dealRandom(13);
        setContractId(18);
    }

    private void initializeButtons()
    {
        solverButton = new SolverButton(this);
        children.add(solverButton);
        solverButton.attach(solver);
        contractButton = new ContractButton(this, contractId);
        children.add(contractButton);
    }

    private void initializeSpriteList()
    {
        spriteList.add(new Rectangle(new Position(), dim, GREEN, 1));
        spriteList.add(new Rectangle(new Position(411, 166), new Dimensions(375, 343), CYAN, BROWN, 1));
        loadTextSprites();
    }

    private void loadTextSprites()
    {
        spriteList.add(new Text("Contract; ", new Position(10, 25), DEFAULT_FONT_SIZE, GRAY, 1));
        spriteList.add(new Text("Current player; " + currentPlayer.getAsciiString(), new Position(10, 65), DEFAULT_FONT_SIZE, GRAY, 1));
        spriteList.add(new Text("Taken; N/S - " + taken.x + " | W/E - " + taken.y, new Position(10, 105),  DEFAULT_FONT_SIZE, GRAY, 1));
    }

    private void removeTextSprites()
    {
        for(int i = 0; i < spriteList.size(); i++)
        {
            if(spriteList.get(i).getClass().equals(Text.class))
                spriteList.remove(i--);
        }
    }

    private void reLoadTextSprites()
    {
        removeTextSprites();
        loadTextSprites();
    }

    @Override
    public void update()
    {
        updateCards();
        updateContractButton();
    }

    private void updateCards()
    {
        if(Card.getRecentlyPlayed() >= 0)
        {
            playCard(findPlayedCard());
            Card.setRecentlyPlayed(-1);
        }
    }

    private void updateContractButton()
    {
        if(ContractChooseButton.getRecentlyChosen() >= 0)
        {
            setContractId(ContractChooseButton.getRecentlyChosen());
            contractButton.reLoadTextSprites(contractId);
            ContractChooseButton.setRecentlyChosen(-1);
            ProgramContainer.getProgramContainer().switchSceneToTable();
        }
    }

    private void dealRandom(int cardAmount) {
        List<Integer> deck = createDeck();
        for (int i = 0; i < GameConstants.PLAYER_COUNT; i++) {
            hand[i] = dealToHand(deck, cardAmount, i);
            children.add(hand[i]);
            hand[i].attachObserversToCards(this);
        }
    }

    public void nextTurn() {
        hand[currentPlayer.ordinal()].removeCard(getPlayedCardId());
        currentPlayer = currentPlayer.nextPlayer();
        reLoadTextSprites();
        if (isPlayerFirstInTurn())
            summarizeTurn();
        manageActivity();
    }

    private PlayerSide selectWinner() {
        PlayerSide currentWinner = currentPlayer;
        CardColor currentAtu = chosenCards[currentPlayer.ordinal()].getColor();
        return compareCards(currentWinner, currentAtu);
    }

    private PlayerSide compareCards(PlayerSide currentWinner, CardColor currentAtu) {
        for (int i = 0; i < GameConstants.PLAYER_COUNT; i++) {
            if (i == currentPlayer.ordinal()) continue;
            if (isNewWinning(chosenCards[currentWinner.ordinal()], chosenCards[i], currentAtu))
                currentWinner = PlayerSide.values()[i];
        }
        return currentWinner;
    }

    private void manageActivity() {
        for (int i = 0; i < GameConstants.PLAYER_COUNT; i++) {
            manageHandActivity(hand[i]);
        }
    }

    private List<Integer> createDeck() {
        List<Integer> deck = new ArrayList<>();
        for (int i = 0; i < GameConstants.COLOR_COUNT * GameConstants.FIGURE_COUNT; i++) {
            deck.add(i);
        }
        Collections.shuffle(deck);
        return deck;
    }

    private Hand dealToHand(List<Integer> deck, int cardAmount, int playerId) {
        int[] temp = new int[cardAmount];
        for (int j = 0; j < cardAmount; j++) {
            temp[j] = deck.get(playerId * cardAmount + j);
        }
        Arrays.sort(temp);
        return new Hand(temp, cardAmount, playerId, this);
    }

    private Card findPlayedCard()
    {
        for(int i = 0; i < PLAYER_COUNT; i++)
        {
            for(int j = 0; j < hand[i].getCard().size(); j++)
            {
                if(hand[i].getCard().get(j).getId() == Card.getRecentlyPlayed())
                    return hand[i].getCard().get(j);
            }
        }
        return null;
    }

    public void playCard(Card card)
    {
        int handId = getCardsHandId(card);
        moveCardToCenter(card, handId);
        chosenCards[handId] = card;
        children.add(chosenCards[handId]);
        nextTurn();
    }

    private void moveCardToCenter(Card card, int handId)
    {
        card.setPos(new Position(Hand.OWNER_CENTER_X[handId], Hand.OWNER_CENTER_Y[handId]));
    }

    private int getCardsHandId(Card card)
    {
        for(int i = 0; i < PLAYER_COUNT; i++)
        {
            if(hand[i].getCard().contains(card))
                return i;
        }
        return -1;
    }

    private void summarizeTurn() {
        currentPlayer = lastWinner = selectWinner();
        reLoadTextSprites();
        clearTableCenter();
        addPoints();
    }

    private void addPoints() {
        if (lastWinner == PlayerSide.N || lastWinner == PlayerSide.S)
            taken.x++;
        else
            taken.y++;
        reLoadTextSprites();
    }

    private boolean isNewWinning(Card old, Card _new, CardColor currentAtu) {
        if (hasNewAtuAdvantage(old, _new))
            return true;
        if (!hasNewColorAdvantage(old, _new, currentAtu))
            return false;
        return hasNewFigureAdvantage(old, _new);
    }

    private void manageHandActivity(Hand hand) {
        for (int j = 0; j < hand.getCard().size(); j++)
        {
            hand.getCard().get(j).setActive(isCardToActivate(hand.getCard().get(j), !hand.hasColor(getFirstColorInTurn())));
        }
    }

    private boolean isCardToActivate(Card card, boolean hasVoid) {
        if (!isCardOwnedByCurrentPlayer(card))
            return false;
        if (isPlayerFirstInTurn())
            return true;
        if(isCardColorMatchingCurrentColor(card))
            return true;
        return hasVoid;
    }

    private void clearTableCenter() {
        for (int i = 0; i < GameConstants.PLAYER_COUNT; i++)
        {
            children.remove(chosenCards[i]);
            chosenCards[i] = null;
        }
    }

    private Card getPlayedCardId() {
        for (int i = 0; i < hand[currentPlayer.ordinal()].getCard().size(); i++) {
            if (hasAlreadyPlayed(currentPlayer) && isPlayedCardMatchingTable(i))
                return hand[currentPlayer.ordinal()].getCard().get(i);
        }
        return null;
    }

    private boolean isPlayedCardMatchingTable(int cardId) {
        return hand[currentPlayer.ordinal()].getCard().get(cardId).getCardId() == chosenCards[currentPlayer.ordinal()].getCardId();
    }

    private boolean hasNewAtuAdvantage(Card old, Card _new) {
        return old.getColor() != getAtu() && _new.getColor() == getAtu();
    }

    private boolean hasNewColorAdvantage(Card old, Card _new, CardColor currentAtu) {
        return ((_new.getColor() == currentAtu) && (_new.getColor() == getAtu() || old.getColor() != getAtu()));
    }

    private boolean hasNewFigureAdvantage(Card old, Card _new) {
        return old.getFigure().ordinal() < _new.getFigure().ordinal();
    }

    private boolean isCardOwnedByCurrentPlayer(Card card)
    {
        return hand[currentPlayer.ordinal()].getCard().contains(card);
    }

    private boolean isCardColorMatchingCurrentColor(Card card)
    {
        return card.getColor() == chosenCards[lastWinner.ordinal()].getColor();
    }

    private CardColor getFirstColorInTurn()
    {
        if(chosenCards[lastWinner.ordinal()] != null)
            return chosenCards[lastWinner.ordinal()].getColor();
        return CardColor.NO_ATU;
    }

    private boolean hasAlreadyPlayed(PlayerSide p)
    {
        return chosenCards[p.ordinal()] != null;
    }

    private boolean isPlayerFirstInTurn()
    {
        return currentPlayer == lastWinner;
    }

    private CardColor getAtu()
    {
        return CardColor.values()[contractId % (GameConstants.COLOR_COUNT + 1)];
    }

}
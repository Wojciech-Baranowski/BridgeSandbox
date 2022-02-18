package main.game.tablecontent;

import lombok.Getter;
import lombok.Setter;
import main.engine.*;
import main.engine.display.Renderer;
import main.engine.display.Window;
import main.engine.structures.State;
import main.game.GameConstants;
import main.game.solver.Solver;
import main.game.tablecontent.card.Card;
import main.game.tablecontent.card.CardColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Table implements State {
    public static final char[] WRITTEN_COLORS = {'C', 'D', 'H', 'S', 'N'};//temporary
    private final int width;
    private final int height;
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

    public Table(int width, int height) {
        this.width = width;
        this.height = height;
        initializeTable();
        initializeGame();
        manageActivity();
        solver = new Solver();
        solverButton = new SolverButton();
        contractButton = new ContractButton();
    }

    private void initializeTable() {
        taken = new IntPair();
        hand = new Hand[GameConstants.PLAYER_COUNT];
        chosenCards = new Card[GameConstants.PLAYER_COUNT];
    }

    private void initializeGame() {
        this.lastWinner = PlayerSide.N;
        this.currentPlayer = PlayerSide.N;
        dealRandom(5);
        setContractId(18);
    }

    private void dealRandom(int cardAmount) {
        List<Integer> deck = createDeck();
        for (int i = 0; i < GameConstants.PLAYER_COUNT; i++) {
            hand[i] = dealToHand(deck, cardAmount, i);
        }
    }

    public void nextTurn() {
        removeCard(getPlayedCardId());
        currentPlayer = currentPlayer.nextPlayer();
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

    @Override
    public void update(Window window, Input input, LoopTimer loopTimer)
    {
        for(int i = 0; i < GameConstants.PLAYER_COUNT; i++)
            hand[i].update(input, this);
        buttonsUpdate(input);
    }

    private void buttonsUpdate(Input input)
    {
        solverButton.buttonUpdate(input, this);
        contractButton.buttonUpdate(input, this);
    }

    @Override
    public void render(Renderer r)
    {
        renderBackground(r);
        renderGameInfo(r);
        renderChosenCards(r);
        renderHands(r);
        renderButtons(r);
        solverButton.render(r);
    }

    private void renderBackground(Renderer r)
    {
        r.drawRectangle(0, 0, width, height, GameConstants.GREEN, 1);
        r.drawRectangle(412, 168, 373, 339, GameConstants.BROWN, 1);
        r.drawRectangle(410, 166, 377, 343, GameConstants.CYAN, 1);
    }

    private void renderGameInfo(Renderer r)
    {
        r.drawText("Current player; " + currentPlayer.getAsciiString(), 10, 65, GameConstants.GRAY, GameConstants.DEFAULT_FONT_SIZE, 1);
        r.drawText("Taken; N/S - " + taken.x + " | W/E - " + taken.y, 10, 105, GameConstants.GRAY, GameConstants.DEFAULT_FONT_SIZE, 1);
    }

    private void renderButtons(Renderer r)
    {
        solverButton.render(r);
        contractButton.render(r, contractId, getAtu().ordinal());
    }

    private void renderChosenCards(Renderer r)
    {
        for(int i = 0; i < GameConstants.PLAYER_COUNT; i++)
        {
            if(chosenCards[i] != null)
                chosenCards[i].render(r);
        }
    }

    private void renderHands(Renderer r)
    {
        for(int i = 0; i < GameConstants.PLAYER_COUNT; i++)
            hand[i].render(r);
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
        return new Hand(temp, cardAmount, playerId);
    }

    private void removeCard(int id) {
        if (id < 0) return;
        hand[currentPlayer.ordinal()].getCard().remove(id);
        repositionCards();
    }

    private void summarizeTurn() {
        currentPlayer = lastWinner = selectWinner();
        clearTableCenter();
        addPoints();
    }

    private void addPoints() {
        if (lastWinner == PlayerSide.N || lastWinner == PlayerSide.S)
            taken.x++;
        else
            taken.y++;
    }

    private void repositionCards() {
        for (int i = 0; i < hand[currentPlayer.ordinal()].getCard().size(); i++) {
            hand[currentPlayer.ordinal()].getCard().get(i).setX(hand[currentPlayer.ordinal()].getX() + i * Hand.CARD_SPACE);
        }
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
            chosenCards[i] = null;
    }

    private int getPlayedCardId() {
        for (int i = 0; i < hand[currentPlayer.ordinal()].getCard().size(); i++) {
            if (hasAlreadyPlayed(currentPlayer) && isPlayedCardMatchingTable(i))
                return i;
        }
        return -1;
    }

    private boolean isPlayedCardMatchingTable(int cardId) {
        return hand[currentPlayer.ordinal()].getCard().get(cardId).getId() == chosenCards[currentPlayer.ordinal()].getId();
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
package main.game.table.solver;

import main.engine.structures.IntPair;
import main.engine.structures.observer.Observable;
import main.engine.structures.observer.Observer;
import main.game.table.bestMovesTable.BestMovesTable;
import main.game.table.card.CardColor;
import main.game.table.card.CardFigure;
import main.game.GameConstants;
import main.game.table.Table;

import java.util.LinkedList;
import java.util.List;

import static main.game.GameConstants.*;

public class Solver implements Observer
{
    private final Table table;
    private int atu;
    private GameState initialState;
    private Feedback feedback;
    private BestMovesTable bestMovesTable;
    public Solver(Table table, BestMovesTable bestMovesTable)
    {
        this.table = table;
        this.bestMovesTable = bestMovesTable;
    }
    @Override
    public void update(Observable o, Object arg) {
        initialize();
    }
    private int chooseWinner(GameState g)
    {
        IntPair[] lastCards = new IntPair[GameConstants.PLAYER_COUNT];
        for(int i = 0; i < GameConstants.PLAYER_COUNT; i++)
        {
            lastCards[(i + g.lastWinner) % GameConstants.PLAYER_COUNT] = new IntPair(g.trace.get(g.trace.size() - 4 + i));
        }
        int currentWinner = g.lastWinner;
        int currentAtu = lastCards[g.lastWinner].x;
        for(int i = 0; i < GameConstants.PLAYER_COUNT; i++)
        {
            if(i == g.lastWinner)
                continue;
            if(lastCards[currentWinner].x != atu && lastCards[i].x == atu)
            {
                currentWinner = i;
            }
            else if((lastCards[i].x != atu) && ((lastCards[i].x != currentAtu) || (lastCards[currentWinner].x == atu)))
            {
                ;
            }
            else if(lastCards[currentWinner].y < lastCards[i].y)
            {
                currentWinner = i;
            }

        }
        return currentWinner;
    }
    private boolean valid(GameState g, int id)
    {
        if(g.cards[g.currPlayer].get(id).x == g.currColor || g.currColor == -1)
            return true;
        for(int i = 0; i < g.cards[g.currPlayer].size(); i++)
        {
            if(g.cards[g.currPlayer].get(i).x == g.currColor)
                return false;
        }
        return true;
    }
    private Feedback move(GameState g, IntPair card, int id)
    {
        g.trace.add(card);
        g.cards[g.currPlayer].remove(id);
        if((g.currPlayer + 1) % GameConstants.PLAYER_COUNT == g.lastWinner)
        {
            g.currPlayer = chooseWinner(g);
            g.lastWinner = g.currPlayer;
            if(g.currPlayer % 2 == 0)
                g.taken.x++;
            else
                g.taken.y++;
        }
        else
        {
            if(g.currPlayer == g.lastWinner)
                g.currColor = card.x;
            g.currPlayer = (g.currPlayer + 1) % GameConstants.PLAYER_COUNT;
        }
        Feedback f;
        Feedback feedback = new Feedback();
        int moves = 0;
        feedback.amount = 1000 * (g.currPlayer % 2 == 1 ? 1 : 0) - 1;
        if(g.cards[g.currPlayer].isEmpty())
        {
            feedback.amount = g.taken.x;
            feedback.trace = g.trace;
            feedback.moves = 1;
            return feedback;
        }
        for(int i = 0; i < g.cards[g.currPlayer].size(); i++)
        {
            if(!valid(g, i))
                continue;
            f = move(new GameState(g), new IntPair(g.cards[g.currPlayer].get(i)), i);
            moves += f.moves;
            if((g.currPlayer % 2 == 1 && feedback.amount > f.amount) || (g.currPlayer % 2 == 0 && feedback.amount < f.amount))
            {
                feedback = new Feedback(f);
            }
        }
        feedback.moves = moves + 1;
        return feedback;
    }

    public void initialize() {
        atu = table.getGameManager().getContractId() % 5;
        initialState = new GameState(table.getGameManager());
        feedback = initialMove(initialState);
        putFeedbackInTable(feedback);
        for(int i = 0; i < feedback.trace.size(); i++)
        {
            System.out.println(CardFigure.values()[feedback.trace.get(i).y] + " " + CardColor.values()[feedback.trace.get(i).x]);
        }
        System.out.println(feedback.moves + " " + feedback.amount);
    }

    private void putFeedbackInTable(Feedback feedback){
        bestMovesTable.updateCardSignatureFields(getFeedbackCardIds(feedback));
    }

    private List<Integer> getFeedbackCardIds(Feedback feedback){
        List<Integer> cardIds = new LinkedList<>();
        for(IntPair card : feedback.trace)
            cardIds.add(card.y * COLOR_COUNT + card.x);
        return getTrimmedFeedbackCardIdsList(cardIds);
    }

    private List<Integer> getTrimmedFeedbackCardIdsList(List<Integer> cardIds){
        for(int i = 0; i < getRedundantCardSignaturesNumber(); i++)
            cardIds.set(i, null);
        return cardIds;
    }

    private int getRedundantCardSignaturesNumber(){
        return (table.getCurrentPlayer().ordinal() - table.getLastWinner().ordinal() + PLAYER_COUNT) % PLAYER_COUNT;
    }

    public Feedback initialMove(GameState g)
    {
        Feedback f;
        Feedback feedback = new Feedback();
        int moves = 0;
        feedback.amount = 1000 * (g.currPlayer % 2 == 1 ? 1 : 0) - 1;
        for(int i = 0; i < g.cards[g.currPlayer].size(); i++)
        {
            if(!valid(g, i))
                continue;
            f = move(new GameState(g), new IntPair(g.cards[g.currPlayer].get(i)), i);
            moves += f.moves;
            if((g.currPlayer % 2 == 1 && feedback.amount > f.amount) || (g.currPlayer % 2 == 0 && feedback.amount < f.amount))
            {
                feedback = new Feedback(f);
            }
        }
        feedback.moves = moves + 1;
        return feedback;
    }
}

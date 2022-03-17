package main.game.table.bestMovesTable;

import main.engine.structures.drawable.Rectangle;
import main.engine.structures.gameObject.Dimensions;
import main.engine.structures.gameObject.GameObject;
import main.engine.structures.gameObject.Position;
import main.game.table.PlayerSide;

import java.util.List;

import static main.game.GameConstants.*;

public class BestMovesTable extends GameObject
{
    public static final int POS_X = 987;
    public static final int POS_Y = 384;
    public static final int WIDTH = 189;
    public static final int HEIGHT = 274;
    private BestMovesTableField[] playerTagFields;
    private BestMovesTableField[] cardSignatureFields;

    public BestMovesTable(GameObject parent) {
        super(new Position(POS_X, POS_Y), new Dimensions(WIDTH, HEIGHT), parent);
        initializePlayerTagFields();
        initializeCardSignatureFields();
        initializeSprites();
    }

    public void updateCardSignatureFields(List<Integer> cardIds){
        for(int i = 0; i < DECK_SIZE; i++){
            if(i < cardIds.size() && cardIds.get(i) != null)
                cardSignatureFields[i].spriteReload(cardIds.get(i));
            else
                cardSignatureFields[i].spriteReload(null);
        }
    }

    private void initializeSprites(){
        spriteList.add(new Rectangle(new Position(), dim, CYAN, BROWN, 1));
    }

    private void initializePlayerTagFields(){
        playerTagFields = new BestMovesTableField[PLAYER_COUNT];
        for(int i = 0; i < PLAYER_COUNT; i++)
            initializePlayerTagField(i);
    }

    private void initializePlayerTagField(int playerId){
        playerTagFields[playerId] = new BestMovesTableField(getPlayerTagPosition(playerId), this, getPlayerTagString(playerId));
        children.add(playerTagFields[playerId]);
    }

    private void initializeCardSignatureFields(){
        cardSignatureFields = new BestMovesTableField[DECK_SIZE];
        for(int i = 0; i < DECK_SIZE; i++)
            initializeCardSignatureField(i);
    }

    private void initializeCardSignatureField(int fieldId){
        cardSignatureFields[fieldId] = new BestMovesTableField(getFieldPosition(fieldId), this);
        children.add(cardSignatureFields[fieldId]);
    }

    private Position getFieldPosition(int fieldId){
        int x = BestMovesTable.POS_X + (fieldId % 4) * (BestMovesTableField.FIELD_WIDTH - 1);
        int y = BestMovesTable.POS_Y + (fieldId / 4 + 1) * (BestMovesTableField.FIELD_HEIGHT - 1);
        return new Position(x, y);
    }

    private Position getPlayerTagPosition(int playerId){
        int x = BestMovesTable.POS_X + playerId * (BestMovesTableField.FIELD_WIDTH - 1);
        return new Position(x, BestMovesTable.POS_Y);
    }

    private String getPlayerTagString(int playerId){
        return " " + PlayerSide.values()[playerId].getAsciiString();
    }
}

package main.engine.display.renderer;

import main.engine.display.font.Font;
import main.engine.structures.drawable.Text;
import main.engine.structures.gameObject.Position;

public class TextRenderer
{
    private static final int UNICODE_OFFSET = 32;
    private Font font;
    private Renderer renderer;
    private Position currentPos;

    public TextRenderer(Renderer renderer, Font font)
    {
        this.renderer = renderer;
        this.font = font;
    }

    public void drawText(Text text, Position textPos, int owner)
    {
        currentPos = new Position();
        for(int i = 0; i < text.getText().length(); i++) {
            processSymbol(text, textPos, owner, i);
        }
    }

    private void processSymbol(Text text, Position textPos, int owner, int symbolIndex)
    {
        if(text.getText().codePointAt(symbolIndex) == '\n')
            currentPos = getNewLinePosition(text);
        else
            drawSymbol(getUnicodeId(text, symbolIndex), text, textPos, owner);
    }

    private void drawSymbol(int unicodeId, Text text, Position textPos, int owner)
    {
        Position absoluteSymbolPos = getSymbolAbsolutePosition(textPos);
        renderer.drawSymbol(unicodeId, text, absoluteSymbolPos, owner);
        moveCursor(text, unicodeId);
    }

    private void moveCursor(Text text, int unicodeId)
    {
        currentPos.incX(font.getSymbols()[text.getSize() / 2][unicodeId].getDim().getW());
    }

    private Position getNewLinePosition(Text text)
    {
        int newLineYPos = currentPos.getY() + font.getSymbols()[text.getSize() / 2][0].getDim().getH();
        return new Position(0, newLineYPos);
    }

    private Position getSymbolAbsolutePosition(Position textPos)
    {
        int absoluteXPos = currentPos.getX() + textPos.getX();
        int absoluteYPos = currentPos.getY() + textPos.getY();
        return new Position(absoluteXPos, absoluteYPos);
    }

    private int getUnicodeId(Text text, int symbolIndex)
    {
        return text.getText().codePointAt(symbolIndex) - UNICODE_OFFSET;
    }
}

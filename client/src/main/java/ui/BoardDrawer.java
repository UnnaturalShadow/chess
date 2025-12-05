package ui;

import chess.*;
import java.util.Collection;

//import static ui.EscapeSequences.SET_BG_COLOR_BROWN;
import static ui.EscapeSequences.*;


public class BoardDrawer
{
    private final ChessBoard board;
    private final ChessGame.TeamColor perspective;
    private final Collection<ChessPosition> highlights;

    // Board square colors
    private static final String LIGHT = SET_BG_COLOR_WHITE;
    private static final String DARK = SET_BG_COLOR_BLACK;

    // Highlight colors
    private static final String HIGHLIGHT_LIGHT = EscapeSequences.SET_BG_COLOR_YELLOW;
    private static final String HIGHLIGHT_DARK = EscapeSequences.SET_BG_COLOR_GREEN;

    // Border color (brown background, black text)
    private static final String BORDER = SET_BG_COLOR_DARK_GREEN + SET_TEXT_COLOR_BLACK;

    public BoardDrawer(ChessBoard board, ChessGame.TeamColor perspective,
                       Collection<ChessPosition> highlights)
    {
        this.board = board;
        this.perspective = perspective;
        this.highlights = highlights;
    }

    public void print()
    {
        System.out.println(); // blank line before board
        printHeader();
        printBoard();
        printHeader();
    }

    private void printHeader()
    {
        System.out.print(BORDER + "   " + RESET_BG_COLOR + RESET_TEXT_COLOR); // no extra space

        char[] cols = (perspective == ChessGame.TeamColor.WHITE)
                ? new char[]{'a','b','c','d','e','f','g','h', ' '}
                : new char[]{'h','g','f','e','d','c','b','a', ' '};

        for (char c : cols)
        {
            System.out.print(BORDER + " " + c + " " + RESET_BG_COLOR + RESET_TEXT_COLOR);
        }
        System.out.println();
    }

    private void printBoard()
    {
        int[] rows = (perspective == ChessGame.TeamColor.WHITE)
                ? new int[]{8,7,6,5,4,3,2,1}
                : new int[]{1,2,3,4,5,6,7,8};

        for (int r : rows)
        {
            // Left border number
            System.out.print(BORDER + " " + r + " " + RESET_BG_COLOR + RESET_TEXT_COLOR);

            int[] cols = (perspective == ChessGame.TeamColor.WHITE)
                    ? new int[]{1,2,3,4,5,6,7,8}
                    : new int[]{8,7,6,5,4,3,2,1};

            for (int c : cols)
            {
                ChessPosition pos = new ChessPosition(r, c);
                boolean darkSquare = ((r + c) % 2 == 0);

                String baseColor = darkSquare ? DARK : LIGHT;

                if (highlights != null && highlights.contains(pos))
                {
                    baseColor = darkSquare ? HIGHLIGHT_DARK : HIGHLIGHT_LIGHT;
                }

                ChessPiece piece = board.getPiece(pos);
                String text = "   ";
                if (piece != null)
                {
                    text = " " + letterFor(piece) + " ";
                }

                System.out.print(baseColor + text + RESET_BG_COLOR + RESET_TEXT_COLOR);
            }

            // Right border number
            System.out.print(BORDER + " " + r + " " + RESET_BG_COLOR + RESET_TEXT_COLOR);
            System.out.println();
        }
    }

    private String letterFor(ChessPiece p)
    {
        char ch;
        switch (p.getPieceType())
        {
            case KING -> ch = 'k';
            case QUEEN -> ch = 'q';
            case ROOK -> ch = 'r';
            case BISHOP -> ch = 'b';
            case KNIGHT -> ch = 'n';
            case PAWN -> ch = 'p';
            default -> ch = '?';
        }

        String letter = (p.getTeamColor() == ChessGame.TeamColor.WHITE)
                ? Character.toString(Character.toUpperCase(ch))
                : Character.toString(Character.toLowerCase(ch));

        if (p.getTeamColor() == ChessGame.TeamColor.WHITE)
        {
            return SET_TEXT_COLOR_BLUE + letter + RESET_TEXT_COLOR;
        }
        else
        {
            return SET_TEXT_COLOR_RED + letter + RESET_TEXT_COLOR;
        }
    }
}

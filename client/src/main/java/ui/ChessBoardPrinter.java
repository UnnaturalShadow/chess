package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPosition;
import chess.ChessPiece;

import java.util.Set;

public class ChessBoardPrinter
{
    private final ChessBoard board;

    private final String border = EscapeSequences.SET_BG_COLOR_BLACK + EscapeSequences.SET_TEXT_COLOR_WHITE;
    private final String reset = EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR;

    public ChessBoardPrinter(ChessGame game)
    {
        this.board = game.getBoard();
    }

    // =========================
    // NORMAL PRINT
    // =========================
    public void printBoard(boolean blackPerspective)
    {
        printBoardWithHighlights(blackPerspective, null);
    }

    // =========================
    // HIGHLIGHT PRINT
    // =========================
    public void printBoardWithHighlights(boolean blackPerspective, Set<ChessPosition> highlights)
    {
        String[] cols = blackPerspective
                ? new String[]{"h","g","f","e","d","c","b","a"}
                : new String[]{"a","b","c","d","e","f","g","h"};

        System.out.print(border + "   ");
        for (String c : cols)
        {
            System.out.print(" " + c + " ");
        }
        System.out.println("   " + reset);

        for (int r = 0; r < 8; r++)
        {
            int row = blackPerspective ? r + 1 : 8 - r;
            int displayRow = row;

            System.out.print(border + " " + displayRow + " " + reset);

            for (int c = 0; c < 8; c++)
            {
                int col = blackPerspective ? 8 - c : c + 1;

                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                boolean isHighlight = highlights != null && highlights.contains(pos);
                boolean light = (row + col) % 2 == 0;

                String bgColor;

                // 🔥 Highlight overrides board color
                if (isHighlight)
                {
                    bgColor = EscapeSequences.SET_BG_COLOR_YELLOW;
                }
                else
                {
                    bgColor = light
                            ? EscapeSequences.SET_BG_COLOR_BLUE
                            : EscapeSequences.SET_BG_COLOR_WHITE;
                }

                if (piece == null)
                {
                    System.out.print(bgColor + "   ");
                }
                else
                {
                    System.out.print(bgColor + " " + letterFor(piece) + bgColor + " ");
                }
            }

            System.out.println(border + " " + displayRow + " " + reset);
        }

        System.out.print(border + "   ");
        for (String c : cols)
        {
            System.out.print(" " + c + " ");
        }
        System.out.println("   " + reset);
    }

    // =========================
    // PIECE RENDERING
    // =========================
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
            return EscapeSequences.SET_TEXT_COLOR_RED + letter + reset;
        }
        else
        {
            return EscapeSequences.SET_TEXT_COLOR_BLACK + letter + reset;
        }
    }
}

package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static ui.EscapeSequences.*;

public class BoardDrawer
{
    public enum SquareColor
    {
        LIGHT,
        DARK
    }

    private final Map<SquareColor, String> squareColorCodes = Map.of(
            SquareColor.DARK, SET_BG_COLOR_BLACK,
            SquareColor.LIGHT, SET_BG_COLOR_LIGHT_GREY
    );

    private final Map<ChessPiece.PieceType, String> whiteTypeToString = Map.of(
            ChessPiece.PieceType.KING, WHITE_KING,
            ChessPiece.PieceType.QUEEN, WHITE_QUEEN,
            ChessPiece.PieceType.BISHOP, WHITE_BISHOP,
            ChessPiece.PieceType.KNIGHT, WHITE_KNIGHT,
            ChessPiece.PieceType.ROOK, WHITE_ROOK,
            ChessPiece.PieceType.PAWN, WHITE_PAWN
    );
    private final Map<ChessPiece.PieceType, String> blackTypeToString = Map.of(
            ChessPiece.PieceType.KING, BLACK_KING,
            ChessPiece.PieceType.QUEEN, BLACK_QUEEN,
            ChessPiece.PieceType.BISHOP, BLACK_BISHOP,
            ChessPiece.PieceType.KNIGHT, BLACK_KNIGHT,
            ChessPiece.PieceType.ROOK, BLACK_ROOK,
            ChessPiece.PieceType.PAWN, BLACK_PAWN
    );



    private String getPieceString(ChessPiece piece)
    {
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
        {
            return whiteTypeToString.get(piece.getPieceType());
        } else
        {
            return blackTypeToString.get(piece.getPieceType());
        }
    }

    PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    private final ChessBoard board;
    private final ChessGame.TeamColor perspective;
    public BoardDrawer(ChessBoard board, ChessGame.TeamColor perspective)
    {
        board.resetBoard();
        this.board = board;
        this.perspective = perspective;
    }

    public void print()
    {
        out.print(SET_TEXT_COLOR_WHITE);
        SquareColor startingSquareColor = SquareColor.LIGHT;
        for (int i = 0; i < 8; i++)
        {
            int rowId = perspective == ChessGame.TeamColor.BLACK ? (7-i): i;

            ChessPiece[] row = board.getTiles()[rowId];
            startingSquareColor = printRowOfSquares(row, startingSquareColor, rowId);
        }
        out.print(RESET_BG_COLOR + " ");
        for (int i = 0; i < 8; i++)
        {
            int rowId = perspective == ChessGame.TeamColor.BLACK ? (7-i): i;
            out.print("\u2003" + (char) (rowId + 97) + " ");
        }
    }

    SquareColor printRowOfSquares(ChessPiece[] pieces, SquareColor startingSquareColor, int row)
    {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        SquareColor currentColor = startingSquareColor;

        out.print(RESET_BG_COLOR);
        out.print((8-row));
        for (int width = 0; width < 8; width++)
        {
            out.print(squareColorCodes.get(currentColor));

            if (pieces[width] != null)
            {
                out.print(getPieceString(pieces[width]));
            } else
            {
                out.print(EMPTY);
            }

            currentColor = swapColor(currentColor);
        }

        out.print(RESET_BG_COLOR + "\n");
        return swapColor(currentColor);
    }

    private SquareColor swapColor(SquareColor currentColor)
    {
        if (currentColor == SquareColor.LIGHT)
        {
            return SquareColor.DARK;
        } else
        {
            return SquareColor.LIGHT;
        }
    }
}

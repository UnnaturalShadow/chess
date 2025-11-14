package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static ui.EscapeSequences.*;

public class BoardDrawer {

    public enum SquareColor {
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

    private final PrintStream out =
            new PrintStream(System.out, true, StandardCharsets.UTF_8);

    private final ChessBoard board;
    private final ChessGame.TeamColor perspective;

    public BoardDrawer(ChessBoard board, ChessGame.TeamColor perspective) {
        this.board = board;
        this.perspective = perspective;
    }

    private String getPieceString(ChessPiece piece) {
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            return whiteTypeToString.get(piece.getPieceType());
        }
        return blackTypeToString.get(piece.getPieceType());
    }

    public void print() {
        out.print(SET_TEXT_COLOR_WHITE);

        SquareColor rowStartColor = SquareColor.LIGHT;

        for (int row = 0; row < 8; row++) {

            int actualRow = (perspective == ChessGame.TeamColor.BLACK)
                    ? 7 - row   // flip vertically
                    : row;

            ChessPiece[] rawRow = board.getTiles()[actualRow];
            ChessPiece[] displayRow = new ChessPiece[8];

            // flip columns for black
            for (int col = 0; col < 8; col++) {
                displayRow[col] = (perspective == ChessGame.TeamColor.BLACK)
                        ? rawRow[7 - col]
                        : rawRow[col];
            }

            rowStartColor = printRow(displayRow, rowStartColor, actualRow);
        }

        // bottom file letters
        out.print(RESET_BG_COLOR);
        out.print(RESET_TEXT_COLOR);
        out.print(" ");

        for (int col = 0; col < 8; col++) {
            int displayCol = (perspective == ChessGame.TeamColor.BLACK)
                    ? 7 - col
                    : col;

            out.print("\u2003" + (char) ('a' + displayCol) + " ");
        }

        out.println();
    }

    private SquareColor printRow(ChessPiece[] row, SquareColor starting, int actualRow) {
        SquareColor currentColor = starting;

        // print rank number
        out.print(RESET_BG_COLOR);
        out.print(RESET_TEXT_COLOR);
        out.print(8 - actualRow);

        for (int col = 0; col < 8; col++) {
            out.print(squareColorCodes.get(currentColor));

            ChessPiece piece = row[col];
            if (piece != null) {
                out.print(SET_TEXT_COLOR_WHITE);
                out.print(getPieceString(piece));
                out.print(RESET_TEXT_COLOR);
            } else {
                out.print(EMPTY); // empty square
            }

            currentColor = flip(currentColor);
        }

        out.print(RESET_BG_COLOR);
        out.print(RESET_TEXT_COLOR);
        out.println();

        return flip(currentColor);
    }

    private SquareColor flip(SquareColor c) {
        return (c == SquareColor.LIGHT ? SquareColor.DARK : SquareColor.LIGHT);
    }
}

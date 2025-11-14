package ui;

import chess.ChessGame;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static ui.EscapeSequences.*;

public class BoardDrawer {

    private static final int BOARD_SIZE_IN_SQUARES = 8;

    // Board arrays with embedded ANSI text color codes
    private static final String[][] WHITE_CHESS_START  = {
            {SET_TEXT_COLOR_BLACK + " R " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " N " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " B " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " Q " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " K " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " B " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " N " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " R " + RESET_TEXT_COLOR},
            {SET_TEXT_COLOR_BLACK + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " P " + RESET_TEXT_COLOR},
            {"   ","   ","   ","   ","   ","   ","   ","   "},
            {"   ","   ","   ","   ","   ","   ","   ","   "},
            {"   ","   ","   ","   ","   ","   ","   ","   "},
            {"   ","   ","   ","   ","   ","   ","   ","   "},
            {SET_TEXT_COLOR_RED + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " P " + RESET_TEXT_COLOR},
            {SET_TEXT_COLOR_RED + " R " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " N " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " B " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " Q " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " K " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " B " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " N " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " R " + RESET_TEXT_COLOR}
    };

    private static final String[][] BLACK_CHESS_START= {
            {SET_TEXT_COLOR_RED + " R " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " N " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " B " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " K " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " Q " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " B " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " N " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " R " + RESET_TEXT_COLOR},
            {SET_TEXT_COLOR_RED + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_RED + " P " + RESET_TEXT_COLOR},
            {"   ","   ","   ","   ","   ","   ","   ","   "},
            {"   ","   ","   ","   ","   ","   ","   ","   "},
            {"   ","   ","   ","   ","   ","   ","   ","   "},
            {"   ","   ","   ","   ","   ","   ","   ","   "},
            {SET_TEXT_COLOR_BLACK + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " P " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " P " + RESET_TEXT_COLOR},
            {SET_TEXT_COLOR_BLACK + " R " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " N " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " B " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " K " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " Q " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " B " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " N " + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_BLACK + " R " + RESET_TEXT_COLOR}
    };

    public void render(ChessGame.TeamColor teamColor){
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        String whiteHeader = "    a  b  c  d  e  f  g  h    ";
        String blackHeader = "    h  g  f  e  d  c  b  a    ";

        if (teamColor == ChessGame.TeamColor.WHITE){
            drawHeader(out, whiteHeader);
            drawChessBoard(out, WHITE_CHESS_START);
            drawHeader(out, whiteHeader);
        } else if (teamColor == ChessGame.TeamColor.BLACK) {
            drawHeader(out, blackHeader);
            drawChessBoard(out, BLACK_CHESS_START);
            drawHeader(out, blackHeader);
        }
    }

    private static void drawHeader(PrintStream out, String headerString) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);
        out.print(headerString);
        out.print(RESET_BG_COLOR);
        out.print(SET_TEXT_COLOR_BLACK);
        out.println();
    }

    private static void drawChessBoard(PrintStream out, String[][] boardArray) {
        for (int row = 0; row < BOARD_SIZE_IN_SQUARES; ++row) {

            // Left-side number
            out.print(SET_BG_COLOR_BLACK);
            out.print(SET_TEXT_COLOR_WHITE);
            out.print(" " + (8 - row) + " ");

            for (int col = 0; col < BOARD_SIZE_IN_SQUARES; ++col) {
                boolean isWhiteSquare = (row + col) % 2 == 0;

                if (isWhiteSquare){
                    out.print(RESET_BG_COLOR);
                } else {
                    out.print(SET_BG_COLOR_BLUE);
                }

                // print the piece (already has color embedded)
                out.print(boardArray[row][col]);
            }

            // Right-side number
            out.print(SET_BG_COLOR_BLACK);
            out.print(SET_TEXT_COLOR_WHITE);
            out.print(" " + (8 - row) + " ");

            out.print(RESET_BG_COLOR);
            out.print(SET_TEXT_COLOR_BLACK);
            out.println();
        }
    }
}

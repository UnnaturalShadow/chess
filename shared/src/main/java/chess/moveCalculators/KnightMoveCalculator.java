package chess.moveCalculators;

import chess.ChessBoard;
import chess.ChessPosition;

public class KnightMoveCalculator extends MoveCalculator
{
    int[][] knightModifiers = {
            {2,1},
            {2, -1},
            {-2,1},
            {-2, -1},
            {1,2},
            {1,-2},
            {-1,2},
            {-1,-2}
    };

    KnightMoveCalculator(ChessBoard board, ChessPosition position)
    {
        super(board, position);
        addMovesUsingModifiers(knightModifiers);
    }
}
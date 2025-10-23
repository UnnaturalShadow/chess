package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessPosition;

public class KingMoveCalculator extends MoveCalculator
{
    int[][] kingModifiers =
    {
            {1,-1},
            {1,0},
            {1,1},
            {0, 1},
            {0,-1},
            {-1,-1},
            {-1,0},
            {-1,1}
    };

    KingMoveCalculator(ChessBoard board,ChessPosition position)
    {
        super(board, position);
        addMovesUsingModifiers(kingModifiers);
    }
}

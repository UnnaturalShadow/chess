package chess.moveCalculators;

import chess.ChessBoard;
import chess.ChessPosition;

public class PawnMoveCalculator extends MoveCalculator
{
    PawnMoveCalculator(ChessBoard board,ChessPosition position)
    {
        super(board, position);
        pawnMoves();

    }
}

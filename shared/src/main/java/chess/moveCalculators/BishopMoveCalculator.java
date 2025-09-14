package chess.moveCalculators;

import chess.ChessBoard;
import chess.ChessPosition;

public class BishopMoveCalculator extends MoveCalculator
{
    BishopMoveCalculator(ChessBoard board, ChessPosition position)
    {
        super(board, position);
        diagonalMoves();
    }
}
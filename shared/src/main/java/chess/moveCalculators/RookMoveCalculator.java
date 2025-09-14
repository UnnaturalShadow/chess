package chess.moveCalculators;

import chess.ChessBoard;
import chess.ChessPosition;

public class RookMoveCalculator extends MoveCalculator
{
    RookMoveCalculator(ChessBoard board, ChessPosition position)
    {
        super(board, position);
        straightMoves();
    }
}
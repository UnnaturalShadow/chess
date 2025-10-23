package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessPosition;

public class QueenMoveCalculator extends MoveCalculator
{
    QueenMoveCalculator(ChessBoard board, ChessPosition position)
    {
        super(board, position);
        diagonalMoves();
        straightMoves();
    }
}

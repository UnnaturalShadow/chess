package chess.moveCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class MoveCalculator
{
    ChessPosition position;
    ChessBoard board;
    ChessPiece piece;
    ArrayList<ChessMove> moves = new ArrayList<>();

    MoveCalculator (ChessBoard board, ChessPosition position)
    {
        this.position = position;
        this.board = board;
        this.piece = board.getPiece(position);
    }

    public Collection<ChessMove> returnMoves()
    {
        return moves;
    }

    public void straightMoves()
    {
        int row = position.getRow();
        int col = position.getColumn();

        for(int i = row + 1; i < 7; i++)
        {
            if(checkCollision(i, col))
            {
                break;
            }
            moves.add(new ChessMove(this.position, new ChessPosition(i, col), null));
        }
        for(int i = row - 1; i > 0; i--)
        {
            if(checkCollision(i, col))
            {
                break;
            }
            moves.add(new ChessMove(this.position, new ChessPosition(i, col), null));
        }
        for(int i = col + 1; i < 7; i++)
        {
            if(checkCollision(row, i))
            {
                break;
            }
            moves.add(new ChessMove(this.position, new ChessPosition(row, i), null));
        }
        for(int i = col - 1; i > 0; i--)
        {
            if(checkCollision(row, i))
            {
                break;
            }
            moves.add(new ChessMove(this.position, new ChessPosition(row, i), null));
        }

    }

    public void diagonalMoves()
    {
        int row = position.getRow();
        int col = position.getColumn();

        for(int i = row + 1; i < 7; i++)
        {
            for(int j = col + 1; j < 7; j++) {
                if (checkCollision(i, j))
                {
                    break;
                }
                moves.add(new ChessMove(this.position, new ChessPosition(i, j), null));
            }
        }
        for(int i = row - 1; i > 0; i--)
        {
            for(int j = col - 1; j > 0; j--) {
                if (checkCollision(i, j))
                {
                    break;
                }
                moves.add(new ChessMove(this.position, new ChessPosition(i, j), null));
            }
        }
        for(int i = row + 1; i < 7; i++)
        {
            for(int j = col - 1; j > 0; j--) {
                if (checkCollision(i, j))
                {
                    break;
                }
                moves.add(new ChessMove(this.position, new ChessPosition(i, j), null));
            }
        }
        for(int i = row - 1; i > 0; i--)
        {
            for(int j = col + 1; j < 7; j++) {
                if (checkCollision(i, j))
                {
                    break;
                }
                moves.add(new ChessMove(this.position, new ChessPosition(i, j), null));
            }
        }
    }

    public boolean checkCollision(int row, int col)
    {
        return this.board.getPiece(new ChessPosition(row, col)) != null;
    }

    public void addMovesUsingModifiers(int[][] modifiers)
    {
        int row = this.position.getRow();
        int col = this.position.getColumn();

        for (int[] m : modifiers) {
            int newRow = row + m[0];
            int newCol = col + m[1];

            //This allows captures of enemy pieces. My checkCollision above only checks for collision, preventing pieces from capturing...
            //I'll have to fix that.
            if (newRow > 0 && newRow < 7 && newCol > 0 && newCol < 7) {
                ChessPosition moveTo = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(moveTo);
                if (targetPiece == null || targetPiece.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(this.position, moveTo, null));
                }
            }
        }
    }
}

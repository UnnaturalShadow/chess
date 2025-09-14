package chess.moveCalculators;

import chess.*;

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

    public static MoveCalculator getMoveCalculator(ChessBoard board, ChessPosition position)
    {

        ChessPiece piece = board.getPiece(position);
        ChessPiece.PieceType type = piece.getPieceType();

        if (type == ChessPiece.PieceType.ROOK) {
            return new RookMoveCalculator(board, position);
        } else if (type == ChessPiece.PieceType.KNIGHT) {
            return new KnightMoveCalculator(board, position);
        } else if (type == ChessPiece.PieceType.BISHOP) {
            return new BishopMoveCalculator(board, position);
        } else if (type == ChessPiece.PieceType.QUEEN) {
            return new QueenMoveCalculator(board, position);
        } else if (type == ChessPiece.PieceType.KING) {
            return new KingMoveCalculator(board, position);
        } else if (type == ChessPiece.PieceType.PAWN) {
            //return new PawnMoveCalculator(board, position);
        }

        return null;
    }

    public Collection<ChessMove> returnMoves()
    {
        return moves;
    }

    public void straightMoves()
    {
        int row = position.getRow();
        int col = position.getColumn();

        for (int r = row + 1; r <= 8; r++)
        {
            if (checkCollision(r, col))
            {
                if (getColor(r, col) != piece.getTeamColor())
                {
                    moves.add(new ChessMove(position, new ChessPosition(r, col), null));
                }
                break;
            }
            moves.add(new ChessMove(position, new ChessPosition(r, col), null));
        }

        for (int r = row - 1; r >= 1; r--)
        {
            if (checkCollision(r, col))
            {
                if (getColor(r, col) != piece.getTeamColor())
                {
                    moves.add(new ChessMove(position, new ChessPosition(r, col), null));
                }
                break;
            }
            moves.add(new ChessMove(position, new ChessPosition(r, col), null));
        }

        for (int c = col + 1; c <= 8; c++)
        {
            if (checkCollision(row, c))
            {
                if (getColor(row, c) != piece.getTeamColor())
                {
                    moves.add(new ChessMove(position, new ChessPosition(row, c), null));
                }
                break;
            }
            moves.add(new ChessMove(position, new ChessPosition(row, c), null));
        }

        for (int c = col - 1; c >= 1; c--)
        {
            if (checkCollision(row, c))
            {
                if (getColor(row, c) != piece.getTeamColor())
                {
                    moves.add(new ChessMove(position, new ChessPosition(row, c), null));
                }
                break;
            }
            moves.add(new ChessMove(position, new ChessPosition(row, c), null));
        }
    }



    public void diagonalMoves()
    {
        int row = position.getRow();
        int col = position.getColumn();

        for (int r = row + 1, c = col + 1; r <= 8 && c <= 8; r++, c++)
        {
            if (checkCollision(r, c))
            {
                if (getColor(r, c) != piece.getTeamColor())
                {
                    moves.add(new ChessMove(position, new ChessPosition(r, c), null));
                }
                break;
            }
            moves.add(new ChessMove(position, new ChessPosition(r, c), null));
        }

        for (int r = row + 1, c = col - 1; r <= 8 && c >= 1; r++, c--)
        {
            if (checkCollision(r, c))
            {
                if (getColor(r, c) != piece.getTeamColor())
                {
                    moves.add(new ChessMove(position, new ChessPosition(r, c), null));
                }
                break;
            }
            moves.add(new ChessMove(position, new ChessPosition(r, c), null));
        }

        for (int r = row - 1, c = col + 1; r >= 1 && c <= 8; r--, c++)
        {
            if (checkCollision(r, c))
            {
                if (getColor(r, c) != piece.getTeamColor())
                {
                    moves.add(new ChessMove(position, new ChessPosition(r, c), null));
                }
                break;
            }
            moves.add(new ChessMove(position, new ChessPosition(r, c), null));
        }

        for (int r = row - 1, c = col - 1; r >= 1 && c >= 1; r--, c--)
        {
            if (checkCollision(r, c))
            {
                if (getColor(r, c) != piece.getTeamColor())
                {
                    moves.add(new ChessMove(position, new ChessPosition(r, c), null));
                }
                break;
            }
            moves.add(new ChessMove(position, new ChessPosition(r, c), null));
        }
    }


    public boolean checkCollision(int row, int col)
    {
        return this.board.getPiece(new ChessPosition(row, col)) != null;
    }

    public ChessGame.TeamColor getColor(int row, int col)
    {
        return this.board.getPiece(new ChessPosition(row, col)).getTeamColor();
    }

    public void addMovesUsingModifiers(int[][] modifiers)
    {
        int row = this.position.getRow();
        int col = this.position.getColumn();

        for (int[] m : modifiers) {
            int newRow = row + m[0];
            int newCol = col + m[1];

            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8)
            {
                ChessPosition moveTo = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(moveTo);

                if (targetPiece == null || targetPiece.getTeamColor() != piece.getTeamColor())
                {
                    moves.add(new ChessMove(position, moveTo, null));
                }
            }
        }
    }
}

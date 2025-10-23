package chess.movecalculators;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The base class that manages the other move calculators and holds
 * their main methods.
 */
public class MoveCalculator
{
    ChessPosition position;
    ChessBoard board;
    ChessPiece piece;
    ArrayList<ChessMove> moves = new ArrayList<>();

    MoveCalculator(ChessBoard board, ChessPosition position)
    {
        this.position = position;
        this.board = board;
        this.piece = board.getPiece(position);
    }

    public static MoveCalculator getMoveCalculator(ChessBoard board, ChessPosition position)
    {
        ChessPiece piece = board.getPiece(position);
        ChessPiece.PieceType type = piece.getPieceType();

        if (type == ChessPiece.PieceType.ROOK)
        {
            return new RookMoveCalculator(board, position);
        }
        else if (type == ChessPiece.PieceType.KNIGHT)
        {
            return new KnightMoveCalculator(board, position);
        }
        else if (type == ChessPiece.PieceType.BISHOP)
        {
            return new BishopMoveCalculator(board, position);
        }
        else if (type == ChessPiece.PieceType.QUEEN)
        {
            return new QueenMoveCalculator(board, position);
        }
        else if (type == ChessPiece.PieceType.KING)
        {
            return new KingMoveCalculator(board, position);
        }
        else if (type == ChessPiece.PieceType.PAWN)
        {
            return new PawnMoveCalculator(board, position);
        }

        return null;
    }

    public Collection<ChessMove> returnMoves()
    {
        return moves;
    }

    // Consolidates repeated collision logic
    private boolean handleCollisionOrAdd(int r, int c)
    {
        if (!inBounds(r, c))
        {
            return true;
        }

        ChessPiece target = board.getPiece(new ChessPosition(r, c));
        if (target != null)
        {
            if (target.getTeamColor() != piece.getTeamColor())
            {
                moves.add(new ChessMove(position, new ChessPosition(r, c), null));
            }
            return true; // Stop movement in this direction
        }

        moves.add(new ChessMove(position, new ChessPosition(r, c), null));
        return false; // Can continue
    }

    public void straightMoves()
    {
        int row = position.getRow();
        int col = position.getColumn();

        for (int r = row + 1; r <= 8; r++)
        {
            if (handleCollisionOrAdd(r, col))
            {
                break;
            }
        }

        for (int r = row - 1; r >= 1; r--)
        {
            if (handleCollisionOrAdd(r, col))
            {
                break;
            }
        }

        for (int c = col + 1; c <= 8; c++)
        {
            if (handleCollisionOrAdd(row, c))
            {
                break;
            }
        }

        for (int c = col - 1; c >= 1; c--)
        {
            if (handleCollisionOrAdd(row, c))
            {
                break;
            }
        }
    }

    public void diagonalMoves()
    {
        int row = position.getRow();
        int col = position.getColumn();

        for (int r = row + 1, c = col + 1; r <= 8 && c <= 8; r++, c++)
        {
            if (handleCollisionOrAdd(r, c))
            {
                break;
            }
        }

        for (int r = row + 1, c = col - 1; r <= 8 && c >= 1; r++, c--)
        {
            if (handleCollisionOrAdd(r, c))
            {
                break;
            }
        }

        for (int r = row - 1, c = col + 1; r >= 1 && c <= 8; r--, c++)
        {
            if (handleCollisionOrAdd(r, c))
            {
                break;
            }
        }

        for (int r = row - 1, c = col - 1; r >= 1 && c >= 1; r--, c--)
        {
            if (handleCollisionOrAdd(r, c))
            {
                break;
            }
        }
    }

    public void pawnMoves()
    {
        int r = position.getRow();
        int c = position.getColumn();
        ChessGame.TeamColor color = piece.getTeamColor();

        int dir = (color == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (color == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promoRow = (color == ChessGame.TeamColor.WHITE) ? 8 : 1;

        int forwardRow = r + dir;

        // one step forward
        if (inBounds(forwardRow, c))
        {
            if (board.getPiece(new ChessPosition(forwardRow, c)) == null)
            {
                addPawnPromotionMoves(forwardRow, c, promoRow);
            }
        }

        // two steps forward
        int doubleRow = r + 2 * dir;
        if (r == startRow)
        {
            if (board.getPiece(new ChessPosition(forwardRow, c)) == null)
            {
                if (board.getPiece(new ChessPosition(doubleRow, c)) == null)
                {
                    moves.add(new ChessMove(position, new ChessPosition(doubleRow, c), null));
                }
            }
        }

        // diagonal captures
        for (int dc : new int[]{-1, 1})
        {
            int captureCol = c + dc;
            if (inBounds(forwardRow, captureCol))
            {
                if (isEnemyPiece(forwardRow, captureCol, piece))
                {
                    addPawnPromotionMoves(forwardRow, captureCol, promoRow);
                }
            }
        }
    }

    private void addPawnPromotionMoves(int row, int col, int promoRow)
    {
        if (row == promoRow)
        {
            for (ChessPiece.PieceType promo : List.of(
                    ChessPiece.PieceType.QUEEN,
                    ChessPiece.PieceType.ROOK,
                    ChessPiece.PieceType.BISHOP,
                    ChessPiece.PieceType.KNIGHT))
            {
                moves.add(new ChessMove(position, new ChessPosition(row, col), promo));
            }
        }
        else
        {
            moves.add(new ChessMove(position, new ChessPosition(row, col), null));
        }
    }

    public void addMovesUsingModifiers(int[][] modifiers)
    {
        int row = this.position.getRow();
        int col = this.position.getColumn();

        for (int[] m : modifiers)
        {
            int newRow = row + m[0];
            int newCol = col + m[1];

            if (inBounds(newRow, newCol))
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

    private boolean inBounds(int row, int col)
    {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    private boolean isEnemyPiece(int row, int col, ChessPiece piece)
    {
        ChessPiece target = board.getPiece(new ChessPosition(row, col));
        return target != null && target.getTeamColor() != piece.getTeamColor();
    }
}

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

    /**
     * Constructor for the class, requires an existing ChessBoard
     * and a position on that board to calculate moves for
     * @param board The board that moves are calculated for
     * @param position The position on that board that moves are calculated from
     */
    MoveCalculator(ChessBoard board, ChessPosition position)
    {
        this.position = position;
        this.board = board;
        this.piece = board.getPiece(position);
    }

    /**
     * Based on the given board and position the piece type is calculated
     * and a MoveCalculator of the correct subclass is created to calculate
     * the moves specific to that piece.
     * @param board The board that moves are calculated for
     * @param position The position on that board that moves are calculated from
     * @return a subclass of MoveCalculator specified to the given piece.
     */
    public static MoveCalculator getMoveCalculator(ChessBoard board, ChessPosition position)
    {
        ChessPiece piece = board.getPiece(position);
        ChessPiece.PieceType type = piece.getPieceType();

        if (type == ChessPiece.PieceType.ROOK) return new RookMoveCalculator(board, position);
        else if (type == ChessPiece.PieceType.KNIGHT) return new KnightMoveCalculator(board, position);
        else if (type == ChessPiece.PieceType.BISHOP) return new BishopMoveCalculator(board, position);
        else if (type == ChessPiece.PieceType.QUEEN) return new QueenMoveCalculator(board, position);
        else if (type == ChessPiece.PieceType.KING) return new KingMoveCalculator(board, position);
        else if (type == ChessPiece.PieceType.PAWN) return new PawnMoveCalculator(board, position);

        return null;
    }

    /**
     * Returns the moves array containing the list of valid moves
     * @return the ArrayList of ChessMoves moves
     */
    public Collection<ChessMove> returnMoves()
    {
        return moves;
    }

    // Helper method to handle movement in a single direction
    private boolean handleCollisionOrAdd(int r, int c)
    {
        if (!inBounds(r, c)) return true;

        ChessPiece target = board.getPiece(new ChessPosition(r, c));
        if (target != null)
        {
            if (target.getTeamColor() != piece.getTeamColor())
            {
                moves.add(new ChessMove(position, new ChessPosition(r, c), null));
            }
            return true; // Stop further movement in this direction
        }
        else
        {
            moves.add(new ChessMove(position, new ChessPosition(r, c), null));
            return false; // Can continue
        }
    }

    public void straightMoves()
    {
        int row = position.getRow();
        int col = position.getColumn();

        // Up
        for (int r = row + 1; r <= 8; r++) if (handleCollisionOrAdd(r, col)) break;
        // Down
        for (int r = row - 1; r >= 1; r--) if (handleCollisionOrAdd(r, col)) break;
        // Right
        for (int c = col + 1; c <= 8; c++) if (handleCollisionOrAdd(row, c)) break;
        // Left
        for (int c = col - 1; c >= 1; c--) if (handleCollisionOrAdd(row, c)) break;
    }

    public void diagonalMoves()
    {
        int row = position.getRow();
        int col = position.getColumn();

        // Top-right
        for (int r = row + 1, c = col + 1; r <= 8 && c <= 8; r++, c++) if (handleCollisionOrAdd(r, c)) break;
        // Top-left
        for (int r = row + 1, c = col - 1; r <= 8 && c >= 1; r++, c--) if (handleCollisionOrAdd(r, c)) break;
        // Bottom-right
        for (int r = row - 1, c = col + 1; r >= 1 && c <= 8; r--, c++) if (handleCollisionOrAdd(r, c)) break;
        // Bottom-left
        for (int r = row - 1, c = col - 1; r >= 1 && c >= 1; r--, c--) if (handleCollisionOrAdd(r, c)) break;
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
        if (inBounds(forwardRow, c) && board.getPiece(new ChessPosition(forwardRow, c)) == null)
        {
            addPawnPromotionMoves(forwardRow, c, promoRow);
        }

        // two steps forward
        int doubleRow = r + 2 * dir;
        if (r == startRow && board.getPiece(new ChessPosition(forwardRow, c)) == null &&
                board.getPiece(new ChessPosition(doubleRow, c)) == null)
        {
            moves.add(new ChessMove(position, new ChessPosition(doubleRow, c), null));
        }

        // diagonal captures
        for (int dc : new int[]{-1, 1})
        {
            int captureCol = c + dc;
            if (inBounds(forwardRow, captureCol) && isEnemyPiece(forwardRow, captureCol, piece))
            {
                addPawnPromotionMoves(forwardRow, captureCol, promoRow);
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

    // Utility methods
    public boolean checkCollision(int row, int col)
    {
        return this.board.getPiece(new ChessPosition(row, col)) != null;
    }

    public ChessGame.TeamColor getColor(int row, int col)
    {
        return this.board.getPiece(new ChessPosition(row, col)).getTeamColor();
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

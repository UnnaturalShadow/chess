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
    MoveCalculator (ChessBoard board, ChessPosition position)
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
            return new PawnMoveCalculator(board, position);
        }

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

    private boolean inBounds(int row, int col)
    {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    private boolean isEnemyPiece(int row, int col, ChessPiece piece)
    {
        ChessPiece target = board.getPiece(new ChessPosition(row, col));
        return target != null && target.getTeamColor() != piece.getTeamColor();
    }

    public void pawnMoves()
    {
        int r = position.getRow();
        int c = position.getColumn();
        ChessGame.TeamColor color = piece.getTeamColor();

        int dir = (color == ChessGame.TeamColor.WHITE) ? 1 : -1; // white goes up, black goes down
        int startRow = (color == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promoRow = (color == ChessGame.TeamColor.WHITE) ? 8 : 1;

        // one step forward
        int forwardRow = r + dir;
        if (inBounds(forwardRow, c) && board.getPiece(new ChessPosition(forwardRow, c)) == null)
        {
            if (forwardRow == promoRow)
            {
                for (ChessPiece.PieceType promo : List.of(
                        ChessPiece.PieceType.QUEEN,
                        ChessPiece.PieceType.ROOK,
                        ChessPiece.PieceType.BISHOP,
                        ChessPiece.PieceType.KNIGHT))
                {
                    moves.add(new ChessMove(position, new ChessPosition(forwardRow, c), promo));
                }
            }
            else
            {
                moves.add(new ChessMove(position, new ChessPosition(forwardRow, c), null));
            }
        }

        // two steps forward (only from start row, and only if path is clear)
        int doubleRow = r + 2 * dir;
        if (r == startRow &&
                board.getPiece(new ChessPosition(forwardRow, c)) == null &&
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
                if (forwardRow == promoRow)
                {
                    for (ChessPiece.PieceType promo : List.of(
                            ChessPiece.PieceType.QUEEN,
                            ChessPiece.PieceType.ROOK,
                            ChessPiece.PieceType.BISHOP,
                            ChessPiece.PieceType.KNIGHT))
                    {
                        moves.add(new ChessMove(position, new ChessPosition(forwardRow, captureCol), promo));
                    }
                }
                else
                {
                    moves.add(new ChessMove(position, new ChessPosition(forwardRow, captureCol), null));
                }
            }
        }
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

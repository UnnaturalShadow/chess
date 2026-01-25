package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame
{

    private TeamColor turn;
    private ChessBoard table;
    public ChessGame()
    {
        turn = TeamColor.WHITE;
        table = new ChessBoard();
        table.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn()
    {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team)
    {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */

    private void nextTurn()
    {
        if(turn == TeamColor.WHITE)
        {
            turn = TeamColor.BLACK;
        }
        else
        {
            turn = TeamColor.WHITE;
        }
    }

    public enum TeamColor
    {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition)
    {
        ChessPiece piece = table.getPiece(startPosition);
        if (piece == null)
        {
            return null;
        }

        Collection<ChessMove> moves = piece.pieceMoves(table, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<>();

        for (ChessMove move : moves)
        {
            if (isLegal(move))
            {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }

    public boolean isLegal(ChessMove move)
    {
        ChessPiece moving = table.getPiece(move.getStartPosition());
        ChessPiece captured = table.getPiece(move.getEndPosition());
        table.addPiece(move.getStartPosition(), null);
        table.addPiece(move.getEndPosition(), moving);
        boolean legal = !isInCheck(moving.getTeamColor());
        table.addPiece(move.getStartPosition(), moving);
        table.addPiece(move.getEndPosition(), captured);
        return legal;

    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException
    {
        if(table.getPiece(move.getStartPosition()) == null)
        {
            throw new InvalidMoveException("No piece to move at that position.");
        }
        if(table.getPiece(move.getStartPosition()).getTeamColor() != turn)
        {
            throw new InvalidMoveException("It's not your turn silly!");
        }
        Collection<ChessMove> goodMoves = validMoves(move.getStartPosition());
        if(goodMoves.contains(move))
        {
            ChessPiece moving = table.getPiece(move.getStartPosition());
            table.addPiece(move.getStartPosition(), null);
            if(move.getPromotionPiece() != null)
            {
                table.addPiece(move.getEndPosition(), new ChessPiece(turn, move.getPromotionPiece()));
            }
            else
            {
                table.addPiece(move.getEndPosition(), moving);
            }
            nextTurn();
            return;
        }
        throw new InvalidMoveException("That is not a legal move");

    }

    public ChessPosition getKingPosition(TeamColor teamColor)
    {
        for (int row = 1; row <= 8; row++)
        {
            for (int col = 1; col <= 8; col++)
            {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = table.getPiece(pos);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor)
                {
                    return pos;
                }
            }
        }
        return null;
    }

    private boolean isThreat(int row, int col, TeamColor teamColor, ChessPosition kingPosition)
    {
        ChessPiece currentPiece = table.getPiece(new ChessPosition(row, col));
        if (currentPiece == null || currentPiece.getTeamColor() == teamColor)
        {
            return false;
        }

        for (ChessMove move : currentPiece.pieceMoves(table, new ChessPosition(row, col)))
        {
            if(currentPiece.getPieceType() == ChessPiece.PieceType.PAWN)
            {
                if(move.getEndPosition().getColumn() != col && move.getEndPosition().equals(kingPosition))
                {
                    return true;
                }
            }
            else if(move.getEndPosition().equals(kingPosition))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor)
    {
        ChessPosition kingPos = getKingPosition(teamColor);
        for(int i = 1; i <= 8; i++)
        {
            for(int j = 1; j <= 8; j++)
            {
                if(isThreat(i, j, teamColor, kingPos))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor)
    {
        if(!isInCheck(teamColor))
        {
            return false;
        }
        //need to check if there are ANY valid moves, not just valid king moves
        for(int i = 1; i <= 8; i++)
        {
            for(int j = 1; j <= 8; j++)
            {
                ChessPosition curPos = new ChessPosition(i, j);
                ChessPiece piece = table.getPiece(curPos);
                if(piece != null && piece.getTeamColor() == teamColor)
                {
                    Collection<ChessMove> canMove = validMoves(curPos);
                    if(!canMove.isEmpty())
                    {
                        return false;
                    }
                }
            }
        }
        return true;

    }


    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor)
    {
        if(isInCheck(teamColor))
        {
            return false;
        }
        //need to check if there are ANY valid moves, not just valid king moves
        for(int i = 1; i <= 8; i++)
        {
            for(int j = 1; j <= 8; j++)
            {
                ChessPosition curPos = new ChessPosition(i, j);
                ChessPiece piece = table.getPiece(curPos);
                if(piece != null && piece.getTeamColor() == teamColor)
                {
                    Collection<ChessMove> canMove = validMoves(curPos);
                    if(!canMove.isEmpty())
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board)
    {
        table = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard()
    {
        return table;
    }

    @Override
    public int hashCode()
    {
        return 31 * table.hashCode() + turn.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ChessGame that = (ChessGame)o;
        return this.table.equals(that.getBoard()) && turn == that.getTeamTurn();
    }
}

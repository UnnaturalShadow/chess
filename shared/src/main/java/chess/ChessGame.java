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
        ChessPiece curr = table.getPiece(startPosition);
        if(curr != null)
        {
            Collection<ChessMove> technicallyPossible =  curr.pieceMoves(table, startPosition);

            for(ChessMove move : technicallyPossible)
            {
                simulate(move);
            }
        }
        return null;
    }

    public void setBoard(ChessBoard template, ChessBoard result)
    {
        for (int i = 1; i <= 8; i++)
        {
            for (int j = 1; j <= 8; j++)
            {
                result.addPiece(new ChessPosition(i, j), template.getPiece(new ChessPosition(i, j)));
            }
        }
    }

    public ChessBoard simulate(ChessMove move)
    {
        ChessBoard current = new ChessBoard();
        setBoard(table, current);

        try
        {
            makeMove(move);
        }
        catch (InvalidMoveException e)
        {
            throw new RuntimeException(e);
        }

        return current;

    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException
    {
        if(!table.inBounds(move.getEndPosition())) {
            throw new InvalidMoveException("Piece would move out of bounds");
        }
            chess.ChessPiece moving = table.getPiece(move.getStartPosition());
            if(moving == null)
            {
                throw new InvalidMoveException("No piece at start position");
            }
            table.addPiece(move.getStartPosition(), null);
            table.addPiece(move.getEndPosition(), moving);
            nextTurn();
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

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor)
    {
//        throw new RuntimeException("Not implemented");
        ChessPosition kingPos = getKingPosition(teamColor);
        for(int i = 1; i <= 8; i++)
        {
            for(int j = 1; j <= 8; j++)
            {
                ChessPiece attacking = table.getPiece(new ChessPosition(i, j));
                if(attacking != null && attacking.getTeamColor() != teamColor)
                {
                    Collection<ChessMove> moves = attacking.pieceMoves(table, new ChessPosition(i, j));
                    for(ChessMove move : moves)
                    {
                        if(move.getEndPosition() == kingPos)
                        {
                            return true;
                        }
                    }
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
//        throw new RuntimeException("Not implemented");
        ChessPosition kingPos = getKingPosition(teamColor);
        if(isInCheck(teamColor))
        {
            ChessPiece king = table.getPiece(kingPos);
            Collection<ChessMove> moves = king.pieceMoves(table, kingPos);
            for(ChessMove move : moves)
            {
                ChessBoard prior = simulate(move);
                if(!isInCheck(teamColor))
                {
                    return false;
                }
                setBoard(prior, table);
            }
            return true;
        }
        return false;
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
        ChessPosition kingPos = getKingPosition(teamColor);
        if(!isInCheck(teamColor))
        {
            ChessPiece king = table.getPiece(kingPos);
            Collection<ChessMove> moves = king.pieceMoves(table, kingPos);
            for(ChessMove move : moves)
            {
                ChessBoard prior = simulate(move);
                if(!isInCheck(teamColor))
                {
                    return false;
                }
                setBoard(prior, table);
            }
            return true;
        }
        return false;
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

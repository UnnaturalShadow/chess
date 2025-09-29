package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame
{


    private ChessBoard board;
    private TeamColor turn;

    public ChessGame()
    {

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
        return board.getPiece(startPosition).pieceMoves(board, startPosition);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException
    {
       if(canMakeMove(move))
       {
           ChessPosition startPosition = move.getStartPosition();
           ChessPiece toMove = board.getPiece(startPosition);
           board.removePiece(startPosition);
           board.addPiece(move.getEndPosition(), toMove);
           return;
       }
       throw new InvalidMoveException("Not a legal move");
    }

    public boolean canMakeMove(ChessMove move)
    {
        ChessPosition startPosition = move.getStartPosition();
        ChessPiece toMove = board.getPiece(startPosition);
        Collection<ChessMove> moves = validMoves(startPosition);
        for(ChessMove validMove : moves)
        {
            if(move.equals(validMove))
            {
                ChessBoard current = board;
                board.removePiece(startPosition);
                board.addPiece(move.getEndPosition(), toMove);
                if(!isInCheck(turn))
                {
                    board = current;
                    return true;
                }
                else
                {
                    board = current;
                    return false;
                }
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
        ChessPosition kingPosition = getKingPosition(teamColor);
        for(int row = 1; row <= 8; row++)
        {
            for(int col = 1; col <= 8; col++)
            {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = board.getPiece(currentPosition);
                if(currentPiece != null)
                {
                    if(currentPiece.getTeamColor() != teamColor)
                    {
                        Collection<ChessMove> moves = currentPiece.pieceMoves(board, currentPosition);
                        for(ChessMove move : moves)
                        {
                            if(move.getEndPosition() == kingPosition)
                            {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public ChessPosition getKingPosition(TeamColor teamColor)
    {
        for(int row = 1; row <= 8; row++)
        {
            for (int col = 1; col <= 8; col++)
            {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = board.getPiece(currentPosition);
                if (currentPiece != null && currentPiece.getPieceType() == ChessPiece.PieceType.KING && currentPiece.getTeamColor() == teamColor)
                {
                    return currentPosition;
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor)
    {
        if(isInCheck(teamColor))
        {
            for(int row = 1; row <= 8; row++)
            {
                for (int col = 1; col <= 8; col++)
                {
                    Collection<ChessMove> moves = validMoves(new ChessPosition(row, col));
                    for(ChessMove move : moves)
                    {
                        if(canMakeMove(move))
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
        if(!isInCheck(teamColor))
        {
            for(int row = 1; row <= 8; row++)
            {
                for (int col = 1; col <= 8; col++)
                {
                    Collection<ChessMove> moves = validMoves(new ChessPosition(row, col));
                    for(ChessMove move : moves)
                    {
                        if(canMakeMove(move))
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
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard()
    {
        return this.board;
    }
}

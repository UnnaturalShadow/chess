package chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

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
        board = new ChessBoard();
        board.resetBoard();
        turn = TeamColor.WHITE;
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
        if(board.getPiece(startPosition) == null)
        {
            return null;
        }
        Collection<ChessMove> moves = board.getPiece(startPosition).pieceMoves(board, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<ChessMove>();
        for(ChessMove move : moves)
        {
            boolean legal = true;
            ChessPiece toMove = board.getPiece(move.getStartPosition());
            board.removePiece(move.getStartPosition());
            ChessPiece killed = board.getPiece(move.getEndPosition());
            if(move.getPromotionPiece() != null)
            {
                board.addPiece(move.getEndPosition(), new ChessPiece(turn, move.getPromotionPiece()));
            }
            else
            {
                board.addPiece(move.getEndPosition(), toMove);
            }
            if (isInCheck(toMove.getTeamColor()))
            {
                legal = false;
            }
            board.removePiece(move.getEndPosition());
            board.addPiece(move.getEndPosition(), killed);
            board.addPiece(move.getStartPosition(), toMove);
            if(legal)
            {
                legalMoves.add(move);
//                System.out.println(move);
            }
        }

        //castling moves logic. For the sake of simplicity castling is considered a king move only
        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException
    {
        ChessPosition startPosition = move.getStartPosition();
        ChessPiece toMove = board.getPiece(startPosition);
       if(canMakeMove(move))
       {
           if(toMove.getTeamColor() != turn)
           {
               throw new InvalidMoveException("Not a legal move");
           }
           board.removePiece(startPosition);
           if(move.getPromotionPiece() != null)
           {
               board.addPiece(move.getEndPosition(), new ChessPiece(turn, move.getPromotionPiece()));
           }
           else
           {
               board.addPiece(move.getEndPosition(), toMove);
           }

           changeTurn();
           return;
       }
       else if(canCastleLeft(turn) && getKingPosition(turn).equals(move.getStartPosition()) && move.getEndPosition().equals(new ChessPosition(1,3)))
       {
           board.removePiece(startPosition);
           board.addPiece(move.getEndPosition(), toMove);
           board.removePiece(new ChessPosition(1,1));
           board.addPiece(new ChessPosition(1, 4), new ChessPiece(turn, ChessPiece.PieceType.ROOK));
       }
       else if(canCastleRight(turn) && getKingPosition(turn).equals(move.getStartPosition()) && move.getEndPosition().equals(new ChessPosition(1,7)))
       {
           board.removePiece(startPosition);
           board.addPiece(move.getEndPosition(), toMove);
           board.removePiece(new ChessPosition(1,8));
           board.addPiece(new ChessPosition(1, 6), new ChessPiece(turn, ChessPiece.PieceType.ROOK));
       }
       throw new InvalidMoveException("Not a legal move");
    }

    public void changeTurn()
    {
        turn = (turn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public boolean canMakeMove(ChessMove move)
    {
        ChessPosition startPosition = move.getStartPosition();
        ChessPiece toMove = board.getPiece(startPosition);
        if(toMove == null)
        {
            return false;
        }
        if(toMove.getTeamColor() != turn)
        {
            return false;
        }
        Collection<ChessMove> moves = board.getPiece(startPosition).pieceMoves(board, startPosition);
        for(ChessMove validMove : moves)
        {
            if(move.equals(validMove))
            {
                board.removePiece(move.getStartPosition());
                ChessPiece killed = board.getPiece(move.getEndPosition());
                if(move.getPromotionPiece() != null)
                {
                    board.addPiece(move.getEndPosition(), new ChessPiece(turn, move.getPromotionPiece()));
                }
                else
                {
                    board.addPiece(move.getEndPosition(), toMove);
                }
                if (isInCheck(turn))
                {
                    board.removePiece(move.getEndPosition());
                    board.addPiece(move.getEndPosition(), killed);
                    board.addPiece(move.getStartPosition(), toMove);
                    return false;
                }
                board.removePiece(move.getEndPosition());
                board.addPiece(move.getEndPosition(), killed);
                board.addPiece(move.getStartPosition(), toMove);
                return true;
            }
        }
        return false;
    }

    public boolean canCastleRight(TeamColor teamColor)
    {
        if (teamColor == TeamColor.WHITE)
        {
            if (getKingPosition(teamColor).equals(new ChessPosition(1, 5)))
            {
                if(!isInCheck(teamColor))
                {
                    //right side castle
                    if(board.getPiece(new ChessPosition(1,8)).getPieceType() == ChessPiece.PieceType.ROOK)
                    {
                        for(int i = 6; i <= 7; i++)
                        {
                            if(board.getPiece(new ChessPosition(1, i)) == null)
                            {
                                chess.ChessPosition ogPos = getKingPosition(teamColor);
                                board.addPiece(new ChessPosition(1, i), board.getPiece(ogPos));
                                if(isInCheck(teamColor))
                                {
                                    board.addPiece(ogPos, board.getPiece(new ChessPosition(1, i)));
                                    board.removePiece(new ChessPosition(1, i));
                                    return false;
                                }
                                board.addPiece(ogPos, board.getPiece(new ChessPosition(1, i)));
                                board.removePiece(new ChessPosition(1, i));
                            }
                            else
                            {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean canCastleLeft(TeamColor teamColor)
    {
        if (teamColor == TeamColor.WHITE)
        {
            if (getKingPosition(teamColor).equals(new ChessPosition(1, 5)))
            {
                if(!isInCheck(teamColor))
                {
                    //left side castle
                    if(board.getPiece(new ChessPosition(1,1)).getPieceType() == ChessPiece.PieceType.ROOK)
                    {
                        for(int i = 2; i <= 4; i++)
                        {
                            if(board.getPiece(new ChessPosition(1, i)) == null)
                            {
                                chess.ChessPosition ogPos = getKingPosition(teamColor);
                                board.addPiece(new ChessPosition(1, i), board.getPiece(ogPos));
                                if(isInCheck(teamColor))
                                {
                                    board.addPiece(ogPos, board.getPiece(new ChessPosition(1, i)));
                                    board.removePiece(new ChessPosition(1, i));
                                    return false;
                                }
                                board.addPiece(ogPos, board.getPiece(new ChessPosition(1, i)));
                                board.removePiece(new ChessPosition(1, i));
                            }
                            else
                            {
                                return false;
                            }
                        }
                        return true;
                    }
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
                            if(move.getEndPosition().equals(kingPosition))
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
                    if(board.getPiece(new ChessPosition(row, col)) != null)
                    {
                        Collection<ChessMove> moves = validMoves(new ChessPosition(row, col));
                        for (ChessMove move : moves)
                        {
                            if (canMakeMove(move))
                            {
                                return false;
                            }
                        }
                    }
                }
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
        if(isInCheck((teamColor)))
        {
            return false;
        }
        if(!isInCheck(teamColor))
        {
            for(int row = 1; row <= 8; row++)
            {
                for (int col = 1; col <= 8; col++)
                {
                    if(board.getPiece(new ChessPosition(row, col)) != null)
                    {
                        if(board.getPiece(new ChessPosition(row, col)).getTeamColor() == teamColor)
                        {
                            Collection<ChessMove> moves = validMoves(new ChessPosition(row, col));
                            if (!moves.isEmpty())
                            {
                                return false;
                            }
                        }
                    }
                }
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

    @Override
    public int hashCode()
    {
        return 31 * board.hashCode() + turn.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        ChessGame that = (ChessGame) o;
        return this.board.equals(that.getBoard()) && turn == that.getTeamTurn();
    }
}

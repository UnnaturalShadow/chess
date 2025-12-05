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
    private ChessBoard board;
    private TeamColor turn;
    public boolean isActive = true;

    public ChessGame()
    {
        board = new ChessBoard();
        board.resetBoard();
        turn = TeamColor.WHITE;
    }

    public TeamColor getTeamTurn()
    {
        return turn;
    }

    public void setTeamTurn(TeamColor team)
    {
        turn = team;
    }

    public enum TeamColor
    {
        WHITE,
        BLACK
    }

    public ArrayList<ChessPosition> getPiecePositions(TeamColor teamColor)
    {
        ArrayList<ChessPosition> positions = new ArrayList<>();
        for (int i = 0; i < 8; i++)
        {
            for (int j = 0; j < 8; j++)
            {
                ChessPosition position = new ChessPosition(i+1, j+1);
                ChessPiece piece = this.board.getPiece(position);
                if (teamColor != null)
                {
                    if (piece != null && piece.getTeamColor() == teamColor)
                    {
                        positions.add(position);
                    }
                }
                else if (piece != null)
                {
                    positions.add(position);
                }

            }
        }

        return positions;
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition)
    {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null)
        {
            return null;
        }

        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<>();

        for (ChessMove move : moves)
        {
            if (isLegalMoveSimulation(move))
            {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }

    private boolean isLegalMoveSimulation(ChessMove move)
    {
        ChessPiece toMove = board.getPiece(move.getStartPosition());
        ChessPiece killed = board.getPiece(move.getEndPosition());

        board.removePiece(move.getStartPosition());
        applyMovePiece(move, toMove);

        boolean legal = !isInCheck(toMove.getTeamColor());

        board.removePiece(move.getEndPosition());
        restorePiece(move, toMove, killed);

        return legal;
    }

    private void applyMovePiece(ChessMove move, ChessPiece toMove)
    {
        if (move.getPromotionPiece() != null)
        {
            board.addPiece(move.getEndPosition(), new ChessPiece(turn, move.getPromotionPiece()));
        }
        else
        {
            board.addPiece(move.getEndPosition(), toMove);
        }
    }

    private void restorePiece(ChessMove move, ChessPiece toMove, ChessPiece killed)
    {
        board.addPiece(move.getEndPosition(), killed);
        board.addPiece(move.getStartPosition(), toMove);
    }

    public void makeMove(ChessMove move) throws InvalidMoveException
    {
        ChessPiece toMove = board.getPiece(move.getStartPosition());
        if (toMove == null || toMove.getTeamColor() != turn || !canMakeMove(move))
        {
            throw new InvalidMoveException("Error: Not a valid move");
        }

        board.removePiece(move.getStartPosition());
        applyMovePiece(move, toMove);
        changeTurn();

        if (isInStalemate(toMove.getTeamColor()) || isInCheckmate(toMove.getTeamColor()))
        {
            isActive = false;
        }
    }

    public void changeTurn()
    {
        turn = (turn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public boolean canMakeMove(ChessMove move)
    {
        ChessPiece toMove = board.getPiece(move.getStartPosition());
        if (toMove == null || toMove.getTeamColor() != turn)
        {
            return false;
        }

        for (ChessMove validMove : board.getPiece(move.getStartPosition()).pieceMoves(board, move.getStartPosition()))
        {
            if (!move.equals(validMove))
            {
                continue;
            }

            ChessPiece killed = board.getPiece(move.getEndPosition());
            board.removePiece(move.getStartPosition());
            applyMovePiece(move, toMove);

            boolean legal = !isInCheck(turn);

            board.removePiece(move.getEndPosition());
            restorePiece(move, toMove, killed);

            if (legal)
            {
                return true;
            }
        }

        return false;
    }

    public boolean isInCheck(TeamColor teamColor)
    {
        ChessPosition kingPosition = getKingPosition(teamColor);
        for (int row = 1; row <= 8; row++)
        {
            for (int col = 1; col <= 8; col++)
            {
                if (isEnemyThreatAt(row, col, teamColor, kingPosition))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isEnemyThreatAt(int row, int col, TeamColor teamColor, ChessPosition kingPosition)
    {
        ChessPiece currentPiece = board.getPiece(new ChessPosition(row, col));
        if (currentPiece == null || currentPiece.getTeamColor() == teamColor)
        {
            return false;
        }

        for (ChessMove move : currentPiece.pieceMoves(board, new ChessPosition(row, col)))
        {
            if (move.getEndPosition().equals(kingPosition))
            {
                return true;
            }
        }

        return false;
    }

    public ChessPosition getKingPosition(TeamColor teamColor)
    {
        for (int row = 1; row <= 8; row++)
        {
            for (int col = 1; col <= 8; col++)
            {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor)
                {
                    return pos;
                }
            }
        }
        return null;
    }

    public boolean isInCheckmate(TeamColor teamColor)
    {
        if (!isInCheck(teamColor))
        {
            return false;
        }

        for (int row = 1; row <= 8; row++)
        {
            for (int col = 1; col <= 8; col++)
            {
                if (canPieceEscape(row, col, teamColor))
                {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean canPieceEscape(int row, int col, TeamColor teamColor)
    {
        ChessPosition pos = new ChessPosition(row, col);
        ChessPiece piece = board.getPiece(pos);

        if (piece == null || piece.getTeamColor() != teamColor)
        {
            return false;
        }

        Collection<ChessMove> moves = validMoves(pos);
        if (moves == null)
        {
            return false;
        }

        for (ChessMove move : moves)
        {
            if (canMakeMove(move))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isInStalemate(TeamColor teamColor)
    {
        if (isInCheck(teamColor))
        {
            return false;
        }

        for (int row = 1; row <= 8; row++)
        {
            for (int col = 1; col <= 8; col++)
            {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor)
                {
                    if (!validMoves(pos).isEmpty())
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void setBoard(ChessBoard board)
    {
        this.board = board;
    }

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
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ChessGame that = (ChessGame)o;
        return this.board.equals(that.getBoard()) && turn == that.getTeamTurn();
    }
}

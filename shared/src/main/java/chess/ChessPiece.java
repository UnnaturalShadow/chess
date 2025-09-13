package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece
{

    private ChessGame.TeamColor color;
    private ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type)
    {
        this.color = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType
    {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor()
    {
        return this.color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType()
    {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition)
    {
        Collection<ChessMove> moves = new ArrayList<ChessMove>();
        if(this.type == ChessPiece.PieceType.KING) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();
            if(col > 0) {
                ChessPosition moveTo = new ChessPosition(row, col - 1);
                if(board.getPiece(moveTo) == null || board.getPiece(moveTo).getTeamColor() != this.color) {
                    ChessMove move = new ChessMove(myPosition, moveTo, null);
                    moves.add(move);
                }
                if(row > 0) {
                    moveTo = new ChessPosition(row - 1, col - 1);
                    if(board.getPiece(moveTo) == null || board.getPiece(moveTo).getTeamColor() != this.color) {
                        ChessMove move = new ChessMove(myPosition, moveTo, null);
                        moves.add(move);
                    }
                }
                if(row < 7) {
                    moveTo = new ChessPosition(row + 1, col - 1);
                    if(board.getPiece(moveTo) == null || board.getPiece(moveTo).getTeamColor() != this.color) {
                        ChessMove move = new ChessMove(myPosition, moveTo, null);
                        moves.add(move);
                    }
                }
            }
            if(col < 7) {
                ChessPosition moveTo = new ChessPosition(row, col + 1);
                if(board.getPiece(moveTo) == null || board.getPiece(moveTo).getTeamColor() != this.color) {
                    ChessMove move = new ChessMove(myPosition, moveTo, null);
                    moves.add(move);
                }
                if(row > 0) {
                    moveTo = new ChessPosition(row - 1, col + 1);
                    if(board.getPiece(moveTo) == null || board.getPiece(moveTo).getTeamColor() != this.color) {
                        ChessMove move = new ChessMove(myPosition, moveTo, null);
                        moves.add(move);
                    }
                }
                if (row < 7) {
                    moveTo = new ChessPosition(row + 1, col + 1);
                    if(board.getPiece(moveTo) == null || board.getPiece(moveTo).getTeamColor() != this.color) {
                        ChessMove move = new ChessMove(myPosition, moveTo, null);
                        moves.add(move);
                    }
                }
            }
            if(row > 0)
            {
                ChessPosition moveTo = new ChessPosition(row - 1, col);
                if(board.getPiece(moveTo) == null || board.getPiece(moveTo).getTeamColor() != this.color) {
                    ChessMove move = new ChessMove(myPosition, moveTo, null);
                    moves.add(move);
                }
            }
            if(row < 7)
            {
                ChessPosition moveTo = new ChessPosition(row + 1, col);
                if(board.getPiece(moveTo) == null || board.getPiece(moveTo).getTeamColor() != this.color) {
                    ChessMove move = new ChessMove(myPosition, moveTo, null);
                    moves.add(move);
                }
            }
        }
        return moves;
    }
}

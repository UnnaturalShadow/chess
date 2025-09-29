package chess;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
import java.util.Objects;
public class ChessMove
{

    private ChessPosition start;
    private ChessPosition end;
    private ChessPiece.PieceType promotion;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece)
    {
        this.start = startPosition;
        this.end = endPosition;
        this.promotion = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition()
    {
        return this.start;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition()
    {
        return this.end;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece()
    {
        return this.promotion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStartPosition(), getEndPosition(), getPromotionPiece());
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
        ChessMove chessMove = (ChessMove) o;
        return Objects.equals(getStartPosition(), chessMove.getStartPosition()) &&
                Objects.equals(getEndPosition(), chessMove.getEndPosition()) &&
                getPromotionPiece() == chessMove.getPromotionPiece();
    }
}

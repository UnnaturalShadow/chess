package chess;

import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove
{
    private ChessPosition start;
    private ChessPosition end;
    private ChessPiece.PieceType promote;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece)
    {
        start = startPosition;
        end =  endPosition;
        promote = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition()
    {
        return start;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition()
    {
        return end;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece()
    {
        return promote;
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
        ChessMove chessMove = (ChessMove) o;
        return Objects.equals(getStartPosition(), chessMove.getStartPosition()) && Objects.equals(getEndPosition(),
                chessMove.getEndPosition()) && getPromotionPiece() == chessMove.getPromotionPiece();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getStartPosition(), getEndPosition(), getPromotionPiece());
    }

    @Override
    public String toString()
    {
        return "["+start.toString()+", "+end.toString()+"]";
    }

    public static ChessMove fromString(String moveStr) {
        if (moveStr == null || (moveStr.length() != 4 && moveStr.length() != 5)) {
            throw new IllegalArgumentException("Invalid move format: " + moveStr);
        }

        ChessPosition start = ChessPosition.fromAlgebraic(moveStr.substring(0, 2));
        ChessPosition end = ChessPosition.fromAlgebraic(moveStr.substring(2, 4));

        ChessPiece.PieceType promote = null;
        if (moveStr.length() == 5) {
            char promoChar = moveStr.charAt(4);
            switch (Character.toUpperCase(promoChar)) {
                case 'Q' -> promote = ChessPiece.PieceType.QUEEN;
                case 'R' -> promote = ChessPiece.PieceType.ROOK;
                case 'B' -> promote = ChessPiece.PieceType.BISHOP;
                case 'N' -> promote = ChessPiece.PieceType.KNIGHT;
                default -> throw new IllegalArgumentException("Invalid promotion piece: " + promoChar);
            }
        }

        return new ChessMove(start, end, promote);
    }
}


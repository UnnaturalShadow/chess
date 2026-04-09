package chess;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class ChessMove {

    @SerializedName(value = "start", alternate = {"startPosition"})
    private ChessPosition start;

    @SerializedName(value = "end", alternate = {"endPosition"})
    private ChessPosition end;

    @SerializedName(value = "promote", alternate = {"promotionPiece"})
    private ChessPiece.PieceType promote;

    public ChessMove(ChessPosition start, ChessPosition end,
                     ChessPiece.PieceType promote) {
        this.start = start;
        this.end = end;
        this.promote = promote;
    }

    public ChessPosition getStartPosition() {
        return start;
    }

    public ChessPosition getEndPosition() {
        return end;
    }

    public ChessPiece.PieceType getPromotionPiece() {
        return promote;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessMove other)) return false;

        return Objects.equals(start, other.start)
                && Objects.equals(end, other.end)
                && promote == other.promote;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, promote);
    }

    @Override
    public String toString() {
        return "[" + start + ", " + end + "]";
    }

    public static ChessMove fromString(String s) {
        if (s == null || (s.length() != 4 && s.length() != 5)) {
            throw new IllegalArgumentException("Bad move: " + s);
        }

        ChessPosition a = ChessPosition.fromAlgebraic(s.substring(0, 2));
        ChessPosition b = ChessPosition.fromAlgebraic(s.substring(2, 4));

        ChessPiece.PieceType promo = null;

        if (s.length() == 5) {
            promo = switch (Character.toUpperCase(s.charAt(4))) {
                case 'Q' -> ChessPiece.PieceType.QUEEN;
                case 'R' -> ChessPiece.PieceType.ROOK;
                case 'B' -> ChessPiece.PieceType.BISHOP;
                case 'N' -> ChessPiece.PieceType.KNIGHT;
                default -> throw new IllegalArgumentException("Bad promo");
            };
        }

        return new ChessMove(a, b, promo);
    }
}
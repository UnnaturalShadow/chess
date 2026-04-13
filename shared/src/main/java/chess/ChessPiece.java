package chess;

import chess.moves.Calculator;
import java.util.Collection;

public class ChessPiece {

    private final ChessGame.TeamColor color;
    private final PieceType type;

    public enum PieceType {
        KING, QUEEN, BISHOP, KNIGHT, ROOK, PAWN
    }

    public ChessPiece(ChessGame.TeamColor color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    public PieceType getPieceType() {
        return type;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition pos) {
        return new Calculator(board, pos).getMoves();
    }

    public String getLetter() {
        return switch (type) {
            case KING -> "K";
            case QUEEN -> "Q";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case ROOK -> "R";
            case PAWN -> "P";
        };
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessPiece other))
        {
            return false;
        }
        return color == other.color && type == other.type;
    }

    @Override
    public int hashCode() {
        return 31 * type.hashCode() + color.hashCode();
    }

    @Override
    public String toString() {
        return color + " " + type;
    }
}
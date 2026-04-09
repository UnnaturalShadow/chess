package chess;

import java.util.Arrays;
import java.util.Objects;

public class ChessBoard {

    private final ChessPiece[][] board;

    public ChessBoard() {
        this.board = new ChessPiece[8][8];
    }

    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow() - 1][position.getColumn() - 1];
    }

    public boolean inBounds(ChessPosition position) {
        int r = position.getRow();
        int c = position.getColumn();
        return r >= 1 && r <= 8 && c >= 1 && c <= 8;
    }

    public void resetBoard() {
        ChessPiece.PieceType[] backRow = {
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK
        };

        ChessGame.TeamColor team = ChessGame.TeamColor.WHITE;

        for (int cycle = 0; cycle < 2; cycle++) {
            int pawnRow = (team == ChessGame.TeamColor.WHITE) ? 2 : 7;
            int majorRow = (team == ChessGame.TeamColor.WHITE) ? 1 : 8;

            for (int col = 1; col <= 8; col++) {
                addPiece(new ChessPosition(pawnRow, col),
                        new ChessPiece(team, ChessPiece.PieceType.PAWN));
            }

            for (int col = 1; col <= 8; col++) {
                addPiece(new ChessPosition(majorRow, col),
                        new ChessPiece(team, backRow[col - 1]));
            }

            team = ChessGame.TeamColor.BLACK;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (ChessPiece[] row : board) {
            for (ChessPiece p : row) {
                sb.append(p == null ? "." : p.getLetter()).append(" ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessBoard other)) return false;
        return Objects.deepEquals(board, other.board);
    }
}
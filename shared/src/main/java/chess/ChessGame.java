package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ChessGame {

    private TeamColor turn;
    private ChessBoard table;

    public ChessGame() {
        this.turn = TeamColor.WHITE;
        this.table = new ChessBoard();
        table.resetBoard();
    }

    public enum TeamColor {
        WHITE, BLACK
    }

    public TeamColor getTeamTurn() {
        return turn;
    }

    public void setTeamTurn(TeamColor team) {
        this.turn = team;
    }

    private void switchTurn() {
        turn = (turn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = table.getPiece(startPosition);
        if (piece == null)
        {
            return null;
        }

        Collection<ChessMove> raw = piece.pieceMoves(table, startPosition);
        Collection<ChessMove> legal = new ArrayList<>();

        for (ChessMove m : raw) {
            if (isLegal(m)) {
                legal.add(m);
            }
        }
        return legal;
    }

    public boolean isLegal(ChessMove move) {
        ChessPiece moving = table.getPiece(move.getStartPosition());
        ChessPiece captured = table.getPiece(move.getEndPosition());

        table.addPiece(move.getStartPosition(), null);
        table.addPiece(move.getEndPosition(), moving);

        boolean ok = !isInCheck(moving.getTeamColor());

        table.addPiece(move.getStartPosition(), moving);
        table.addPiece(move.getEndPosition(), captured);

        return ok;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = table.getPiece(move.getStartPosition());

        if (piece == null)
        {
            throw new InvalidMoveException("No piece selected.");
        }
        if (piece.getTeamColor() != turn)
        {
            throw new InvalidMoveException("Wrong turn.");
        }

        if (!validMoves(move.getStartPosition()).contains(move)) {
            throw new InvalidMoveException("Illegal move.");
        }

        table.addPiece(move.getStartPosition(), null);

        if (move.getPromotionPiece() != null) {
            table.addPiece(move.getEndPosition(),
                    new ChessPiece(turn, move.getPromotionPiece()));
        } else {
            table.addPiece(move.getEndPosition(), piece);
        }

        switchTurn();
    }

    public ChessPosition getKingPosition(TeamColor color) {
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece p = table.getPiece(pos);

                if (p != null &&
                        p.getPieceType() == ChessPiece.PieceType.KING &&
                        p.getTeamColor() == color) {
                    return pos;
                }
            }
        }
        return null;
    }

    public boolean isInCheck(TeamColor color) {
        ChessPosition king = getKingPosition(color);

        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                if (isThreat(r, c, color, king))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isThreat(int r, int c, TeamColor color, ChessPosition king) {
        ChessPosition pos = new ChessPosition(r, c);
        ChessPiece p = table.getPiece(pos);

        if (p == null || p.getTeamColor() == color)
        {
            return false;
        }

        for (ChessMove m : p.pieceMoves(table, pos)) {
            if (m.getEndPosition().equals(king))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isInCheckmate(TeamColor color) {
        return isInCheck(color) && noMoves(color);
    }

    public boolean isInStalemate(TeamColor color) {
        return !isInCheck(color) && noMoves(color);
    }

    private boolean noMoves(TeamColor color) {
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece p = table.getPiece(pos);

                if (p != null && p.getTeamColor() == color) {
                    Collection<ChessMove> moves = validMoves(pos);
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void setBoard(ChessBoard board) {
        this.table = board;
    }

    public ChessBoard getBoard() {
        return table;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ChessGame chessGame = (ChessGame) o;

        return turn == chessGame.turn &&
                Objects.equals(table, chessGame.table);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turn, table);
    }
}
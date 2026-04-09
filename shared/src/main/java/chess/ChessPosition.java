package chess;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class ChessPosition {

    private final int row;

    @SerializedName(value = "col", alternate = {"column"})
    private final int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return col;
    }

    public static ChessPosition fromAlgebraic(String s) {
        if (s == null || s.length() != 2) {
            throw new IllegalArgumentException("Bad pos");
        }

        int c = s.charAt(0) - 'a' + 1;
        int r = s.charAt(1) - '1' + 1;

        return new ChessPosition(r, c);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessPosition other)) return false;
        return row == other.row && col == other.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
}
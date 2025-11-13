package requestobjects;

import model.GameData;

import java.util.List;

public record ListResult(List<GameData> games)
{
}
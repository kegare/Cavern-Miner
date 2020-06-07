package cavern.miner.config;

public enum DisplayCorner
{
	TOP_RIGHT,
	TOP_LEFT,
	BOTTOM_RIGHT,
	BOTTOM_LEFT;

	public boolean isTop()
	{
		return this == TOP_RIGHT || this == TOP_LEFT;
	}

	public boolean isBottom()
	{
		return this == BOTTOM_RIGHT || this == BOTTOM_LEFT;
	}

	public boolean isRight()
	{
		return this == TOP_RIGHT || this == BOTTOM_RIGHT;
	}

	public boolean isLeft()
	{
		return this == TOP_LEFT || this == BOTTOM_LEFT;
	}
}
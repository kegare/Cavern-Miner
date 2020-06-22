package cavern.miner.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

public interface CommandConsumer<T>
{
	void accept(T t) throws CommandSyntaxException;

	default int run(T t) throws CommandSyntaxException
	{
		accept(t);

		return 1;
	}
}
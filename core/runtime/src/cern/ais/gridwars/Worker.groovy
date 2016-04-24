package cern.ais.gridwars

import cern.ais.gridwars.Game.TurnCallback
import cern.ais.gridwars.command.MovementCommand
import cern.ais.gridwars.network.Network
import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import groovy.transform.ThreadInterrupt
import groovy.transform.TimedInterrupt

import java.nio.ByteBuffer
import java.util.concurrent.TimeoutException

class Worker extends Listener {
	Client c
	private int id
	private int TIME_OUT = 60 * 1000 // 60 s

	Worker(int id, int port, String host) {
		this.id = id
		println("Worker $id. Created!")
		c = new Client(25 * 1024 * 1024, 25 * 1024 * 1024)
		c.addListener(this)
		c.start()
		Network.init(c)
		c.connect(5000, host, port)

		while (true) {
			sleep(1000)
		}
	}

	static void main(String[] args) {
		if (!args)
			println("Usage: worker id [port = 10000] [hostname = localhost]")

		new Worker(args[0] as int, (args.length > 1 ? args[1] : 10000) as int, args.length > 2 ? args[2] : "localhost")
	}

	@Override void connected(Connection connection) {
		println("Worker $id Connected!")
		connection.sendTCP(new Ready(id))
	}

	@Override void disconnected(Connection connection) {
		println("Worker $id Disconnected!")
		System.exit(1)
	}

	@Override void received(Connection connection, Object o) {
		println("Received ${ o.getClass() }")
		switch (o) {
			case Std.DIE: System.exit(0); break;
			case StartMatch: startGame(o as StartMatch); break;
		}
	}

	def startGame(StartMatch match) {
		Thread.start {
			long time = System.currentTimeMillis()
			Game game = null
			List<Player> players = null
			try {
				players = PlayerUtil.prepare(match)
				game = new Game(players, new TurnCallback() {
					@Override void onPlayerResponse(Player player, int turn, List<MovementCommand> movementCommands,
					                                ByteBuffer binaryGameStatus)
					{
						println("Processing turn $turn")
						c.sendTCP(new TurnInfo(binaryGameStatus.array(), turn, player.id))
					}
				})

				game.startUp()
				while (!game.done()) {
					if (System.currentTimeMillis() - time > TIME_OUT)
						throw new TimeoutException()
					game.nextTurn()
				}

				players*.outputStream*.close()
			}
			catch (any) {
				println("Unrecoverable error in game.")
				any.printStackTrace()
			}

			println("Game simulation takes ${ ((System.currentTimeMillis() - time) / 1000 as Double).round(2) } s.")
			if (game && players && game.done())
				println("Fin. Game $match.matchId finished. $game.winner won!")
			else
				println("Fin. Game $match.matchId failed!")

			c.sendTCP(new MatchResults(match.matchId, match.playerData1.player, trimOutput(players?.get(0)?.outputFile?.bytes),
				match.playerData2.player, trimOutput(players?.get(1)?.outputFile?.bytes), game?.winner?.id, game ? true : false))
			println("Sending...")
		}
	}

	static final int MAX_SIZE = 2 * 1024 * 1024 // 2MB
	static byte[] trimOutput(byte[] array) {
		if (!array) return new byte[0]
		if (array.length < MAX_SIZE)
			return array

		def res = new byte[array.length]
		System.arraycopy(array, 0, res, 0, MAX_SIZE)
		res
	}
}

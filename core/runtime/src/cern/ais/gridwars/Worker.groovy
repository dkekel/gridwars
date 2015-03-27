package cern.ais.gridwars

import cern.ais.gridwars.Game.TurnCallback
import cern.ais.gridwars.command.MovementCommand
import cern.ais.gridwars.network.Network
import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener

import java.nio.ByteBuffer

class Worker extends Listener {
	Client c
	private int id

	Worker(int id) {
		this.id = id
		println("Worker $id. Created!")
		c = new Client(16384000, 2048000)
		c.addListener(this)
		c.start()
		Network.init(c)
		c.connect(5000, 'localhost', 10000)

		while (true) {
			sleep(1000)
		}
	}

	static void main(String[] args) {
		new Worker(args[0] as int)
	}

	@Override void connected(Connection connection) {
		println("Worker $id Connected!")
		connection.sendTCP(new Ready(id))
	}

	@Override void disconnected(Connection connection) {
		println("Worker $id Disconnected!")
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
			def players = PlayerUtil.prepare(match)
			def game = new Game(players, new TurnCallback() {
				@Override void onPlayerResponse(Player player, int turn, List<MovementCommand> movementCommands, ByteBuffer binaryGameStatus) {
					println("Processing turn $turn")
					c.sendTCP(new TurnInfo(binaryGameStatus.array(), turn, player.id))
				}
			})

			game.startUp()
			while (!game.done())
				game.nextTurn()

			players*.outputStream*.close()
			players*.outputStream

			println("Game simulation takes ${ ((System.currentTimeMillis() - time) / 1000 as Double).round(2) } s.")
			println("Fin. $game.winner won!")
			c.sendTCP(new MatchResults(match.matchId, match.playerData1.player, players[0].outputFile.bytes,
				match.playerData2.player, players[1].outputFile.bytes, game.winner.id))
			println("Sending...")
		}
	}
}

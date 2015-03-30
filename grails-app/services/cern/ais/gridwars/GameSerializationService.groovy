package cern.ais.gridwars

import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterOutputStream

class GameSerializationService
{
	private static final int TURN_DATA_SIZE = 4 * GameConstants.UNIVERSE_SIZE * GameConstants.UNIVERSE_SIZE

	def fileSystemService
	def save(long matchId, Collection<TurnInfo> info) {
		ByteArrayOutputStream compressed = new ByteArrayOutputStream();
		def os = new DeflaterOutputStream(compressed)
		for (it in info)
			os.write(it.data)
		os.close()
		println "compressed data takes: ${ (compressed.size() / 1024 / 1024 as Double).round(2) } MB."

		def match = Match.get(matchId)
		fileSystemService.matchResult(matchId).bytes = compressed.toByteArray()
		match.running = false
		match.winner = match.players[0].agent
		match.turns = info.size()

		def player1 = match.players[0]
		player1.setOutcome(Outcome.DRAW)
		player1.save()

		def player2 = match.players[1]
		player2.setOutcome(Outcome.DRAW)
		player2.save()
		match.save(failOnError: true)
	}

	Iterator<byte[]> load(long matchId) {
		def decompressed = new ByteArrayOutputStream()
		def inflater = new InflaterOutputStream(decompressed)
		inflater.write(fileSystemService.matchResult(matchId).bytes)
		inflater.close()

		int numTurns = decompressed.size() / TURN_DATA_SIZE as int
		def dataToBeSent = decompressed.toByteArray()
		int curr = 0
		new Iterator<byte[]>() {
			@Override boolean hasNext() {
				return curr < numTurns
			}

			@Override byte[] next() {
				def res = new byte[TURN_DATA_SIZE]
				System.arraycopy(dataToBeSent, curr * TURN_DATA_SIZE, res, 0, TURN_DATA_SIZE)
				curr++
				res
			}

			@Override void remove() {
				throw new UnsupportedOperationException()
			}
		}
	}
}

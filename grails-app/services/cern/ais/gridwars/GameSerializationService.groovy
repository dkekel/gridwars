package cern.ais.gridwars

import cern.ais.gridwars.Match.Status

import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterOutputStream

class GameSerializationService
{
	private static final int TURN_DATA_SIZE = 4 * GameConstants.UNIVERSE_SIZE * GameConstants.UNIVERSE_SIZE

	def fileSystemService
	def save(long matchId, Collection<TurnInfo> info, MatchResults results) {
		ByteArrayOutputStream compressed = new ByteArrayOutputStream();
		def os = new DeflaterOutputStream(compressed)
		for (it in info)
			os.write(it.data)
		os.close()
		println "compressed data takes: ${ (compressed.size() / 1024 / 1024 as Double).round(2) } MB."

		def match = Match.get(matchId)
		fileSystemService.matchResult(matchId).bytes = compressed.toByteArray()
		match.status = Status.SUCCEDED
		match.winner = Agent.get(results.winnerId)
		match.turns = info.size()

		match.save(failOnError: true)

		fileSystemService.outputFile(matchId, match.player1.id).bytes = results.output1
		fileSystemService.outputFile(matchId, match.player2.id).bytes = results.output2
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

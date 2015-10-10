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
		match.status = results.isComplete ? Status.SUCCEDED : Status.FAILED
		match.winner = results.winnerId ? Agent.get(results.winnerId) : null
		match.turns = info.size()

		match.save(failOnError: true)

		if (results.output1)
			fileSystemService.outputFile(matchId, match.player1.id).bytes = results.output1
		if (results.output2)
			fileSystemService.outputFile(matchId, match.player2.id).bytes = results.output2
	}

	byte[] load(long matchId) {
		fileSystemService.matchResult(matchId).bytes
	}
}

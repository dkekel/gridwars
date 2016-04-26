package cern.ais.gridwars

import cern.ais.gridwars.Match.Status
import cern.ais.gridwars.security.Role
import cern.ais.gridwars.security.User
import cern.ais.gridwars.security.UserRole

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

		def compressedMatch = compressed.toByteArray()
		fileSystemService.matchResult(matchId).bytes = compressedMatch
		match.status = results.isComplete ? Status.SUCCEDED : Status.FAILED
		match.winner = results.winnerId ? Agent.get(results.winnerId) : null
		match.turns = info.size()
		match.fileSize = compressedMatch.length
		if (results.isComplete && results.winnerId) {
			def player1 = User.get(match.player1.team.id)
			def player2 = User.get(match.player2.team.id)

			def player1IsAdmin = player1.authorities.find { it.authority == "ADMIN_BOT" }
			def player2IsAdmin = player2.authorities.find { it.authority == "ADMIN_BOT" }
			if ((player1IsAdmin && !player2IsAdmin) || (player2IsAdmin && !player1IsAdmin)) {
				def rank = Math.max(player1.rank, player2.rank)
				if (results.winnerId != match.player1Id)
					match.winner.team.rank = rank
			}
			match.winner.team.save(validate: true)
		}

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

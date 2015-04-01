package cern.ais.gridwars

import groovy.transform.Immutable

@Immutable
class MatchResults
{
	long matchId

	long player1
	byte[] output1

	long player2
	byte[] output2

	Long winnerId
	boolean isComplete
}

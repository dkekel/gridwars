package cern.ais.gridwars

import groovy.transform.Immutable

@Immutable
class TurnInfo {
	byte[] data
	int turn
	long playerId
}

package cern.ais.gridwars

import groovy.transform.Immutable

@Immutable
class TurnInfo implements Comparable<TurnInfo> {
	byte[] data
	int turn
	long playerId

	@Override int compareTo(TurnInfo o) {
		return turn
	}
}

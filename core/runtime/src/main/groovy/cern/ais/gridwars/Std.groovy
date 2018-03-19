package cern.ais.gridwars

import groovy.transform.Immutable

enum Std {
	DIE
}

@Immutable
class Ready {
	int workerId
}

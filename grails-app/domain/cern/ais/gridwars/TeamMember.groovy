package cern.ais.gridwars

import cern.ais.gridwars.security.User

class TeamMember
{
	static enum Gender {
		MALE, FEMALE
	}

	static enum TShirtSize {
		XS,S,M,L,XL
	}

	static belongsTo = [team: User]

	String name
	String lastName
	Gender gender
	TShirtSize tShirtSize

	static constraints = {
		name blank: false, size: 2..100
		lastName blank: false, size: 2..100
	}
}

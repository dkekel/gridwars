package cern.ais.gridwars

import cern.ais.gridwars.security.User

class TeamMemberController
{
	def springSecurityService

	def index() {
		[team: teams, member: (params.member ?: null) as TeamMember]
	}

	private List<TeamMember> getTeams() {
		TeamMember.findAllByTeam(springSecurityService.currentUser as User)
	}

	def add() {
		TeamMember member = null
		try {
			member = new TeamMember(params)
			member.team = springSecurityService.currentUser as User
			member.save(flush: true, failOnError: true)
			redirect(action: "index")
		}
		catch (ignored) {
			render(view: "index", model: [member: member, team: teams])
		}
	}

	def delete(long id) {
		teams?.find { it.id == id }?.delete()
		redirect(action: "index")
	}
}

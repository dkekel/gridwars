package cern.ais.gridwars

import cern.ais.gridwars.security.User
import grails.plugin.springsecurity.SpringSecurityUtils

class TeamMemberController
{
	def springSecurityService

	def index(Long id) {
		if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN") && id != null)
			[team: TeamMember.findAllById(id), member: null]
		else
			[team: teams, member: (params.member ?: null) as TeamMember]
	}

	def list() {
		[users: User.list()]
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

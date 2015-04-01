package cern.ais.gridwars

import cern.ais.gridwars.Match.Status

class AdminController
{
	def fileSystemService
	def matchmakingService

	def index() {
		[config: fileSystemService.config.text]
	}

	def queue() {
		[matches: matchmakingService.getPendingMatches(), showStatus: params.showStatus == null ? false : params.showStatus]
	}

	def histo() {
		render view: "queue", model: [matches: Match.list(), showStatus: true]
	}

	def update() {
		fileSystemService.config.text = params.config
		fileSystemService.init()
		redirect action: "index"
	}

	def status() {
		def match = Match.get(params.id as long)
		if (match) {
			try {
				match.status = Status.valueOf(params.status as String)
				match.save(flush: true, failOnError: true)
				println("set status $match.status")
			}
			catch (any) {
				println("Failure in status method: ")
				any.printStackTrace()
			}
		}
		redirect action: "queue"
	}
}

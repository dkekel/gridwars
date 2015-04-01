package cern.ais.gridwars

class AdminController
{
	def fileSystemService
	def matchmakingService

	def index() {
		[config: fileSystemService.config.text]
	}

	def queue() {
		[matches: matchmakingService.getPendingMatches()]
	}

	def update() {
		fileSystemService.config.text = params.config
		fileSystemService.init()
		redirect action: "index"
	}
}

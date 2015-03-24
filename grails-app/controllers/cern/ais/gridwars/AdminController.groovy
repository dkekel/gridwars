package cern.ais.gridwars

class AdminController
{
	def fileSystemService
	def index() {
		[config: fileSystemService.config.text]
	}

	def update() {
		fileSystemService.config.text = params.config
		fileSystemService.init()
		redirect action: "index"
	}
}

// Relative pathes to files produced by server
jar = 'player-jars/'
output = 'player-outputs/'
matches = 'player-matches/'

admins = ["grid.wars@cern.ch"]
sendMailsToAdmin = !true
spawnWorkers = !false
jobsEnabled = true

capabilities {
	upload = true // Used to block uploading, while final match is ready.
	registration = true // Doesn't work
	matches = true // Doesn't work
}

numMatches = 15 // Number of matches to be played between each player. Use 1 before, and odd number from 3 to final match.

workers {
	classpath = "${ System.properties.GW_HOME }/workers/workerClassPath/*"
	home = "${ System.properties.GW_HOME }/workers/"
	max = 4  // Number of workers
	managerPort = 10001
	durability = 1 // NOT USED YET (idea was to have a number of matches played on each worker before death)
}

panic {
	maxFailures = 300 // 5 min of inactivity
	queueSize = 50 // pendingMatches.
}

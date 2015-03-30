jar = 'player-jars/'
output = 'player-outputs/'
matches = 'player-matches/'

admins = ["xSeagullx@gmail.com", "grid.wars@cern.ch"]
sendMailsToAdmin = !true

capabilities {
	upload = true
	registration = true
	matches = true
}

workers {
	classpath = "${ System.properties.GW_HOME }/workers/workerClassPath/*"
	home = "${ System.properties.GW_HOME }/workers/"
	max = 4
	managerPort = 10001
	durability = 1
}


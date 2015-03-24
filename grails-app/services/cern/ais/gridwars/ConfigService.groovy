package cern.ais.gridwars

class ConfigService implements Map
{
	@Delegate private ConfigObject conf

	void update(File file) {
		conf = new ConfigSlurper().parse(file.toURI().toURL())
	}
}

package cern.ais.gridwars

import cern.ais.gridwars.bot.PlayerBot
import groovy.transform.Immutable

@Immutable class PlayerData {
	byte[] jar
	long player // Agent id
	String className
}

@Immutable
class StartMatch {
	long matchId

	PlayerData playerData1
	PlayerData playerData2
}

class PlayerUtil {
	static List<Player> prepare(StartMatch match) {
		int i = 0;
		[match.playerData1, match.playerData2].collect {
			new Player(it.player, loadBot(it), new File("Player${ it.player }_Output.txt"), i++)
		}
	}

	private static PlayerBot loadBot(PlayerData data) {
		def file = File.createTempFile(randomStr(10), randomStr(10))
		file.bytes = data.jar
		def classLoader = new URLClassLoader([file.toURI().toURL()] as URL[], this.getClassLoader())
		classLoader.loadClass(data.className).newInstance() as PlayerBot
	}

	private static r = new Random()
	private static charList = ('a'..'z').asList()

	private static String randomStr(int length)
	{
		(1..length).collect { charList[r.nextInt(charList.size() - 1)] }.join('')
	}
}

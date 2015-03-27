package cern.ais.gridwars

import cern.ais.gridwars.network.Network
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server

import java.util.zip.DeflaterOutputStream

class RuntimeMain extends Listener {
	Server s

	RuntimeMain(String classPath) {
		s = new Server(16384000, 2048000)
		s.start()
		Network.init(s)
		s.addListener(this)
		s.bind(10000)
		println("Start server")

		Thread.start {
			println("Worker 10001 is being born")
			def fos = new FileOutputStream(new File('Worker10001_Output.txt'))
			def builder = new ProcessBuilder(new File(System.getProperty("java.home") as String, 'bin/java').absolutePath, "-cp", classPath, "cern.ais.gridwars.Worker", '1')
			builder.directory(new File('/Users/seagull/Dev/cern/gridwars/home/workerHome'))
			builder.redirectErrorStream(true)
			def p = builder.start()
			p.waitForProcessOutput(fos, fos)
			int ev = p.exitValue()
			fos.close()
			println("Worker 10001 is dead. ExitValue $ev")
		}

		while (true) {
			sleep(1000)
		}
	}

	static main(arg) {
		new RuntimeMain("/Users/seagull/Dev/cern/gridwars/home/workerClassPath/*")
	}

	long i = 0;
	ByteArrayOutputStream bis = new ByteArrayOutputStream();
	
	@Override void received(Connection connection, Object o) {
		switch (o) {
			case TurnInfo: print('.'); bis.write((o as TurnInfo).data); if ((o as TurnInfo).turn % 100 == 0) println(); break;
			case MatchResults:
				def mr = o as MatchResults
				println("\n Math finished. $mr.winnerId won!")
				println "amout of data produced: ${ (bis.size() / 1024 / 1024 as Double).round(2) } MB."
				ByteArrayOutputStream cbis = new ByteArrayOutputStream();
				def os = new DeflaterOutputStream(cbis)
				os.write(bis.toByteArray())
				println "compressed data takes: ${ (cbis.size() / 1024 / 1024 as Double).round(2) } MB."
			break;
		}
	}

	@Override void connected(Connection connection) {
		def sM = new StartMatch(1, 
			new PlayerData(new File('/Users/seagull/Dev/cern/gridwars/target/work/gw.defName.defBot.jar').bytes, 1, "gw.defName.defBot"),
			//new PlayerData(new File('/Users/seagull/Dev/cern/gridwars/target/work/ru.tversu.gridwars.TverSUbot81.jar').bytes, 2, "ru.tversu.gridwars.TverSUbot81")
			new PlayerData(new File('/Users/seagull/Dev/cern/gridwars/target/work/gw.defName.defBot.jar').bytes, 2, "gw.defName.defBot")
		)
		connection.sendTCP(sM)
	}

	@Override void disconnected(Connection connection) {
		println("Someone is dead… $connection.ID")
	}
}

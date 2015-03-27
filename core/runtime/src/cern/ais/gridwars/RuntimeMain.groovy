package cern.ais.gridwars

import cern.ais.gridwars.network.Network
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import groovy.transform.Canonical
import groovy.util.logging.Log

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.DeflaterOutputStream

@Log
class RuntimeMain extends Listener {
	private class Task {
		StartMatch match
		Closure callback

		Connection connection
		int workerId

		Task(StartMatch match, Closure callback) {
			this.match = match
			this.callback = callback
		}
	}

	private static AtomicInteger lastWorkerId = new AtomicInteger(0)

	private final Server s
	private final String classPath
	private pool = Executors.newFixedThreadPool(4)
	private Map<Connection, Task> workers = new ConcurrentHashMap<>()
	private Queue<Task> queue = new ConcurrentLinkedQueue<>()

	RuntimeMain(String classPath)
	{
		this.classPath = classPath
		s = new Server(16384000, 2048000)
		s.start()
		Network.init(s)
		s.addListener(this)
		s.bind(10000)
		println("Start server")
	}

	static main(arg)
	{
		def m = new RuntimeMain("/Users/seagull/Dev/cern/gridwars/core/runtime/build/home/workerClassPath/*")
		def sM = new StartMatch(1,
			new PlayerData(new File('/Users/seagull/Dev/cern/gridwars/target/work/gw.defName.defBot.jar').bytes, 1, "gw.defName.defBot"),
			//new PlayerData(new File('/Users/seagull/Dev/cern/gridwars/target/work/ru.tversu.gridwars.TverSUbot81.jar').bytes, 2, "ru.tversu.gridwars.TverSUbot81")
			new PlayerData(new File('/Users/seagull/Dev/cern/gridwars/target/work/gw.defName.defBot.jar').bytes, 2, "gw.defName.defBot")
		)
		def tryExit = { int count-> { -> count--; if (!count) System.exit(0) } }(2)
		m.send(sM) { println("done 1"); tryExit() }
		m.send(sM) { println("done 2"); tryExit() }

		while (true)
			sleep(1000)
	}

	boolean isSlotAvailable() {
		return workers.size() < 4
	}

	private void spawnWorker() {
		pool.submit {
			int workerId = lastWorkerId.andIncrement
			println("Worker $workerId is being born")
			def fos = new FileOutputStream(new File("Worker${workerId}_Output.txt"))
			def builder = new ProcessBuilder(new File(System.getProperty("java.home") as String, 'bin/java').absolutePath, "-cp", classPath, "cern.ais.gridwars.Worker", "$workerId")
			builder.directory(new File('/Users/seagull/Dev/cern/gridwars/home/workerHome'))
			builder.redirectErrorStream(true)
			def p = builder.start()
			p.waitForProcessOutput(fos, fos)
			int ev = p.exitValue()
			fos.close()
			println("Worker $workerId is dead. ExitValue $ev")
		}
	}

	private long i = 0;
	private ByteArrayOutputStream bis = new ByteArrayOutputStream();

	@Override void received(Connection connection, Object o)
	{
		switch (o)
		{
		case TurnInfo: print('.'); bis.write((o as TurnInfo).data); if ((o as TurnInfo).turn % 100 == 0) println(); break;
		case MatchResults:
			def mr = o as MatchResults
			println("\n Math finished. $mr.winnerId won!")
			println "amout of data produced: ${ (bis.size() / 1024 / 1024 as Double).round(2) } MB."
			ByteArrayOutputStream cbis = new ByteArrayOutputStream();
			def os = new DeflaterOutputStream(cbis)
			os.write(bis.toByteArray())
			println "compressed data takes: ${ (cbis.size() / 1024 / 1024 as Double).round(2) } MB."
			workers[connection].callback()
			workers.remove(connection)
			// TODO do smth with worker.
			break;
		case Ready:
			def task = queue.poll()
			task.connection = connection
			task.workerId = (o as Ready).workerId 
			workers[connection] = task
			connection.sendTCP(task.match)
			break
		}
	}

	void send(StartMatch match, Closure callback) {
		if (!slotAvailable)
			throw new IllegalStateException("Sorry no slot available!")

		log.info("Queue. $match")
		queue << new Task(match, callback)
		spawnWorker()
	}

	@Override void connected(Connection connection) {
		log.info("Worker connected. $connection")
	}

	@Override void disconnected(Connection connection) {
		println("Someone is dead… $connection.ID")
	}
}

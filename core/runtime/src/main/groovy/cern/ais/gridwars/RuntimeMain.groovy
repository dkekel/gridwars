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
		private static long TIMEOUT = 1000 * 60 // 1 minute  
		StartMatch match
		Closure callback

		Connection connection
		int workerId
		long startTime = -1

		Task(StartMatch match, Closure callback) {
			this.match = match
			this.callback = callback
		}

		boolean isTimeOut() {
			startTime != -1 && (System.currentTimeMillis() - startTime > TIMEOUT)
		}
	}

	private static AtomicInteger lastWorkerId = new AtomicInteger(0)

	private final Server s
	private final String classPath
	private final String workerHome
	private pool
	private Map<Connection, Task> workers = new ConcurrentHashMap<>()
	private Queue<Task> queue = new ConcurrentLinkedQueue<>()
	private static int port
	int maxWorkers

	RuntimeMain(String classPath, String home, int maxWorkers = 4, int port = 10000)
	{
		this.port = port
		workerHome = home
		this.maxWorkers = maxWorkers
		this.classPath = classPath
		pool = Executors.newFixedThreadPool(maxWorkers)

		s = new Server(25 * 1024 * 1024, 25 * 1024 * 1024)
		s.start()
		Network.init(s)
		s.addListener(this)
		s.bind(port)
		println("Start server")
	}

	static main(arg)
	{
		def m = new RuntimeMain("/Users/seagull/Dev/cern/gridwars/core/runtime/build/home/workerClassPath/*", '/Users/seagull/Dev/cern/gridwars/home/workerHome')
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
		return workers.size() + queue.size() < maxWorkers
	}

	private void spawnWorker() {
		pool.submit {
			int workerId = lastWorkerId.andIncrement
			try
			{
				println("Worker $workerId is being born")
				def fos = new FileOutputStream(new File("Worker${ workerId }_Output.txt"))
				def builder = new ProcessBuilder(new File(System.getProperty("java.home") as String, 'bin/java').absolutePath, "-cp", classPath, "cern.ais.gridwars.Worker", "$workerId", "$port")
				builder.directory(new File(workerHome))
				builder.redirectErrorStream(true)
				def p = builder.start()
				p.waitForProcessOutput(fos, fos)
				int ev = p.exitValue()
				fos.close()
				println("Worker $workerId is dead. ExitValue $ev")
			}
			catch (any) {
				print("Error while instantiating worker $workerId")
				any.printStackTrace()
			}
		}
	}

	private Map<Integer, PriorityQueue<TurnInfo>> gameData = new ConcurrentHashMap<>()

	@Override void received(Connection connection, Object o)
	{
		switch (o)
		{
		case TurnInfo:
			if (!gameData.containsKey(connection.ID))
				break
			TurnInfo turn = o as TurnInfo
			gameData[connection.ID].add(turn)
			break;
		case MatchResults:
			if (!workers.containsKey(connection))
				break
			def mr = o as MatchResults
			println("\n Math finished. $mr.winnerId won!")
			def queue = gameData[connection.ID]
			workers[connection].callback(queue, o)
			workers.remove(connection)
			gameData.remove(connection.ID)
			// TODO do smth with worker.
			connection.sendTCP(Std.DIE)
			break;
		case Ready:
			def task = queue.poll()
			if (!task) {
				println("No task to execute: Killing ${ workers[connection]?.workerId }.")
				connection.sendTCP(Std.DIE)
				return
			}
			task.connection = connection
			task.startTime = System.currentTimeMillis() 
			task.workerId = (o as Ready).workerId 
			workers[connection] = task
			connection.sendTCP(task.match)
			gameData[connection.ID] = new PriorityQueue<>()
			break
		}
	}

	void cleanup() {
		workers.values().grep { Task t -> t.timeOut }.each {
			it.connection.sendTCP(Std.DIE)
			def task = workers.remove(it.connection)
			if (task)
				reportFailure(task, gameData.remove(it.connection.ID))?.callback()
		}
	}

	void send(StartMatch match, Closure callback) {
		if (!slotAvailable)
			throw new IllegalStateException("Sorry no slot available!")

		log.info("Queue. $match.matchId")
		queue << new Task(match, callback)
		spawnWorker()
	}

	@Override void connected(Connection connection) {
		log.info("Worker connected. $connection")
	}

	@Override void disconnected(Connection connection) {
		println("Someone is dead… $connection.ID")
		def cb = workers.remove(connection)
		def data = gameData.remove(connection.ID)
		if (cb)
			reportFailure(cb, data)
		if (!queue.empty)
			spawnWorker()
	}
	
	private void reportFailure(Task cb, Collection<TurnInfo> data) {
		cb.callback(data, new MatchResults(cb.match.matchId, cb.match.playerData1.player, "Failure".bytes, cb.match.playerData2.player, "Failure".bytes, null, false))
	}
}

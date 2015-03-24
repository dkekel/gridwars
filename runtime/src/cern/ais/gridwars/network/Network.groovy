package cern.ais.gridwars.network

import cern.ais.gridwars.MatchResults
import cern.ais.gridwars.PlayerData
import cern.ais.gridwars.StartMatch
import cern.ais.gridwars.Std
import cern.ais.gridwars.TurnInfo
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.ByteArraySerializer
import com.esotericsoftware.kryo.serializers.DeflateSerializer
import com.esotericsoftware.kryonet.EndPoint
import com.esotericsoftware.minlog.Log

class Network {
	static void init(EndPoint endPoint) {
		Log.set(Log.LEVEL_INFO)

		def kryo = endPoint.kryo
		def serializer = new DeflateSerializer(new ByteArraySerializer())
		serializer.compressionLevel = 8
		kryo.register((new byte[0]).class, serializer)
		kryo.register(ArrayList)
		kryo.register(HashSet)
		kryo.register(LinkedHashMap)

		kryo.register(Std)
		kryo.register(TurnInfo)
		kryo.register(StartMatch)
		kryo.register(PlayerData)
		kryo.register(MatchResults)
	}
}

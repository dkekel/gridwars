package cern.ais.gridwars

import org.apache.commons.codec.binary.Hex

import java.security.MessageDigest

class FileSystemService {
	static class SharedJarFile {
		final File file
		private boolean shared

		private SharedJarFile(File file, boolean shared) {
			this.file = file;
			this.shared = shared;
		}

		void dispose() {
			if (shared) // File is owned by someone else.
				return

			file.delete();
		}
	}

  static transactional = false
  static scope = 'singleton'

  def grailsApplication
  def configService
  @Lazy private File jarHome = { initHome(new File(home, configService.jar as String)) }()
  @Lazy private File matchHome = { initHome(new File(home, configService.matches as String)) }()
  @Lazy private File outputHome = { initHome(new File(home, configService.output as String)) }()

  private String digest(byte[] bytes) {
    Hex.encodeHexString(MessageDigest.getInstance("SHA-256").digest(bytes))
  }

  File getConfig() {
    new File(home, "config.groovy")
  }

  File getHome() {
    new File(System.properties.GW_HOME as String)
  }

  void init() {
    configService.update(config)
  }
  
  File jarFile(String name) {
    new File(jarHome, name)
  }

  File outputFile(long gameId, long agentId) {
    new File(outputHome, "match_${ gameId }_agent_${ agentId }.txt")
  }
  
  private static File initHome(File file) {
    if (!file.exists() && !file.mkdirs())
      throw new RuntimeException("Failed to initalize fileSystem. Can't create directory $file.")
    file
  }

  File matchResult(long id) {
    new File(matchHome, "match_$id")
  }

  SharedJarFile storeJar(byte[] bytes) {
    def file = jarFile(digest(bytes) + ".jar")
    def exists = file.exists()
    if (!exists)
      file.bytes = bytes;

    new SharedJarFile(file, exists)
  }
}

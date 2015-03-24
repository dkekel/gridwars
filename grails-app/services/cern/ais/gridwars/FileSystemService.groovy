package cern.ais.gridwars

class FileSystemService {
  static transactional = false
  static scope = 'singleton'

  def grailsApplication
  private File jarHome
  private File outputHome

  void init() {
    jarHome = initHome("${ grailsApplication.config.cern.ais.gridwars.basedir }player-jars/")
    outputHome = initHome("${grailsApplication.config.cern.ais.gridwars.basedir}player-outputs/")
  }
  
  File jarFile(String name) {
    new File(jarHome, name)
  }

  File outputFile(String name) {
    new File(outputHome, name)
  }
  
  private static File initHome(String filePath) {
    def file = new File(filePath)
    if (!file.exists() && !file.mkdirs())
      throw new RuntimeException("Failed to initalize fileSystem. Can't create directory $file.")
    file
  }
}

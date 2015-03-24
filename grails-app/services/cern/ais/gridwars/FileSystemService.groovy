package cern.ais.gridwars

class FileSystemService {
  static transactional = false
  static scope = 'singleton'

  def grailsApplication
  def configService
  private File jarHome
  private File outputHome

  File getConfig() {
    new File(home, "config.groovy")
  }

  File getHome() {
    new File(System.properties.GW_HOME as String)
  }

  void init() {
    configService.update(config)
    jarHome = initHome(new File(home, configService.jar as String))
    outputHome = initHome(new File(home, configService.output as String))
  }
  
  File jarFile(String name) {
    new File(jarHome, name)
  }

  File outputFile(String name) {
    new File(outputHome, name)
  }
  
  private static File initHome(File file) {
    if (!file.exists() && !file.mkdirs())
      throw new RuntimeException("Failed to initalize fileSystem. Can't create directory $file.")
    file
  }
}

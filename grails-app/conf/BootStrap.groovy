import cern.ais.gridwars.User
import grails.util.Environment
import org.apache.commons.codec.digest.DigestUtils

class BootStrap
{

  def init = { servletContext ->
    
    if (Environment.current == Environment.DEVELOPMENT) {
      2.times {
        def u = new User()
        u.username = "user$it"
        u.salt = "salt";
        def pass = "user$it"
        u.hashedPassword = DigestUtils.sha256Hex(u.salt + pass)
        u.save(failOnError: true, validate: true)
        println("$u.username $pass")
      }
    }
  }

  def destroy = {
  }
}

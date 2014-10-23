import cern.ais.gridwars.User
import org.apache.commons.codec.digest.DigestUtils

class BootStrap
{

  def init = { servletContext ->

//    environment {
//      dev {

//        (0..9).each {
//          def u = new User()
//          u.username = "user$it"
//          u.salt = Math.round(Math.random() * 10000).toString()
//          u.hashedPassword = DigestUtils.sha256Hex(u.salt + "user$it")
//          u.save(failOnError: true, validate: true)
//        }
//      }
//    }
  }

  def destroy = {
  }
}

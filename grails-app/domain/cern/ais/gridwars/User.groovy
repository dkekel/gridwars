package cern.ais.gridwars

class User
{
  String username
  String hashedPassword
  String salt

  // Transients
  String password
  String passwordConfirm

  static transients = ['password', 'passwordConfirm']

  static hasMany = [agents: Agent]

  static constraints = {
    username blank: false, unique: true
    password bindable: true, blank: false, size: 5..15, matches: /[\S]+/, validator: { val, obj ->
      if (obj.password != obj.passwordConfirm)
        return 'user.password.dontmatch'
    }
    passwordConfirm bindable: true
  }
}

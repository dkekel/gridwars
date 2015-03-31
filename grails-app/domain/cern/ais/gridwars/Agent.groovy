package cern.ais.gridwars

import cern.ais.gridwars.security.User

class Agent
{
  String  jarPath
  String  fqClassName
  Date    uploadDate
  double  eloScore = 1000
  boolean active = true

  static belongsTo = [team: User]

  static constraints = {
  }

  boolean equals(o)
  {
    if (this.is(o)) return true
    if (getClass() != o.class) return false
    return id == ((Agent) o).id
  }

  int hashCode()
  {
    return (id != null ? id.hashCode() : 0)
  }
}

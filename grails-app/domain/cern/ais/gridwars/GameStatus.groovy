package cern.ais.gridwars

class GameStatus
{

  byte[] imageData
//  String content

  static constraints = {
    imageData nullable: false, maxSize: 1048576
  }

  static mapping = {
//    content type: 'text'
  }

}

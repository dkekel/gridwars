package cern.ais.gridwars

import java.text.SimpleDateFormat

class MatchJob
{
  def                      concurrent = false
  def MatchmakingService   matchmakingService
  def GameExecutionService gameExecutionService
  def mailService
  def configService

  static triggers = {
    simple repeatInterval: 1000l // execute job once in 1 seconds
  }

  def execute()
  {
    println("MatchJob tick")
    // execute job
    if (gameExecutionService == null || matchmakingService == null)
      return

    Match match = matchmakingService.nextMatch
    def pendingMatches = matchmakingService.pendingMatches.size()
    def limit = configService?.panic?.queueSize ?: 50
    if (pendingMatches > limit) {
      if (!matchmakingService.inPanic) {
        matchmakingService.inPanic = true
        mailService.sendMail {
          to configService.admins
          subject "[WARN] Pending matches $pendingMatches > $limit!"
          body "Time: ${ new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss").format(new Date()) }"
        }
      }
    }
    else {
      if (matchmakingService.inPanic) {
        matchmakingService.inPanic = false
        mailService.sendMail {
          to configService.admins
          subject "[INFO] Pending matches $pendingMatches is OK!"
          body "Time: ${ new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss").format(new Date()) }"
        }
      }
    }
    
    gameExecutionService.cleanup()

    if (match)
    {
      gameExecutionService.executeGame(match)
    }
  }
}

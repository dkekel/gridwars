# GridWars server project.

## Build & deploy process
_all paths are relative to project root directory._
__Require JDK 1.7, tomcat 7.x__

1. Use gradle wrapper script to install project dependencies in local maven repository. `/core/gradlew install`
2. Use grails wrapper script to build _.war_ file. `/grailsw war`
3. Prepare folder structure for grailsw (see details below). Make sure, tomcat user has an __rw__ access to it.
4. Generated from grails javadoc and api-jar file should be set up in tomcat config as static content.
5. Modify mailing service setup in Config file.
5. Deploy war file in tomcat. Note, that you need to have environmental variable called __GW_HOME__ to prepared
   directory structure. It'll be printed on grails startup, and server will die, if var is not specified.

## Directory structure in GW_HOME.
```
/
  /db (created automatically)
  /player-jars (specified in configs)
  /player-matches (specified in configs)
  /player-outputs (specified in configs)
  /workers (specified in configs)
    /workerClassPath (specified in configs. Contents of that dir are created by gradle task createWorkerClassPath.)
  config.groovy (put file from repository here.)
```

## Usage.
Admin controller have a nice overview of all available controllers.
For monitoring use:
`/admin/queue`  
`/admin/histo`  

For final match use 
`/admin/clear` and `/admin/startFight` controllers.

You can update most config values from admin page.

Default admin accounts are:
__login:__ _admin_ , __pass:__ _admin_
__login:__ _admin2_, __pass:__ _admin_

Passwords can be changed from user controller.

Enjoy)

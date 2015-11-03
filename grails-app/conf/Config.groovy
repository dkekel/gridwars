// locations to search for config files that get merged into the main config;
// config files can be ConfigSlurper scripts, Java properties files, or classes
// in the classpath in ConfigSlurper format

if (!System.properties.GW_HOME)
  throw new IllegalStateException("No GW_HOME accessible! Shutdown.")
grails.config.locations = [ "file:${ System.properties.GW_HOME }config.groovy }" ]

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
        all: '*/*',
        atom: 'application/atom+xml',
        css: 'text/css',
        csv: 'text/csv',
        form: 'application/x-www-form-urlencoded',
        html: ['text/html', 'application/xhtml+xml'],
        js: 'text/javascript',
        json: ['application/json', 'text/json'],
        multipartForm: 'multipart/form-data',
        rss: 'application/rss+xml',
        text: 'text/plain',
        xml: ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart = false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

grails.dbconsole.enabled = true

environments {
  development {
    grails.logging.jul.usebridge = true
    def base = 'web-app/'
    def dbDir = "${base}/db/gw"
//    dataSource.url = "jdbc:h2:file:${dbDir}"
    cern.ais.gridwars.basedir = base
    cern.ais.gridwars.fileprotocol = "file://"
  }
  production {
    grails.app.context = "/"
    grails.logging.jul.usebridge = false
    cern.ais.gridwars.basedir = '/var/lib/tomcat/webapps/ROOT/'
    cern.ais.gridwars.fileprotocol = "file://"
    grails.serverURL = "http://gridwars.cern.ch"
    server.port = "80"
  }
}

// log4j configuration
log4j = {
  // Example of changing the log pattern for the default console appender:
  //
  //appenders {
  //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
  //}
  appenders {
    console name: 'stdout', layout: pattern(conversionPattern: '%d{ABSOLUTE} %5p [%c{1}] %m %n')
  }

  root {
    error 'stdout'
  }
//  trace 'org.hibernate.type'
  //debug 'org.hibernate.SQL'

  debug 'grails.app.jobs'
  debug 'cern.ais.gridwars'
  error 'org.codehaus.groovy.grails.web.servlet',        // controllers
          'org.codehaus.groovy.grails.web.pages',          // GSP
          'org.codehaus.groovy.grails.web.sitemesh',       // layouts
          'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
          'org.codehaus.groovy.grails.web.mapping',        // URL mapping
          'org.codehaus.groovy.grails.commons',            // core / classloading
          'org.codehaus.groovy.grails.plugins',            // plugins
          'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
          'org.springframework',
          'org.hibernate',
          'net.sf.ehcache.hibernate'
}

// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

/* remove this line 
// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside null
                scriptlet = 'none' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}
remove this line */

//grails.plugin.springsecurity.useBasicAuth = true
//grails.plugin.springsecurity.basic.realmName = "Grid Wars Server"

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'cern.ais.gridwars.security.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'cern.ais.gridwars.security.UserRole'
grails.plugin.springsecurity.authority.className = 'cern.ais.gridwars.security.Role'
grails.plugin.springsecurity.securityConfigType = "InterceptUrlMap"
grails.plugin.springsecurity.password.algorithm='SHA-512'

grails.plugin.springsecurity.ui.register.defaultRoleNames = ['ROLE_USER']
grails.plugin.springsecurity.ui.password.validationRegex= /^[!@#$%^&\d\w_\.]+$/

grails.plugin.springsecurity.interceptUrlMap = [
	//'/**':                            ['permitAll'],
	'/':                              ['ROLE_USER', 'ROLE_ADMIN'],
	'/assets/**':                     ['permitAll'],
	'/**/js/**':                      ['permitAll'],
	'/**/css/**':                     ['permitAll'],
	'/**/images/**':                  ['permitAll'],
	'/**/favicon.ico':                ['permitAll'],

	'/login/**':                      ['permitAll'],
	'/register/**':                   ['permitAll'],
	'/logout/**':                     ['ROLE_USER', 'ROLE_ADMIN'],

	'/game/**':                       ['ROLE_USER', 'ROLE_ADMIN'],
	'/teamMember/**':                 ['ROLE_USER', 'ROLE_ADMIN'],
	'/agentUpload/**':                ['ROLE_USER', 'ROLE_ADMIN'],
	'/game.ws':                       ['ROLE_USER', 'ROLE_ADMIN'],
	'/player-outputs/**':             ['ROLE_USER', 'ROLE_ADMIN'],

	'/admin/**':                      ['ROLE_ADMIN'],
	'/dbconsole/**':                  ['ROLE_ADMIN'],
  '/user/**':             ['ROLE_ADMIN'],
  '/role/**':             ['ROLE_ADMIN'],
  '/api/**':          ['ROLE_USER', 'ROLE_ADMIN'],
	//'/**':                            ['ROLE_ADMIN'],
]

grails.mail.default.from="grid.wars@cern.ch"
grails {
	mail {
		host = "smtp.cern.ch"
		port = 25
		username = "gridwars"
		password = "Babu-Kera"
		props = [
			"mail.smtp.auth":"true",
			"mail.smtp.socketFactory.port":"25",
			"mail.smtp.starttls.enable":"true",
		]
	}
}

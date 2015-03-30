dataSource {
    pooled = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
      dataSource {
        dbCreate = "update"
        println("jdbc:h2:${ "${System.properties.GW_HOME}/db/gw" };MVCC=TRUE;LOCK_TIMEOUT=10000")
        url = "jdbc:h2:${ "${System.properties.GW_HOME}/db/gw" };MVCC=TRUE;LOCK_TIMEOUT=10000"
        pooled = true
        logSql = true

        properties {
          maxActive = -1
          minEvictableIdleTimeMillis=1800000
          timeBetweenEvictionRunsMillis=1800000
          numTestsPerEvictionRun=3
          testOnBorrow=true
          testWhileIdle=true
          testOnReturn=true
          validationQuery="SELECT 1"
        }
      }

//      dataSource {
//        dbCreate = "create-drop"
//        url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
//      }
    }
    test {
        dataSource {
            dbCreate = "create-drop"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
    production {
      dataSource {
        dbCreate = "update"
        println("jdbc:h2:${ "${System.properties.GW_HOME}/db/gw" };MVCC=TRUE;LOCK_TIMEOUT=10000")
        url = "jdbc:h2:${ "${System.properties.GW_HOME}/db/gw" };MVCC=TRUE;LOCK_TIMEOUT=10000"
        pooled = true
        logSql = true

        properties {
          maxActive = -1
          minEvictableIdleTimeMillis=1800000
          timeBetweenEvictionRunsMillis=1800000
          numTestsPerEvictionRun=3
          testOnBorrow=true
          testWhileIdle=true
          testOnReturn=true
          validationQuery="SELECT 1"
        }
      }

//      dataSource {
//        pooled = true
//        driverClassName = "com.mysql.jdbc.Driver"
//        dialect = "org.hibernate.dialect.MySQL5InnoDBDialect"
//        dbCreate = "update"
//        url = "jdbc:mysql://128.142.152.59/gridwars?useUnicode=yes&characterEncoding=UTF-8"
//        username = "gridwars"
//        password = ""
//      }
    }
}

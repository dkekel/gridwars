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
            dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
    test {
        dataSource {
            dbCreate = "create-drop"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
    production {
      dataSource {
        pooled = true
        driverClassName = "com.mysql.jdbc.Driver"
        dialect = "org.hibernate.dialect.MySQL5InnoDBDialect"
        dbCreate = "update"
        url = "jdbc:mysql://128.142.152.59/gridwars?useUnicode=yes&characterEncoding=UTF-8"
        username = "gridwars"
        password = ""
      }
    }
}

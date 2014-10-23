class UrlMappings
{

  static mappings = {
    "/$controller/$action?/$id?" {
      constraints {
        // apply constraints here
      }
    }

    "/"(controller: 'main')
    "500"(view: '/error')
  }
}

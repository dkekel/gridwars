class UrlMappings
{

  static mappings = {
    "/$controller/$action?/$id?" {
      constraints {
        // apply constraints here
      }
    }

    "/"(controller: 'game')
    "500"(view: '/error')
  }
}

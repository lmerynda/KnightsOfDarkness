rootProject.name = "knights-of-darkness"
include("game", "storage", "web", "common")

sonar {
  properties {
    property("sonar.projectKey", "Uprzejmy_KnightsOfDarkness")
    property("sonar.organization", "uprzejmy")
    property("sonar.host.url", "https://sonarcloud.io")
  }
}

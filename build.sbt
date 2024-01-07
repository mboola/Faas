name := "Faas"
version := "1.0"

scalaVersion := "2.13.12"

//Define source directories
Compile / unmanagedSourceDirectories += baseDirectory.value / "src" / "main" / "scala"
Compile / unmanagedSourceDirectories += baseDirectory.value / "src"

//Dependencies
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0"
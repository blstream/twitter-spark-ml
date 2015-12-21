name := "twitterspark"

organization := "com.blstream"

version := "0.0.1"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val specsVersion = "2.4.17"
  val sparkVersion = "1.5.2"
  Seq(
    //"org.scalaz"          %%  "scalaz-core"                 % "7.1.5"                        withSources() withJavadoc(),
    "org.apache.spark"      %% "spark-core"                   % sparkVersion                   withSources() withJavadoc(),
    "org.apache.spark"      %% "spark-mllib"                  % sparkVersion                   withSources() withJavadoc(),
    "org.apache.spark"      %% "spark-streaming"              % sparkVersion                   withSources() withJavadoc(),
    "org.apache.spark"      %% "spark-streaming-twitter"      % sparkVersion                   withSources() withJavadoc(),
    "io.spray"              %% "spray-json"                   % "1.3.2"                        withSources() withJavadoc(),
    "com.google.code.gson"  %  "gson"                         % "2.3"                          withSources() withJavadoc(),
    "org.twitter4j"         %  "twitter4j-core"               % "3.0.3"                        withSources() withJavadoc(),
    "org.specs2"            %% "specs2-core"                  % specsVersion      % "test"     withSources() withJavadoc(),
    "org.specs2"            %% "specs2-scalacheck"            % specsVersion      % "test"     withSources() withJavadoc()
  )
}

initialCommands := "import com.blstream.twitterspark._"

Revolver.settings

scalariformSettings

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
   {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
   }
}
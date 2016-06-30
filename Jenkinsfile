#!groovy

node {
   // Mark the code checkout 'stage'....
   stage 'Checkout'

   // Get some code from a GitHub repository
   git url: 'https://github.com/BraintagsGmbH/vertx-util.git'

   // Get the maven tool.
   // ** NOTE: This 'M3' maven tool must be configured
   // **       in the global configuration.           
   def mvnHome = tool 'M3'
   env.PATH = "${mvnHome}/bin:${env.PATH}"

   // Mark the code build 'stage'....
   stage 'Build'

    wrap([$class: 'ConfigFileBuildWrapper', managedFiles: [[fileId: 'MAVEN_SETTINGS_GLOBAL_OSSHR', 
    	replaceTokens: false, targetLocation: 'settings.xml' ]]]) {
        //sh "echo $MAVEN_SETTINGS"
         sh "pwd"
         sh "mvn -s settings.xml -Dsign.skip=true  clean deploy"
    }


}
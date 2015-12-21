package com.blstream.twitterspark

import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder

object OAuthConfig {

  def someAuth = Some(new OAuthAuthorization(oAuthConfig))

  def oAuthConfig = {
    val cb = new ConfigurationBuilder()
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey("vOBB6Uy1UK4Dc0PozgxuJw7gl")
      .setOAuthConsumerSecret("fWCY4w3j5pei3XHZkcW4HxIqplmspH41hbeWBuXIUJfAYPkJ3Q")
      .setOAuthAccessToken("4514740876-ClomzW5XflRXEaU9t5nvwZV1M0ZEvzx8ZMdyhlD")
      .setOAuthAccessTokenSecret("Fd1CQIoU31SKkeHIdXZNcs4VmvU2tbyDw5OzXQpKKEyau")
    cb.build()
  }
}

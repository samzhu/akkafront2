package com.sam.server

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.sam.actor.PostsSender
import com.sam.routes.RestInterface
import com.typesafe.config.ConfigFactory

object AkkaHttpMicroservice extends App with RestInterface {

  override val config = ConfigFactory.parseString("akka.cluster.roles = [frontend]").
    withFallback(ConfigFactory.load())

  override implicit val system = ActorSystem("ClusterSystem", config)
  override implicit val postsSender = system.actorOf(Props[PostsSender], name = "frontend")

  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val logger = Logging(system, getClass)


  val port = config.getInt("http.port")
  val interface = config.getString("http.interface")

  val binding = Http().bindAndHandle(routes, interface, port)
  logger.info(s"Bound to port $port on interface $interface")
  binding onFailure {
    case ex: Exception ⇒
      logger.error(s"Failed to bind to $interface:$port!", ex)
  }
  sys.addShutdownHook(system.terminate())
}

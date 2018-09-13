package com.showtix

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

trait Startup extends RequestTimeout {
  def startup(api: Route)(implicit system: ActorSystem): Unit = {
    val host = system.settings.config.getString("http.host") // Gets the host and a port from the configuration
    val port = system.settings.config.getInt("http.port")
    startHttpServer(api, host, port)
  }

  def startHttpServer(api: Route, host: String, port: Int)(implicit system: ActorSystem): Unit = {
    implicit val ec: ExecutionContextExecutor = system.dispatcher  // bindingFuture.map requires an implicit ExecutionContext
    implicit val materializer: ActorMaterializer = ActorMaterializer()  // bindAndHandle requires an implicit materializer
    val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(api, host, port) // start HTTP server

    val log = Logging(system.eventStream, "show-tix")

    bindingFuture.map{ serverBinding ⇒ log.info(s"RestApi bound to ${serverBinding.localAddress}") }.onComplete {
      case Success(_) ⇒ log.info(s"Server successfully started at {}:{}", host, port)
      case Failure(e) ⇒
        log.error(e, "Failed to bind to {}:{}!", host, port)
        system.terminate()
    }
  }

}

package com.showtix

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContextExecutor, Future}

trait Startup extends RequestTimeout {
  def startup(api: Route)(implicit system: ActorSystem)= {
    val host = system.settings.config.getString("http.host") // Gets the host and a port from the configuration
    val port = system.settings.config.getInt("http.port")
    startHttpServer(api, host, port)
  }

  def startHttpServer(api: Route, host: String, port: Int)(implicit system: ActorSystem) = {
    implicit val ec: ExecutionContextExecutor = system.dispatcher  // bindingFuture.map requires an implicit ExecutionContext
    implicit val materializer: ActorMaterializer = ActorMaterializer()  // bindAndHandle requires an implicit materializer
    val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(api, host, port) // start HTTP server

    val log = Logging(system.eventStream, "show-tix")
    try {
      //    Here we start the HTTP server and log the info
      bindingFuture.map { serverBinding ⇒
        log.info(s"RestApi bound to ${serverBinding.localAddress}")
      }
    }catch {
      //    If the HTTP server fails to start, we throw an Exception and log the error and close the system
      case ex: Exception ⇒
        log.error(ex, "Failed to bind to {}:{}!", host, port)
        //      System shutdown
        system.terminate()
    }
  }

}

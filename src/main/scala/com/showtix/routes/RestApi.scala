package com.showtix.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.showtix.messages.Coachella._
import com.showtix.messages._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import StatusCodes._


class RestApi(system: ActorSystem, timeout: Timeout) extends RestRoutes {
  implicit val requestTimeout: Timeout = timeout
  implicit def executionContext: ExecutionContextExecutor = system.dispatcher

  def createCoachella(): ActorRef = system.actorOf(Coachella.props)
}

trait RestRoutes extends CoachellaApi with EventMarshaller {
  val service = "show-tix"
  val version = "v1"

//  endpoint for creating an event with tickets
  protected val createEventRoute: Route = {
    pathPrefix(service / version / "events" / Segment ) { event ⇒
      post {
//    POST show-tix/v1/events/event_name
        pathEndOrSingleSlash {
          entity(as[EventDescription]) { ed =>
            onSuccess(createEvent(event, ed.tickets)) {
              case Coachella.EventCreated(event) => complete(Created, event)
              case Coachella.EventExists =>
                val err = Error(s"$event event already exists!")
                complete(BadRequest, err)
            }
          }
        }
      }
    }
  }

  protected val getAllEventsRoute: Route = {
    pathPrefix(service / version / "events") {
      get {
        // GET show-tix/v1/events
        pathEndOrSingleSlash {
          onSuccess(getEvents()) { events ⇒
            complete(StatusCodes.OK, events)
          }
        }
      }
    }
  }

  protected val getEventRoute: Route = {
    pathPrefix(service / version / "events" / Segment) { event ⇒
      get {
        // GET show-tix/v1/events/:event
        pathEndOrSingleSlash {
          onSuccess(getEvent(event)) {
            _.fold(complete(NotFound))(e ⇒ complete(OK, e))
          }
        }
      }
    }
  }

  protected val deleteEventRoute: Route = {
    pathPrefix(service / version / "events" / Segment) { event ⇒
      delete {
        // DELETE show-tix/v1/events/:event
        pathEndOrSingleSlash {
          onSuccess(cancelEvent(event)) {
            _.fold(complete(NotFound))(e => complete(OK, e))
          }
        }
      }
    }
  }

  protected val purchaseEventTicketRoute: Route = {
    pathPrefix(service / version / "events" / Segment / "tickets") { event ⇒
      post {
        // POST show-tix/v1/events/:event/tickets
        pathEndOrSingleSlash {
          entity(as[TicketRequest]) { request ⇒
            onSuccess(requestTickets(event, request.tickets)) { tickets ⇒
              if (tickets.entries.isEmpty) complete(NotFound)
              else complete(Created, tickets)
            }
          }
        }
      }
    }
  }


  val routes: Route = createEventRoute ~ getAllEventsRoute ~ getEventRoute ~ deleteEventRoute ~ purchaseEventTicketRoute
}

trait CoachellaApi {

  def createCoachella(): ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  lazy val coachella: ActorRef = createCoachella()

  def createEvent(event: String, numberOfTickets: Int): Future[EventResponse] = {
    coachella.ask(CreateEvent(event, numberOfTickets))
      .mapTo[EventResponse]
  }

  def getEvents(): Future[Events] = coachella.ask(GetEvents).mapTo[Events]

  def getEvent(event: String): Future[Option[Event]] = coachella.ask(GetEvent(event)).mapTo[Option[Event]]

  def cancelEvent(event: String): Future[Option[Event]] = coachella.ask(CancelEvent(event)).mapTo[Option[Event]]

  def requestTickets(event: String, tickets: Int): Future[TicketSeller.Tickets] = {
    coachella.ask(GetTickets(event, tickets)).mapTo[TicketSeller.Tickets]
  }
}

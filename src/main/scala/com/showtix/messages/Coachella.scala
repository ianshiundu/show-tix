package com.showtix.messages

import akka.actor.Props
import akka.util.Timeout
import com.showtix.actors.Coachella

object Coachella {
  def props(implicit timeout: Timeout) = Props(new Coachella)

  case class CreateEvent(name: String, tickets: Int) // message to create an event
  case class GetEvent(name: String) // message to get an event
  case object GetEvents // message to request all events
  case class GetTickets(event: String, tickets: Int) // message to get tickets for an event
  case class CancelEvent(name: String) // message to cancel an event

  case class Event(name: String, tickets: Int) // message describing the event
  case class Events(events: Vector[Event]) // message describing a list of events

  sealed trait EventResponse // message response to create an event
  case class EventCreated(event: Event) extends EventResponse // message to indicate the event was created
  case object EventExists extends EventResponse // message to indicate that the event already exists
}





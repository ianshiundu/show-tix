package com.showtix.messages

import play.api.libs.json._
import com.showtix.messages.Coachella._
import de.heikoseeberger.akkahttpplayjson._

// message containing the initial number of tickets for the event
case class EventDescription(tickets: Int) {
  require(tickets > 0)
}

// message containing the required number of tickets
case class TicketRequest(tickets: Int) {
  require(tickets > 0)
}

// message containing an error
case class Error(message: String)

// convert our case classes from and to JSON
trait EventMarshaller extends PlayJsonSupport {

  implicit val eventDescriptionFormat: OFormat[EventDescription] = Json.format[EventDescription]
  implicit val ticketRequests: OFormat[TicketRequest] = Json.format[TicketRequest]
  implicit val errorFormat: OFormat[Error] = Json.format[Error]
  implicit val eventFormat: OFormat[Event] = Json.format[Event]
  implicit val eventsFormat: OFormat[Events] = Json.format[Events]
  implicit val ticketFormat: OFormat[TicketSeller.Ticket] = Json.format[TicketSeller.Ticket]
  implicit val ticketsFormat: OFormat[TicketSeller.Tickets] = Json.format[TicketSeller.Tickets]
}

object EventMarshaller extends EventMarshaller

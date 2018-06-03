package com.showtix.actors

import akka.actor.{Actor, PoisonPill}
import com.showtix.messages.Coachella

class TicketSeller(event: String) extends Actor {
  import com.showtix.messages.TicketSeller._
//  list of tickets
  var tickets = Vector.empty[Ticket]

  def receive: PartialFunction[Any, Unit] = {
// Adds the new tickets to the existing list of tickets when Tickets message is received
    case Add(newTickets) ⇒ tickets = tickets ++ newTickets

    case Buy(numberOfTickets) ⇒
      // Takes a number of tickets off the list
      val entries = tickets.take(numberOfTickets)

    if (entries.size >= numberOfTickets) {
// if there are enough tickets available, responds with a Tickets message containing the tickets
      sender() ! Tickets(event, entries)
      tickets = tickets.drop(numberOfTickets)
//   otherwise respond with an empty Tickets message
    } else sender() ! Tickets(event)
// returns an event containing the number of tickets left when GetEvent is received
    case GetEvent ⇒ sender() ! Some(Coachella.Event(event, tickets.size))

    case Cancel ⇒ sender() ! Some(Coachella.Event(event, tickets.size))
      self ! PoisonPill
  }
}

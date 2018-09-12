package com.showtix.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.showtix.StopSystemAfterAll
import com.showtix.messages.Coachella
import com.showtix.messages.Coachella._
import com.showtix.messages.TicketSeller._
import org.scalatest.{MustMatchers, WordSpecLike}

class CoachellaSpec extends TestKit(ActorSystem("testBoxOffice"))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with DefaultTimeout
  with StopSystemAfterAll {
  "Coachella" must {

    "Create an event and get tickets from the correct Ticket Seller" in {

      val coachella = system.actorOf(Coachella.props)
      val eventName = "RHCP"
      coachella ! CreateEvent(eventName, 10)
      expectMsg(EventCreated(Event(eventName, 10)))

      coachella ! GetEvents
      expectMsg(Events(Vector(Event(eventName, 10))))

      coachella ! Coachella.GetEvent(eventName)
      expectMsg(Some(Event(eventName, 10)))

      coachella ! GetTickets(eventName, 1)
      expectMsg(Tickets(eventName, Vector(Ticket(1))))

      coachella ! GetTickets("DavidBowie", 1)
      expectMsg(Tickets("DavidBowie"))
    }

    "Create a child actor when an event is created and sends it a Tickets message" in {
      val coachella = system.actorOf(Props(
        new Coachella  {
          override def createTicketSeller(name: String): ActorRef = testActor
        }
      )
      )

      val tickets = 3
      val eventName = "RHCP"
      val expectedTickets = (1 to tickets).map(Ticket).toVector
      coachella ! CreateEvent(eventName, tickets)
      expectMsg(Add(expectedTickets))
      expectMsg(EventCreated(Event(eventName, tickets)))
    }

    "Get and cancel an event that is not created yet" in {
      val coachella = system.actorOf(Coachella.props)
      val noneExitEventName = "noExitEvent"
      coachella ! Coachella.GetEvent(noneExitEventName)
      expectMsg(None)

      coachella ! CancelEvent(noneExitEventName)
      expectMsg(None)
    }

    "Cancel a ticket which event is not created " in {
      val coachella = system.actorOf(Coachella.props)
      val noneExitEventName = "noExitEvent"

      coachella ! CancelEvent(noneExitEventName)
      expectMsg(None)
    }

    "Cancel a ticket which event is created" in {
      val coachella = system.actorOf(Coachella.props)
      val eventName = "RHCP"
      val tickets = 10
      coachella ! CreateEvent(eventName, tickets)
      expectMsg(EventCreated(Event(eventName, tickets)))

      coachella ! CancelEvent(eventName)
      expectMsg(Some(Event(eventName, tickets)))
    }
  }


}

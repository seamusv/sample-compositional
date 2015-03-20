package com.vngrs.proto
package ragnar
package actor

import logic._

import akka.actor.ActorRef
import akka.pattern.pipe
import akka.util.Timeout

trait Server extends BaseActor { def service: Service }
object Server {

  class Contained(val service: Service) extends Server {

    override def receive: Receive = propagate orElse unknown

    def propagate: Receive = service.parse andThen { res =>
      implicit val ec = context.dispatcher
      val target = sender()
      service.execute(ec)(res) pipeTo target
    }

  }
  object Contained

}

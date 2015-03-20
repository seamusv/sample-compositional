package com.vngrs.proto
package ragnar
package actor

import logic._

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.util.Timeout

object LogicActorTestHelpers {

  abstract class LogicActorScope extends ServiceTestHelpers.ProxyActorScope {
    def server(s: Service)(implicit as: ActorSystem) = as.actorOf(Props(classOf[Server.Contained], s))
    def propagator(ar: ActorRef, tout: Timeout, name: String = "test-propagator")(implicit as: ActorSystem) =
      as.actorOf(Props(classOf[Propagator.Contained], name, ar, tout))
  }

}

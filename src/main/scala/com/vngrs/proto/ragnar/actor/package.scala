package com.vngrs.proto
package ragnar

package object actor {

  trait BaseActor extends akka.actor.Actor with akka.actor.ActorLogging {
    def unknown: Receive = { case _ => sender ! Message.Unknown }
  }
  object BaseActor

  trait Message
  object Message {

    case object Unknown

  }

}

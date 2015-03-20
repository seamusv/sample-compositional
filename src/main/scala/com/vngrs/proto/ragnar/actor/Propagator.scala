package com.vngrs.proto
package ragnar
package actor

import logic._

trait Propagator extends Server { def target: akka.actor.ActorRef }
object Propagator {

  class Contained(name: String, target: akka.actor.ActorRef, within: akka.util.Timeout)
    extends Server.Contained(new Proxy(name, target, within))

}

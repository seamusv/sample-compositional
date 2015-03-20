package com.vngrs.proto
package ragnar
package actor

import common._
import test._

import akka.pattern.ask
import scala.concurrent.duration._

class PropagatorTest extends ActorTestHelpers.AkkaTest with Things {
  import CommonTestHelpers._
  import LogicActorTestHelpers._
  import logic.ServiceTestHelpers._

  "Propagator" in {

    "Contained" should {
      implicit def tout: akka.util.Timeout = 1 second
      val expRes = Done(something)
      val badRes = Fail(-1)

      "back propagate success" >> new LogicActorScope {
        val res = upon(propagator(reflector, tout) ? expRes)
        res must beEqualTo(expRes)
      }

      "back propagate failure" >> new LogicActorScope {
        val res = upon(propagator(reflector, tout) ? badRes)
        res must beEqualTo(badRes)
      }

      "send back unknown for non-Result" >> new LogicActorScope {
        val res = upon(propagator(reflector, tout) ? something)
        res must beEqualTo(logic.Proxy.unknownRes)
      }

    }
  }
}

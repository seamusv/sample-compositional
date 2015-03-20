package com.vngrs.proto
package ragnar
package actor

import test._

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit, TestProbe }

import org.specs2.specification.Scope
import org.specs2.mutable.After
import org.specs2.time.NoTimeConversions

object ActorTestHelpers {

  trait AkkaTest extends UnitTest with NoTimeConversions
  object AkkaTest

  abstract class ActorScope extends TestKit(ActorSystem()) with After with Scope with ImplicitSender {
    def after = system.shutdown()
    def probe = TestProbe()
  }
  object ActorScope

}

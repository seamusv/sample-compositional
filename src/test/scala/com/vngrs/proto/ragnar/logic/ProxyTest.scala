package com.vngrs.proto
package ragnar
package logic

import common._
import test._

import scala.concurrent.duration._

class ProxyTest extends UnitTest with Things with org.specs2.time.NoTimeConversions {
  import CommonTestHelpers._
  import ServiceTestHelpers._

  "Proxy" in {

    "execute" should {
      val expRes = Done(something)
      val badRes = Fail(-1)

      "convert any to Propagation" in {
        val req = something
        val tout = 1 second
        val prox = Proxy("name", mockActor, tout)
        prox.parse(req) must beEqualTo(Proxy.Propagation(req, tout))
      }

      "return success as-is" >> new ProxyActorScope {
        implicit val exCon = ExCon.global
        val prox = proxy(reflector, 1 second)
        val resp = upon(prox.execute(exCon)(task(expRes)))
        resp must beEqualTo(expRes)
      }

      "return failure as-is" >> new ProxyActorScope {
        implicit val exCon = ExCon.global
        val prox = proxy(reflector, 1 second)
        val resp = upon(prox.execute(exCon)(task(badRes)))
        resp must beEqualTo(badRes)
      }

      "return unknown for non-Result" >> new ProxyActorScope {
        implicit val exCon = ExCon.global
        val prox = proxy(reflector, 1 second)
        val resp = upon(prox.execute(exCon)(task(something)))
        resp must beEqualTo(Proxy.unknownRes)
      }

    }
  }
}

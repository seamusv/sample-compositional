package com.vngrs.proto
package ragnar
package actor

import common._
import test._

import scala.concurrent.duration._

class ServerTest extends ActorTestHelpers.AkkaTest with Things {
  import CommonTestHelpers._
  import LogicActorTestHelpers._
  import logic.ServiceTestHelpers._

  "Server" in {

    "Contained" should {
      val cont = 5
      val func: Int => Int = _ * 2
      val serv = spiedService(cont)
      val req = mockReq(func)
      val bad = mockReq(func, wrongDomain)
      val res = Done(func(cont))

      "complete legal request" >> new LogicActorScope {
        server(serv) ! req
        expectMsg(1 second, res)
      }

      "fail for bad request" >> new LogicActorScope {
        server(serv) ! bad
        expectMsg(1 second, denyMes)
      }

      "return unknown for irrelevant request" >> new LogicActorScope {
        server(serv) ! something
        expectMsg(1 second, Message.Unknown)
      }

    }
  }
}

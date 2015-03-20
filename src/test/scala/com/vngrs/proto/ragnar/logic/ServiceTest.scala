package com.vngrs.proto
package ragnar
package logic

import common._
import test._

class ServiceTest extends UnitTest {
  import CommonTestHelpers._
  import ServiceTestHelpers._

  val ec: ExCon = ExCon.Implicits.global

  "Service" in {

    "Default Validators" in {
      "allowAll" should ("allow any request" in valid(allowAll, Done(true)))
      "denyAll" should ("deny all requests" in valid(denyAll, Fail(1)))
      "invalidAll" should ("invalidate all requests" in valid(invalidAll, Fail(-1)))
      "unknownOper" should ("return unknown" in (upon(unknownOper(mockReq)) must beEqualTo(Fail(-1))))

    }

    "Steps" in {
      val cont = 5
      val func: Int => Int = _ * 2
      val req = mockReq(func)
      val bad = mockReq(func, wrongDomain)
      val res = Done(func(cont))
      val serv = spiedService(cont)

      "interpret" should { "extract request" in (serv.interpret.lift(req) must beSome) }
      "validate" should {
        "allow legal request" in valid(serv.validate, allowMes, mockTask(rightDomain, None, req))
        "reject illegal request" in valid(serv.validate, denyMes, mockTask(wrongDomain, None, req))
      }
      "process" in { "carry out request" in (upon(serv.process.execute(spiedTask(mockReq(func)))) must beEqualTo(res)) }
      "execute" in {
        "parse and carry out validated request" in (upon(serv.execute(ec)(req)) must beEqualTo(res))
        "parse and reject invalidated request" in (upon(serv.execute(ec)(bad)) must beEqualTo(denyMes))
      }
    }
  }
}

package com.vngrs.proto
package ragnar
package common

import test._

class PhaseTest extends UnitTest  {
  import CommonTestHelpers._

  "Phase" in {

    "merge(Vald, Vald)" in {
      "merged 'selective' and 'allowAll' " should {
        val merged = Phase.merge(selective, allowAll)

        "allow legal" in { valid(merged, allowMes, mockTask(rightDomain, None)) }
        "disallow illegal" in { valid(merged, denyMes, mockTask(wrongDomain, None)) }
      }
      "merged selective and denyAll" should {
        val merged = Phase.merge(selective, denyAll)

        "disallow legal" in { valid(merged, denyMes, mockTask(rightDomain, None)) }
        "disallow illegal" in { valid(merged, denyMes, mockTask(wrongDomain, None)) }
      }
    }

    "merge(Step, Step)" in {
      "merged steps" should {
        val zero = 0
        val even = 4
        val odd = 3
        val res = Done(zero)
        val i2s = spiedStep(intToString, isEven)
        val s2i = spiedStep(stringToInt, isZeroStr)
        val merged = Phase.merge(i2s, s2i)

        "execute subsequently" in (upon(merged.execute(zero)) must beEqualTo(res))
        "fail when first fails" in (upon(merged.execute(odd)) must beEqualTo(failedMes))
        "fail when second fails" in (upon(merged.execute(even)) must beEqualTo(failedMes))

      }
    }

    "guard(Vald, Step)" in {
      "guarded step" should {
        val cont = 5
        val func: Int => Int = _ * 2
        val res = Done(func(cont))
        val vald = selective
        val task = spiedTask(mockReq(func))
        val badt = spiedTask(mockReq(func), wrongDomain)
        val exec = spiedExec[Int, Int](cont)
        val guarded = Phase.guard(vald, exec)

        "execute for legal task" in (upon(guarded.execute(task)) must beEqualTo(res))
        "not execute for illegal task" in (upon(guarded.execute(badt)) must beEqualTo(denyMes))

      }
    }
  }

}

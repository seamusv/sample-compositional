package com.vngrs.proto
package ragnar
package logic

import common._
import test._

import scala.collection.mutable.{ Map => MMap }

class DictionaryTest extends UnitTest with Things {

  import CommonTestHelpers._
  import ServiceTestHelpers._

  "Dictionary" in {

    "operators" in {
      val pairs = Seq(("a", something), ("b", something), ("c", something))
      def dictFor[A]: (Seq[(String, A)]) => MMap[String, A] = MMap(_:_*)
      def cont[A]: MMap[String, A] => Cont[MMap[String, A]] = new TestContext("some-context", _)

      "Size" should {

        "return size of dictionary" in {
          val dict = dictFor[Any](pairs)
          upon(Dictionary.Size.execute(cont(dict))) must beEqualTo(Done(pairs.size))
        }
      }

      "HasKey" should {
        val dict = dictFor[Any](pairs)
        def hasKey(key: String) = upon(Dictionary.HasKey(key).execute(cont(dict)))

        "return true for existing key" in (hasKey(pairs(0)._1) must beEqualTo(Done(true)))
        "return false for non-existent key" in (hasKey("something") must beEqualTo(Done(false)))
      }

      "HasValue" should {
        val dict = dictFor[Any](pairs)
        def hasVal(value: Any) = upon(Dictionary.HasValue(value).execute(cont(dict)))

        "return true for existing value" in (hasVal(pairs(0)._2) must beEqualTo(Done(true)))
        "return false for non-existent value" in (hasVal("something") must beEqualTo(Done(false)))
      }

      "Get" should {
        val dict = dictFor[Any](pairs)
        def get(key: String) = upon(Dictionary.Get[Any](key).execute(cont(dict)))

        "get value for existing key" in (get(pairs(0)._1) must beEqualTo(Done(Some(pairs(0)._2))))
        "fail for non-existent key" in (get("something") must beEqualTo(Done(None)))

      }

      "Add" should {
        def add(key: String, dict: MMap[String, Any]) = upon(Dictionary.Add[Any]((key, something)).execute(cont(dict)))

        "add new pair for key" in {
          val dic = dictFor[Any](pairs)
          val res = add("something", dic)
          "result is Done(true)" in (res must beEqualTo(Done(true)))
          "dictionary mutated" in (dic.contains("something") must beTrue)
        }
        "fail for already existing key" in {
          val old = dictFor[Any](pairs)
          val now = old.clone()
          val res = add(pairs(0)._1, now)
          "result is false" in (res must beEqualTo(Done(false)))
          "dictionary is unchanged" in (now must beEqualTo(old))
        }

      }

      "Remove" should {
        def remove(key: String, dict: MMap[String, Any]) = upon(Dictionary.Remove(key).execute(cont(dict)))

        "remove existing pair for key" in {
          val dic = dictFor[Any](pairs)
          val res = remove(pairs(0)._1, dic)
          "result is true" in (res must beEqualTo(Done(true)))
          "dictionary mutated" in (dic.contains(pairs(0)._1) must beFalse)
        }

        "fail for non-existent key" in {
          val old = dictFor[Any](pairs)
          val now = old.clone()
          val res = remove("something", now)
          "result is false" in (res must beEqualTo(Done(false)))
          "dictionary is unchanged" in (now must beEqualTo(old))
        }
      }

    }
  }
}

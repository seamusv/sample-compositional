package com.vngrs.proto
package ragnar
package logic

import data._
import common._

import scala.collection.generic.{ Growable, Shrinkable }

case class Dictionary[A](name: String, dict: Dictionary.DictMut[A])
  extends Service.Contained[Dictionary.DictMut[A]](dict) with Service.AutoTasking.NativeOps {
  import Dictionary._

  override type R[-X, +Y] = DirectoryOp[X, Y]
  override def parse: PF[Any, R[T, Any]] = {
    case Size => Size
    case r: HasKey => r
    case r: HasValue[A] => r
    case r: Get[A] => r
    case r: Add[A] => r
    case r: Remove => r
  }

}
object Dictionary {

  type Dict[+A] = Col[(String, A)] with PF[String, A]
  type DictGrw[A] = Growable[(String, A)] with PF[String, A]
  type DictRed[A] = Shrinkable[String] with PF[String, A]
  type DictMut[A] = Dict[A] with DictGrw[A] with DictRed[A]

  sealed trait DirectoryOp[-A, +B] extends Operator[A, B] with Req[A, B]

  private def size = (t: Col[Any]) => Done(t.size)

  private def hasKey(key: String) = (t: Dict[Any]) => Done(t.isDefinedAt(key))
  
  private def hasValue[A](elem: A) = (t: Col[(String, A)]) => Done(t.exists(kv => elem.equals(kv._2)))

  private def get[A](key: String) = (t: Dict[A]) => Done(t.lift(key))
  
  private def add[A](key: String, value: A) = (t: DictGrw[A]) =>
    if (t.isDefinedAt(key)) Done(false)
    else { t += ((key, value)); Done(true) }

  private def remove(key: String) = (t: DictRed[Any]) =>
    if (t.isDefinedAt(key)) { t -= key; Done(true) } else Done(false)

  abstract class Abstract[-A, +B](func: A => Res[B])
    extends Operator.Closed[A, B](func) with DirectoryOp[A, B]
  object Abstract

  case object Size extends Abstract[Col[Any], Int](size)

  case class HasKey(key: String) extends Abstract[Dict[Any], Boolean](hasKey(key))
  object HasKey

  case class HasValue[A](value: A) extends Abstract[Col[(String, A)], Boolean](hasValue(value))
  object HasValue

  case class Get[A](key: String) extends Abstract[Dict[A], Opt[A]](get(key))
  object Get

  case class Add[A](tuple: (String, A)) extends Abstract[DictGrw[A], Boolean](add(tuple._1, tuple._2))
  object Add

  case class Remove(key: String) extends Abstract[DictRed[Any], Boolean](remove(key))
  object Remove

}

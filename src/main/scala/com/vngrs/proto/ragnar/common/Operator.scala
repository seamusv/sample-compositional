package com.vngrs.proto
package ragnar
package common

trait Operator[-A, +B] extends Oper[A, B]
object Operator {

  sealed trait Effect
  object Effect {

    case object Read extends Effect
    case object Write extends Effect
    case object Execute extends Effect

  }

  class Closed[-A, +B](func: A => Res[B]) extends Operator[A, B] {
    override def execute(context: Cont[A])(implicit exCon: ExCon): Fut[Res[B]] = context.value.map(func)
  }
  object Closed

}

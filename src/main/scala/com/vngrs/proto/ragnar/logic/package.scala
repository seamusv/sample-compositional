package com.vngrs.proto
package ragnar

import data._
import common._

package object logic {

  val (allowMes, denyMes, invalidMes) = (Done(true), Fail(1), Fail(-1))
  val (allow, deny, invalid) = {
    (Fut.successful(allowMes), Fut.successful(denyMes), Fut.successful(invalidMes))
  }
  val (allowAll, denyAll, invalidAll) = {
    def toVald: Fut[Res[Boolean]] => Vald = res => new Vald { def execute(t: AnyTask)(implicit ex: ExCon) = res }
    (toVald(allow), toVald(deny), toVald(invalid))
  }

  val unknown: Fut[Res[Nothing]] = invalid
  val unknownOper: PF[Req[Nothing, Any], Fut[Res[Nothing]]] = { case _ => unknown }

  type Inpt[A, B, R <: Req[A, B]] = PF[R, Task[A, B]]
  type Proc[A, B, R <: Req[A, B]] = PF[R, Fut[Res[B]]]

  trait Service {
    import Implicits._

    type T
    type R[-X, +Y] <: Req[X, Y]

    def parse: PartialFunction[Any, R[T, Any]]

    protected def context: Cont[T]
    protected def interpret[B]: Inpt[T, B, R[T, B]] = PartialFunction.empty

    protected def validate: Vald = allowAll
    protected def process[B]: Exec[T, B] = new Exec[T, B] {
      override def execute(in: Task[T, B])(implicit exCon: ExCon) = in.op.execute(context)
    }

    def execute[B](implicit exCon: ExCon): Proc[T, B, R[T, B]] =
      (interpret[B] andThen (validate guards process[B]).execute) orElse unknownOper

  }
  object Service {

    trait AutoTasking extends Service {
      this: Service =>

      protected def domain = context
      protected def extUser: PF[R[T, _], User] = AutoTasking.userExtractor
      protected def bind[B]: PF[R[T, B], (Opt[User], Oper[T, B])] = PF.empty
      protected final def pack[B]: PF[(Opt[User], Oper[T, B]), Task[T, B]] = {
        case (user: Opt[User], op: Oper[T, B]) => Task[T, B](domain, user, op)
      }

      protected abstract override def interpret[B] = super.interpret[B] orElse (bind[B] andThen pack[B])

    }
    object AutoTasking {

      def userExtractor[R[-X, +Y] <: Req[X, Y]]: PF[R[Nothing, _], User] = { case Owned(user) => user }
      private[this] def userOptExtractor[R[-X, +Y] <: Req[X, Y]]: R[Nothing, _] => Opt[User] = userExtractor.lift

      trait NativeOps extends AutoTasking {

        type R[-X, +Y] <: Req[X, Y] with Oper[X, Y]

        protected override abstract def bind[B]: PF[R[T, B], (Opt[User], Oper[T, B])] = super.bind[B] orElse {
          case oper => (userOptExtractor(oper), oper)
        }

      }
      object NativeOps

    }

    abstract class Contained[A](v: A)
      extends Service with Context[A] {

      override type T = A

      final val value = Fut.successful(v)
      final val context: Context[T] = this

    }
    object Contained

  }

  object Implicits {

    implicit class PhaseWrapper[A, B](val value: Step[A, B]) extends AnyVal {
      def merge[C](next: Step[B, C], handler: Hand[C]): Step[A, C] = Phase.merge(value, next, handler)
    }
    object PhaseWrapper

    implicit class ValidationWrapper[A, B](val value: Vald) extends AnyVal {

      def and(next: Vald): Vald = Phase.merge(value, next)
      def guards(next: Exec[A,  B], handler: Hand[B] = Phase.propagate): Exec[A, B] = Phase.guard(value, next, handler)

    }
    object ValidationWrapper

  }

}

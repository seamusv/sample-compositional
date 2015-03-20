package com.vngrs.proto
package ragnar

import data._

import scala.language.{ existentials, higherKinds, reflectiveCalls}

package object common {

  def trivial[A]: A => A = a => a

  val future = scala.concurrent.Future
  val promise = scala.concurrent.Promise
  def upon[A] = scala.concurrent.Await.result[A] _

  type PF[-A, +B] = PartialFunction[A, B]
  val PF = PartialFunction
  type Opt[+A] = Option[A]
  val Opt = Option
  type Fut[+A] = scala.concurrent.Future[A]
  val Fut = scala.concurrent.Future
  type ExCon = scala.concurrent.ExecutionContext
  val ExCon = scala.concurrent.ExecutionContext

  type Res[+A] = Result[A]
  val Res = Result

  type Step[-A, +B] = Phase[A, B]
  type Domn = AnyRef with Namd
  type Cont[+A] = Context[A]
  type Oper[-A, +B] = Step[Cont[A], B]

  type Exec[A, B] = Step[Task[A, B], B]
  type Hand[+A] = Step[Fail, A]
  type Vald = Step[AnyTask, Boolean]
  type AnyTask = Task[_, Any]

  val (allowMes, denyMes, invalidMes) = (Done(true), Fail(1), Fail(-1))
  val (allow, deny, invalid) = {
    (Fut.successful(allowMes), Fut.successful(denyMes), Fut.successful(invalidMes))
  }
  val (allowAll, denyAll, invalidAll) = {
    def toVald: Fut[Res[Boolean]] => Vald = res => new Vald { def execute(t: AnyTask)(implicit ex: ExCon) = res }
    (toVald(allow), toVald(deny), toVald(invalid))
  }

  trait Context[+A] {
    def name: String
    def value: Fut[A]
  }
  object Context

  type Req[-A, +B] = Request[A, B]
  val Req = Request

  trait Request[-A, +B]
  object Request {

    trait Query[-A, +B] extends Request[A, B]
    object Query

    trait Command[-A] extends Request[A, Nothing]
    object Command

  }

  sealed trait Result[+A] extends Serializable {

    def success: Boolean
    def message: Opt[String]

  }
  object Result

  case class Done[+A](data: A, message: Opt[String]) extends (() => A) with Result[A] {
    def apply() = data
    def success = true
  }
  object Done {
    def apply[A](data: A): Done[A] = Done(data, None)
    def apply[A](data: A, message: String): Done[A] = Done(data, Some(message))
  }

  case class Fail(code: Int, message: Opt[String]) extends Result[Nothing] {
    def success = false
  }
  object Fail {
    def apply(code: Int): Fail = Fail(code, None)
    def apply(code: Int, message: String): Fail = Fail(code, Some(message))
  }

  case class Task[A, +B](domain: Domn, user: Opt[User], op: Oper[A, B])
  object Task

  trait Phase[-A, +B] {
    def execute(in: A)(implicit exCon: ExCon): Fut[Res[B]]
  }
  object Phase {

    val propagate: Hand[Nothing] = new Hand[Nothing] {
      def execute(f: Fail)(implicit exCon: ExCon) = Fut.successful(f)
    }

    def merge(head: Vald, next: Vald): Vald = {
      new Vald {
        def execute(task: Task[_, Any])(implicit ec: ExCon): Fut[Res[Boolean]] = {
          head.execute(task) flatMap {
            case Done(true, _) => next.execute(task)
            case Done(false, _) => invalid
            case f: Fail => Fut.successful(f)
          }
        }
      }
    }

    def merge[A, B, C](head: Step[A, B], next: Step[B, C], h: Hand[C] = propagate): Phase[A, C] = new Phase[A, C] {
      def execute(context: A)(implicit exCon: ExCon) = {
        head.execute(context).flatMap {
          case Done(value, _) => next.execute(value)
          case f: Fail => h.execute(f)
        }
      }
    }

    def guard[A, B](head: Vald, next: Exec[A, B], handler: Hand[B] = propagate): Exec[A, B] = {
      new Exec[A, B] {
        def execute(task: Task[A, B])(implicit ec: ExCon): Fut[Res[B]] = {
          head.execute(task) flatMap {
            case Done(true, _) => next.execute(task)
            case Done(false, _) => invalid
            case f: Fail => Fut.successful(f)
          }
        }
      }
    }

  }

}

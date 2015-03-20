package com.vngrs.proto
package ragnar
package common

import data._
import test._

object CommonTestHelpers {
  import Mocks._

  val someContext = "some-context"
  val rightDomain = "right-domain"
  val wrongDomain = "wrong-domain"

  val stringToInt: String => Int = s => Integer.parseInt(s): Int
  val intToString: Int => String = i => i.toString

  val isEven: Int => Boolean = i => (i % 2) == 0
  val isOdd: Int => Boolean = !isEven(_)
  val isZero: Int => Boolean = i => i == 0
  val isZeroStr: String => Boolean = stringToInt andThen isZero
  val badGuard = (a: Any) => false

  val selective = new Vald {
    def execute(t: AnyTask)(implicit ex: ExCon) = t.domain.name match {
      case `rightDomain` => allow
      case _ => deny
    }
  }

  val failedMes = Fail(2)
  val failed = Fut.successful(failedMes)
  val wrongType = Fut.successful(Fail(-1))

  def upon[A](a: scala.concurrent.Awaitable[A]) = common.upon(a, scala.concurrent.duration.DurationInt(1).second)
  def valid(vald: Vald, res: Res[Boolean], t: AnyTask = mockTask()) = upon(vald.execute(t)(ExCon.global)) == res

  def mockPhase[A : scala.reflect.ClassTag]: Phase[A, A] = mockPhase[A, A](trivial)
  def mockPhase[A : scala.reflect.ClassTag, B](func: A => B): Phase[A, B] = {
    val phase = mock[Phase[A, B]]
    phase.execute(any[A])(any[ExCon]) answers ({
      case a: A => Fut.successful(Done(func(a)))
      case _ => wrongType
    }: Any => Fut[Res[B]])
    phase
  }

  def mockReq[A, B] = mock[Req[A, B]]
  def mockReq[A : scala.reflect.ClassTag, B](func: A => B, domain: String = rightDomain) =
    new TestReq(func, domain)

  def mockOp[A]: Oper[A, A] = mockOp(trivial)
  def mockOp[A, B](func: A => B): Oper[A, B] = {
    val oper = mock[Oper[A, B]]
    oper.execute(any[Cont[A]])(any[ExCon]) answers ({
      case c: Cont[A] => c.value.map(a => Done(func(a)))(ExCon.global)
      case f => println(f); wrongType
    }: Any => Fut[Res[B]])
    oper
  }

  def mockTask[A](domain: String = rightDomain, user: Opt[User] = None): Task[A, A] = mockTask(domain, user, trivial)
  def mockTask[A, B](domain: String, user: Opt[User], func: A => B): Task[A, B] = {
    val op = mockOp(func)
    val dom = new { def name = domain }
    val task = spy(new Task(dom, user, op))
    task.domain returns dom
    task.user returns user
    task.op returns op
    task
  }

  def mockContext[A](cont: A) = {
    val context = mock[Cont[A]]
    context.name returns someContext
    context.value returns Fut.successful(cont)
    context
  }

  def spiedStep[A, B](func: A => B, guard: A => Boolean = (a: A) => true): Step[A, B] = spy(new TestStep(func, guard))
  def spiedExec[A, B](value: A): Exec[A, B] = new TestProcess[A, B](value)
  def spiedTask[A, B](oper: Oper[A, B], domain: String = rightDomain, user: Opt[User] = None) =
    spy(new Task(new { def name = domain }, user, oper))

  class TestReq[-T, +B](func: T => B, val domain: String = rightDomain)
    extends Req[T, B] with Oper[T, B] with (T => B) {

    override def apply(t: T): B = func(t)
    override def execute(in: Cont[T])(implicit exCon: ExCon): Fut[Res[B]] = in.value.map(t => Done(func(t)))

  }
  object TestReq

  class TestProcess[A, B](value: A) extends Exec[A, B] {
    override def execute(in: Task[A, B])(implicit exCon: ExCon) = in.op.execute(mockContext(value))
  }
  object TestProcess

  class TestStep[A, B](func: A => B, guard: A => Boolean = (a: A) => true) extends Step[A, B] {
    def execute(in: A)(implicit exCon: ExCon): Fut[Res[B]] = if (!guard(in)) failed else Fut.successful(Done(func(in)))
  }
  object TestStep

}

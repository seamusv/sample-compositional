package com.vngrs.proto
package ragnar
package logic

import common._
import test._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.util.Timeout
import scala.concurrent.duration._

object ServiceTestHelpers {
  import Mocks._
  import CommonTestHelpers._
  import actor.ActorTestHelpers._

  implicit def durationToTimeout(duration: Duration): Timeout = Timeout(duration._1, duration._2)
  def mockActor = mock[ActorRef]
  def spiedService[A](cont: A, vald: Vald = selective) = spy(new TestService(cont, vald))

  class Reflector extends Actor with org.specs2.control.Debug {
    def receive = { case msg => sender().tell(msg, self) }
  }
  object Reflector

  abstract class ProxyActorScope extends ActorScope {
    def task(res: Any) = Proxy.Propagation(res, 1 second)
    def reflector = system.actorOf(Props[Reflector])
    def proxy(ar: ActorRef, tout: Timeout, name: String = "proxy")(implicit as: ActorSystem) = Proxy(name, ar, tout)
  }
  object ProxyActorScope

  class TestContext[A](val name: String, val value: Fut[A]) extends Cont[A] {
    def this(name: String, some: A) = this(name, Fut.successful(some))
  }
  object TestContext

  class TestService[A](cont: A, vald: Vald = selective) extends Service {

    override type T = A
    override type R[-T, +B] = TestReq[T, B]
    override def interpret[B] = { case req => Task[T, B](new { def name = req.domain }, None, req) }
    override def context: Cont[T] = new TestContext(someContext, cont)
    override def validate = vald
    override def process[B] = super.process[B]
    override def parse: PF[Any, R[T, Any]] = {
      case c: TestReq[T, Any] => c
    }
  }
  object TestService

}

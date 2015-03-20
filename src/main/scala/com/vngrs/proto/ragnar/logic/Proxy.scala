package com.vngrs.proto
package ragnar
package logic

import common._

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

case class Proxy(name: String, target: ActorRef, timeout: Timeout)
  extends Service.Contained(target) with Service.AutoTasking.NativeOps {
  import Proxy._
  override type R[-X, +Y] = Req[X, Y] with Oper[X, Y]
  override def parse: PF[Any, R[ActorRef, Any]] = { case mes => Propagation(mes, timeout) }
}
object Proxy {

  val unknownRes = Fail(-5)

  case class Propagation[-X, +Y](mes: Any, implicit val timeout: Timeout)
    extends Req[ActorRef, Any] with Oper[ActorRef, Any] {
    override def execute(in: Cont[ActorRef])(implicit exCon: ExCon): Fut[Res[Any]] = in.value.flatMap {
      target => (target ? mes).map {
          case d: Done[Any] => d
          case f: Fail => f
          case _ => unknownRes
        }

    }
  }
}

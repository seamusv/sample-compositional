package com.vngrs.proto
package ragnar
package actor

import common._

import akka.actor._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{ Success, Failure }

class Registrar(factory: Registrar.ActFac) extends Actor {
  import Registrar._

  implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case ChildExists(name) => execChildExists(name, sender())
    case ChildForName(name) => execChildForName(name, sender())
  }

  protected def execChildExists(name: String, target: ActorRef) =
    target ! Done(context.child(name).isDefined)

  protected def execChildForName(name: String, target: ActorRef) =
    context.child(name).map(a => Fut.successful(Done(a))).getOrElse(factory(name, context)) onComplete {
      case Success(d: Done[_]) => target ! d
      case Success(f: Fail) => target ! f
      case Failure(e) => target ! Fail(-5)
    }

}
object Registrar {

  type ActFac = (String, ActorContext) => Fut[Res[ActorRef]]

  implicit val timeout = 1 second

  case class ChildExists(name: String)
  object ChildExists

  case class ChildForName(name: String)
  object ChildForName

}

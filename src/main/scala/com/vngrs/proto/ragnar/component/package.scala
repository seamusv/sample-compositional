package com.vngrs.proto
package ragnar

import common._

package object component {

  trait Component extends data.Named {

    def name: String
    def init: Component.Init
    def role: Component.Role

  }
  object Component {

    type Parm = (akka.actor.ActorSystem, Opt[akka.actor.ActorRef])
    type Init = Parm => Parm

    val facName = "facade"
    val logName = "logic"
    val repName = "repository"
    val standln = "standalone"

    sealed trait Role extends data.Named
    object Role {

      abstract class Abstract(val name: String) extends Role
      case object Repository extends Abstract(repName)
      case object Facade extends Abstract(facName)
      case object Logic extends Abstract(logName)
      case object Standalone extends Abstract(standln)

    }

    sealed abstract class Concrete(val role: Role) extends Component
    object Concrete {

      case class Facade(name: String, init: Init) extends Concrete(Role.Facade)
      object Facade { def unapply(comp: Component) = comp.role == Role.Facade }

      case class Logic(name: String, init: Init) extends Concrete(Role.Logic)
      object Logic { def unapply(comp: Component) = comp.role == Role.Logic }

      case class Repository(name: String, init: Init) extends Concrete(Role.Repository)
      object Repository { def unapply(comp: Component) = comp.role == Role.Repository }

      case class Standalone(name: String, facade: Facade, logic: Logic, repo: Repository)
        extends Concrete(Role.Standalone) {
        override def init: Init = repo.init andThen logic.init andThen facade.init
      }
      object Standalone { def unapply(comp: Component) = comp.role == Role.Standalone }

    }

  }
}

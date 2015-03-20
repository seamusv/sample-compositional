package com.vngrs.proto
package ragnar

package object data {

  type ID = Long

  type User = Enty with Namd
  type Enty = { def id: ID }
  type Namd = { def name: String }
  type Desc = { def description: String }
  type Ownd = { def user: Option[User] }

  type Col[+A] = Traversable[A]

  trait Owned { def user: Option[User] }
  object Owned {
    def unapply(owned: Owned) = owned.user
  }

  trait Named { def name: String }
  object Named { def unapply(named: Named) = named.name }

}

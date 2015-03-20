package com.vngrs.proto
package ragnar
package logic

import common._

trait Validator {
  this: Service =>
  protected def validate: Vald = allowAll
}
object Validator

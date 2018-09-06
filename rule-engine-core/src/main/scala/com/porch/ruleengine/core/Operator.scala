package com.porch.ruleengine.core

sealed trait Operator
object Operator {
  case object CONTAINS extends Operator
  case object DOES_NOT_CONTAIN extends Operator
  case object STARTS_WITH extends Operator
  case object ENDS_WITH extends Operator
  case object EQUALS extends Operator
}


package com.porch.ruleengine.core

trait Rule {
  val name: String
  val definition: RuleDefinition
  val executionOrder: Int
}

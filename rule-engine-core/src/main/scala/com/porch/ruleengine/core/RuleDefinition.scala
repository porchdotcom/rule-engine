package com.porch.ruleengine.core

case class RuleDefinition(schemaVersion: String,
                          logic: RuleTerm)

package com.porch.ruleengine.core

case class Condition(variable: String,
                     operator: Operator,
                     value: String)


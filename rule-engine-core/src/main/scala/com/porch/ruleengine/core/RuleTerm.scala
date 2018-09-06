package com.porch.ruleengine.core

case class RuleTerm(matchAll: Boolean = false,
                    condition: Option[Condition] = None,
                    and: Seq[RuleTerm] = Seq.empty,
                    or: Seq[RuleTerm] = Seq.empty)

package com.porch.ruleengine.core

class RuleEngine(var rules: Seq[Rule]) {

  def getRules: Seq[Rule] = {
    rules
  }

  def reloadRules(newRules: Seq[Rule]): Unit = {
    rules = newRules
  }

  def getFirstMatchingRule(variables: Map[String, Any]): Option[Rule] = {
    rules.find { rule => isRuleMatched(rule, variables) }
  }

  // This can be called in the future when the caller wants all matching rules
  def getAllMatchingRules(variables: Map[String, Any]): Seq[Rule] = {
    rules.filter { rule => isRuleMatched(rule, variables) }
  }

  def isRuleMatched(rule: Rule, variables: Map[String, Any]): Boolean = {
    isRuleTermMatched(rule.definition.logic, variables)
  }

  def isRuleTermMatched(ruleTerm: RuleTerm, variables: Map[String, Any]): Boolean = {
    if (ruleTerm.matchAll) {
      return true
    }

    if (ruleTerm.condition.isDefined) {
      return isConditionMatched(ruleTerm.condition.get, variables)
    }

    if (ruleTerm.and.nonEmpty) {
      return ruleTerm.and.forall { rule => isRuleTermMatched(rule, variables) }
    }

    if (ruleTerm.or.nonEmpty) {
      return ruleTerm.or.exists { rule => isRuleTermMatched(rule, variables) }
    }

    // NOTE: when logic is null, we assume that the rule is not matched
    false
  }

  def isConditionMatched(condition: Condition, variables: Map[String, Any]): Boolean = {
    val variableOpt = variables.get(condition.variable)
    if (variableOpt.isEmpty) return false
    val variable = variableOpt.get

    // We can add more operators here, e.g. equals and greater than, etc.
    condition.operator match {
      case Operator.CONTAINS => variable.asInstanceOf[String].contains(condition.value)
      case Operator.DOES_NOT_CONTAIN => !variable.asInstanceOf[String].contains(condition.value)
      case Operator.EQUALS => variable.asInstanceOf[String].equals(condition.value)
      case Operator.STARTS_WITH => variable.asInstanceOf[String].startsWith(condition.value)
      case Operator.ENDS_WITH => variable.asInstanceOf[String].endsWith(condition.value)
      case _ => return false
    }
  }
}

object RuleEngine {
  // areRulesValid can be called when loading the rules from DB
  def areRulesValid(rules: Seq[Rule]): Boolean = {
    if (!areRulesSorted(rules)) return false

    !rules.exists { rule => !isRuleValid(rule) }
  }

  def areRulesSorted(rules: Seq[Rule]): Boolean = {
    rules == rules.sortBy(_.executionOrder)
  }

  // isRuleValid can be called when saving a rule
  def isRuleValid(rule: Rule): Boolean = {
      isRuleTermValid(rule.definition.logic)
  }

  def isRuleTermValid(ruleTerm: RuleTerm): Boolean = {
    // no other condition can go with matchAll
    if (ruleTerm.matchAll && (ruleTerm.condition.isDefined || ruleTerm.and.nonEmpty || ruleTerm.or.nonEmpty)) {
      return false
    }

    // no condition at all
    if (!ruleTerm.matchAll && ruleTerm.condition.isEmpty && ruleTerm.or.isEmpty && ruleTerm.and.isEmpty) {
      return false
    }

    if (ruleTerm.condition.isDefined && (ruleTerm.matchAll || ruleTerm.and.nonEmpty || ruleTerm.or.nonEmpty)) {
      return false
    }

    if (ruleTerm.and.nonEmpty && (ruleTerm.matchAll || ruleTerm.condition.isDefined || ruleTerm.or.nonEmpty)) {
      return false
    }

    if (ruleTerm.or.nonEmpty && (ruleTerm.matchAll || ruleTerm.condition.isDefined || ruleTerm.and.nonEmpty)) {
      return false
    }

    // recursive check
    if (!ruleTerm.matchAll && ruleTerm.or.exists { rule => !isRuleTermValid(rule) }) {
      return false
    }

    if (!ruleTerm.matchAll && ruleTerm.and.exists { rule => !isRuleTermValid(rule) }) {
      return false
    }

    true
  }


}

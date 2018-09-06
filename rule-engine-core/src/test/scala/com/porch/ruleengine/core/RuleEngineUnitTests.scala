package com.porch.ruleengine.core

import org.scalatest.{FlatSpec, Matchers}

case class TestConcreteRule(id: String,
                            name: String,
                            definition: RuleDefinition,
                            executionOrder: Int) extends Rule

class RuleEngineUnitTests extends FlatSpec with Matchers {

  val REFERRER_URL = "REFERRER_URL"
  val REQUEST_URL = "REQUEST_URL"

  val rule1 = TestConcreteRule(
    id = "rule1",
    name = "rule 1",
    definition = RuleDefinition(
      schemaVersion = "1.0",
      logic = RuleTerm(
        and = Seq(
          RuleTerm(or = Seq(
            RuleTerm(condition = Some(Condition(variable = REFERRER_URL, operator = Operator.ENDS_WITH, value = "google.com/"))),
            RuleTerm(condition = Some(Condition(variable = REQUEST_URL, operator = Operator.CONTAINS, value = "/pro")))
          )),
          RuleTerm(condition = Some(Condition(variable = REQUEST_URL, operator = Operator.CONTAINS, value = "utm_campaign=october_fest")))
        )
      )
    ),
    executionOrder = 1)

  val rule2 = TestConcreteRule(
    id = "rule2",
    name = "rule 2",
    definition = RuleDefinition(
      schemaVersion = "1.0",
      logic = RuleTerm(
        and = Seq(
          RuleTerm(or = Seq(
            RuleTerm(condition = Some(Condition(variable = REFERRER_URL, operator = Operator.ENDS_WITH, value = "facebook.com/"))),
            RuleTerm(condition = Some(Condition(variable = REQUEST_URL, operator = Operator.CONTAINS, value = "/project")))
          )),
          RuleTerm(condition = Some(Condition(variable = REQUEST_URL, operator = Operator.CONTAINS, value = "utm_campaign=christmas")))
        )
      )
    ),
    executionOrder = 2)

  val rule3 = TestConcreteRule(
    id = "rule3",
    name = "rule 3",
    definition = RuleDefinition(
      schemaVersion = "1.0",
      logic = RuleTerm(
        and = Seq(
          RuleTerm(condition = Some(Condition(variable = REFERRER_URL, operator = Operator.DOES_NOT_CONTAIN, value = "https://porch.com/"))),
          RuleTerm(condition = Some(Condition(variable = REFERRER_URL, operator = Operator.EQUALS, value = "http://www.google.com/"))),
          RuleTerm(condition = Some(Condition(variable = REQUEST_URL, operator = Operator.STARTS_WITH, value = "https://porch.com/"))),
          RuleTerm(condition = Some(Condition(variable = REQUEST_URL, operator = Operator.CONTAINS, value = "/pro"))),
          RuleTerm(condition = Some(Condition(variable = REQUEST_URL, operator = Operator.ENDS_WITH, value = "utm_campaign=new_year")))
        )
      )
    ),
    executionOrder = 3)

  val rule4 = TestConcreteRule(
    id = "rule4",
    name = "rule 4",
    definition = RuleDefinition(
      schemaVersion = "1.0",
      logic = RuleTerm(
        or = Seq(
          RuleTerm(condition = Some(Condition(variable = REFERRER_URL, operator = Operator.EQUALS, value = "http://www.cnn.com/"))),
          RuleTerm(condition = Some(Condition(variable = REFERRER_URL, operator = Operator.STARTS_WITH, value = "http://www.nytimes.com/"))),
          RuleTerm(condition = Some(Condition(variable = REQUEST_URL, operator = Operator.CONTAINS, value = "/all-my-projects"))),
          RuleTerm(condition = Some(Condition(variable = REQUEST_URL, operator = Operator.ENDS_WITH, value = "utm_campaign=thanksgiving")))
        )
      )
    ),
    executionOrder = 4)
  val rules = List(rule1, rule2, rule3, rule4)

  "RuleEngine findFirstMatch" should "return rule 1" in {
    val ruleEngine = new RuleEngine(rules)
    val variables = Map(REFERRER_URL -> "http://www.google.com/", REQUEST_URL -> "https://porch.com/pro?utm_campaign=october_fest")
    val rule = ruleEngine.getFirstMatchingRule(variables)
    rule should not be null
    rule should not be None
    rule.get.name shouldBe rule1.name
  }

  "RuleEngine findFirstMatch" should "return rule 2" in {
    val ruleEngine = new RuleEngine(rules)
    val variables = Map(REFERRER_URL -> "http://www.facebook.com/", REQUEST_URL -> "https://porch.com/pro?utm_campaign=christmas")
    val rule = ruleEngine.getFirstMatchingRule(variables)
    rule should not be null
    rule should not be None
    rule.get.name shouldBe rule2.name
  }

  "RuleEngine findFirstMatch: when all AND conditions pass" should "return rule 3" in {
    val ruleEngine = new RuleEngine(rules)
    val variables = Map(REFERRER_URL -> "http://www.google.com/", REQUEST_URL -> "https://porch.com/pro?utm_campaign=new_year")
    val rule = ruleEngine.getFirstMatchingRule(variables)
    rule should not be null
    rule should not be None
    rule.get.name shouldBe rule3.name
  }

  "RuleEngine findFirstMatch: when last AND condition fails" should "return rule None" in {
    val ruleEngine = new RuleEngine(rules)
    val variables = Map(REFERRER_URL -> "http://www.google.com/", REQUEST_URL -> "https://porch.com/pro?utm_campaign=Superman")
    val rule = ruleEngine.getFirstMatchingRule(variables)
    rule shouldBe None
  }

  "RuleEngine findFirstMatch: when last OR condition passes" should "return rule None" in {
    val ruleEngine = new RuleEngine(rules)
    val variables = Map(REFERRER_URL -> "https://porch.com/", REQUEST_URL -> "https://porch.com/pro?utm_campaign=thanksgiving")
    val rule = ruleEngine.getFirstMatchingRule(variables)
    rule should not be null
    rule should not be None
    rule.get.name shouldBe rule4.name
  }

  "RuleEngine getAllMatchingRules" should "return rule1 and rule 2" in {
    val ruleEngine = new RuleEngine(rules)
    val variables = Map(REFERRER_URL -> "http://google.com/", REQUEST_URL -> "https://porch.com/project?utm_campaign=october_fest&utm_campaign=christmas")
    val matchingRules = ruleEngine.getAllMatchingRules(variables)
    matchingRules should not be null
    matchingRules should not be None
    matchingRules.length shouldBe 2
    matchingRules(0).name shouldBe rule1.name
    matchingRules(1).name shouldBe rule2.name
  }

  "RuleEngine getRules" should "return get back the rules" in {
    val ruleEngine = new RuleEngine(rules)
    val rulesReturned = ruleEngine.getRules
    rulesReturned shouldBe rules
  }

  "RuleEngine areRulesSorted" should "return true" in {
    val areRulesValid = RuleEngine.areRulesSorted(rules)
    areRulesValid shouldBe true
  }

  "RuleEngine areRulesSorted" should "return false" in {
    val unsortedRules = List(rule1, rule2, rule4, rule3)
    val areRulesValid = RuleEngine.areRulesSorted(unsortedRules)
    areRulesValid shouldBe false
  }

  "RuleEngine areRulesValid" should "return true" in {
    val areRulesValid = RuleEngine.areRulesValid(rules)
    areRulesValid shouldBe true
  }

  "RuleEngine areRulesValid: unsorted rules" should "return false" in {
    val unsortedRules = List(rule1, rule2, rule4, rule3)
    val areRulesValid = RuleEngine.areRulesValid(unsortedRules)
    areRulesValid shouldBe false
  }

  "RuleEngine isConditionMatched: CONTAINS" should "return true" in {
    val ruleEngine = new RuleEngine(List())
    val condition = Condition("REFERRER_URL", Operator.CONTAINS, "hello")
    val variables = Map("REFERRER_URL" -> "http://www.hello.com/")
    val isConditionMatched = ruleEngine.isConditionMatched(condition, variables)
    isConditionMatched shouldBe true
  }

  "RuleEngine isConditionMatched: CONTAINS" should "return false" in {
    val ruleEngine = new RuleEngine(List())
    val condition = Condition("REFERRER_URL", Operator.CONTAINS, "cnn")
    val variables = Map("REFERRER_URL" -> "http://www.hello.com/")
    val isConditionMatched = ruleEngine.isConditionMatched(condition, variables)
    isConditionMatched shouldBe false
  }

  "RuleEngine isConditionMatched: DOES_NOT_CONTAIN" should "return true" in {
    val ruleEngine = new RuleEngine(List())
    val condition = Condition("REFERRER_URL", Operator.DOES_NOT_CONTAIN, "cnn")
    val variables = Map("REFERRER_URL" -> "http://www.hello.com/")
    val isConditionMatched = ruleEngine.isConditionMatched(condition, variables)
    isConditionMatched shouldBe true
  }

  "RuleEngine isConditionMatched: DOES_NOT_CONTAIN" should "return false" in {
    val ruleEngine = new RuleEngine(List())
    val condition = Condition("REFERRER_URL", Operator.DOES_NOT_CONTAIN, "hello")
    val variables = Map("REFERRER_URL" -> "http://www.hello.com/")
    val isConditionMatched = ruleEngine.isConditionMatched(condition, variables)
    isConditionMatched shouldBe false
  }

  "RuleEngine isConditionMatched: STARTS_WITH" should "return true" in {
    val ruleEngine = new RuleEngine(List())
    val condition = Condition("REFERRER_URL", Operator.STARTS_WITH, "http://www.hello.com/")
    val variables = Map("REFERRER_URL" -> "http://www.hello.com/abcde")
    val isConditionMatched = ruleEngine.isConditionMatched(condition, variables)
    isConditionMatched shouldBe true
  }

  "RuleEngine isConditionMatched: STARTS_WITH" should "return false" in {
    val ruleEngine = new RuleEngine(List())
    val condition = Condition("REFERRER_URL", Operator.STARTS_WITH, "http://www.cnn.com/")
    val variables = Map("REFERRER_URL" -> "http://www.hello.com/abcde")
    val isConditionMatched = ruleEngine.isConditionMatched(condition, variables)
    isConditionMatched shouldBe false
  }

  "RuleEngine isConditionMatched: ENDS_WITH" should "return true" in {
    val ruleEngine = new RuleEngine(List())
    val condition = Condition("REFERRER_URL", Operator.ENDS_WITH, "abcde")
    val variables = Map("REFERRER_URL" -> "http://www.hello.com/abcde")
    val isConditionMatched = ruleEngine.isConditionMatched(condition, variables)
    isConditionMatched shouldBe true
  }

  "RuleEngine isConditionMatched: ENDS_WITH" should "return false" in {
    val ruleEngine = new RuleEngine(List())
    val condition = Condition("REFERRER_URL", Operator.ENDS_WITH, "xyz")
    val variables = Map("REFERRER_URL" -> "http://www.hello.com/abcde")
    val isConditionMatched = ruleEngine.isConditionMatched(condition, variables)
    isConditionMatched shouldBe false
  }

  "RuleEngine isConditionMatched: EQUALS" should "return true" in {
    val ruleEngine = new RuleEngine(List())
    val condition = Condition("REFERRER_URL", Operator.EQUALS, "http://www.hello.com/")
    val variables = Map("REFERRER_URL" -> "http://www.hello.com/")
    val isConditionMatched = ruleEngine.isConditionMatched(condition, variables)
    isConditionMatched shouldBe true
  }

  "RuleEngine isConditionMatched: EQUALS" should "return false" in {
    val ruleEngine = new RuleEngine(List())
    val condition = Condition("REFERRER_URL", Operator.EQUALS, "http://www.cnn.com/")
    val variables = Map("REFERRER_URL" -> "http://www.hello.com/")
    val isConditionMatched = ruleEngine.isConditionMatched(condition, variables)
    isConditionMatched shouldBe false
  }

  "RuleEngine isRuleTermMatched: with matchAll as true" should "return true" in {
    val ruleEngine = new RuleEngine(List())
    val ruleTerm = RuleTerm(matchAll = true, condition = None, and = Seq.empty, or = Seq.empty)
    val variables = Map("REFERRER_URL" -> "http://www.hello.com/")
    val isConditionMatched = ruleEngine.isRuleTermMatched(ruleTerm, variables)
    isConditionMatched shouldBe true
  }

  "RuleEngine isRuleTermMatched: correct condition" should "return true" in {
    val ruleEngine = new RuleEngine(List())
    val condition = Condition("REFERRER_URL", Operator.EQUALS, "http://www.hello.com/")
    val ruleTerm = RuleTerm(condition = Some(condition), and = Seq.empty, or = Seq.empty)
    val variables = Map("REFERRER_URL" -> "http://www.hello.com/")
    val isConditionMatched = ruleEngine.isRuleTermMatched(ruleTerm, variables)
    isConditionMatched shouldBe true
  }

  "RuleEngine isRuleTermMatched: incorrect condition" should "return false" in {
    val ruleEngine = new RuleEngine(List())
    val condition = Condition("REFERRER_URL", Operator.EQUALS, "http://www.cnn.com/")
    val ruleTerm = RuleTerm(condition = Some(condition), and = Seq.empty, or = Seq.empty)
    val variables = Map("REFERRER_URL" -> "http://www.hello.com/")
    val isConditionMatched = ruleEngine.isRuleTermMatched(ruleTerm, variables)
    isConditionMatched shouldBe false
  }

  "RuleEngine isRuleTermMatched: test AND conditions" should "return true" in {
    val ruleEngine = new RuleEngine(List())
    val condition1 = Condition("REFERRER_URL", Operator.STARTS_WITH, "http://")
    val condition2 = Condition("REFERRER_URL", Operator.ENDS_WITH, "hello.com/")
    val ruleTerm1 = RuleTerm(condition = Some(condition1))
    val ruleTerm2 = RuleTerm(condition = Some(condition2))
    val and: Seq[RuleTerm] = List(ruleTerm1, ruleTerm2)
    val ruleTerm = RuleTerm(condition = None, and = and, or = Seq.empty)
    val variables = Map("REFERRER_URL" -> "http://www.hello.com/")
    val isConditionMatched = ruleEngine.isRuleTermMatched(ruleTerm, variables)
    isConditionMatched shouldBe true
  }

  "RuleEngine isRuleTermMatched: test AND conditions" should "return false" in {
    val ruleEngine = new RuleEngine(List())
    val condition1 = Condition("REFERRER_URL", Operator.STARTS_WITH, "http://")
    val condition2 = Condition("REFERRER_URL", Operator.ENDS_WITH, "hello")
    val ruleTerm1 = RuleTerm(condition = Some(condition1))
    val ruleTerm2 = RuleTerm(condition = Some(condition2))
    val and: Seq[RuleTerm] = List(ruleTerm1, ruleTerm2)
    val ruleTerm = RuleTerm(condition = None, and = and, or = Seq.empty)
    val variables = Map("REFERRER_URL" -> "http://www.hello.com/")
    val isConditionMatched = ruleEngine.isRuleTermMatched(ruleTerm, variables)
    isConditionMatched shouldBe false
  }

  "RuleEngine isRuleTermMatched: test OR conditions" should "return true" in {
    val ruleEngine = new RuleEngine(List())
    val condition1 = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")
    val condition2 = Condition("REFERRER_URL", Operator.ENDS_WITH, "hello.com/")
    val ruleTerm1 = RuleTerm(condition = Some(condition1))
    val ruleTerm2 = RuleTerm(condition = Some(condition2))
    val or: Seq[RuleTerm] = List(ruleTerm1, ruleTerm2)
    val ruleTerm = RuleTerm(condition = None, and = Seq.empty, or = or)
    val variables = Map("REFERRER_URL" -> "http://www.hello.com/")
    val isConditionMatched = ruleEngine.isRuleTermMatched(ruleTerm, variables)
    isConditionMatched shouldBe true
  }

  "RuleEngine isRuleTermMatched: test OR conditions" should "return false" in {
    val ruleEngine = new RuleEngine(List())
    val condition1 = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")
    val condition2 = Condition("REFERRER_URL", Operator.ENDS_WITH, "hello")
    val ruleTerm1 = RuleTerm(condition = Some(condition1))
    val ruleTerm2 = RuleTerm(condition = Some(condition2))
    val or: Seq[RuleTerm] = List(ruleTerm1, ruleTerm2)
    val ruleTerm = RuleTerm(condition = None, and = Seq.empty, or = or)
    val variables = Map("REFERRER_URL" -> "http://www.hello.com/")
    val isConditionMatched = ruleEngine.isRuleTermMatched(ruleTerm, variables)
    isConditionMatched shouldBe false
  }

  "RuleEngine isRuleTermValid: test condition" should "return true" in {
    val condition1 = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")
    val ruleTerm = RuleTerm(condition = Some(condition1), and = Seq.empty, or = Seq.empty)
    val isRuleTermValid = RuleEngine.isRuleTermValid(ruleTerm)
    isRuleTermValid shouldBe true
  }

  "RuleEngine isRuleTermValid: AND conditions" should "return true" in {
    val condition1 = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")
    val condition2 = Condition("REFERRER_URL", Operator.ENDS_WITH, "hello.com/")
    val ruleTerm1 = RuleTerm(condition = Some(condition1))
    val ruleTerm2 = RuleTerm(condition = Some(condition2))
    val and: Seq[RuleTerm] = List(ruleTerm1, ruleTerm2)
    val ruleTerm = RuleTerm(condition = None, and = and, or = Seq.empty)
    val isRuleTermValid = RuleEngine.isRuleTermValid(ruleTerm)
    isRuleTermValid shouldBe true
  }

  "RuleEngine isRuleTermValid: OR conditions" should "return true" in {
    val condition1 = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")
    val condition2 = Condition("REFERRER_URL", Operator.ENDS_WITH, "hello.com/")
    val ruleTerm1 = RuleTerm(condition = Some(condition1))
    val ruleTerm2 = RuleTerm(condition = Some(condition2))
    val or: Seq[RuleTerm] = List(ruleTerm1, ruleTerm2)
    val ruleTerm = RuleTerm(condition = None, and = Seq.empty, or = or)
    val isRuleTermValid = RuleEngine.isRuleTermValid(ruleTerm)
    isRuleTermValid shouldBe true
  }

  "RuleEngine isRuleTermValid: no properties" should "return false" in {
    val ruleTerm = RuleTerm(condition = None, and = Seq.empty, or = Seq.empty)
    val isRuleTermValid = RuleEngine.isRuleTermValid(ruleTerm)
    isRuleTermValid shouldBe false
  }

  "RuleEngine isRuleTermValid: condition and AND conditions" should "return false" in {
    val condition = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")
    val condition1 = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")
    val condition2 = Condition("REFERRER_URL", Operator.ENDS_WITH, "hello.com/")
    val ruleTerm1 = RuleTerm(condition = Some(condition1))
    val ruleTerm2 = RuleTerm(condition = Some(condition2))
    val and: Seq[RuleTerm] = List(ruleTerm1, ruleTerm2)
    val ruleTerm = RuleTerm(condition = Some(condition), and = and, or = Seq.empty)
    val isRuleTermValid = RuleEngine.isRuleTermValid(ruleTerm)
    isRuleTermValid shouldBe false
  }

  "RuleEngine isRuleTermValid: condition and OR conditions" should "return false" in {
    val condition = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")
    val condition1 = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")
    val condition2 = Condition("REFERRER_URL", Operator.ENDS_WITH, "hello.com/")
    val ruleTerm1 = RuleTerm(condition = Some(condition1))
    val ruleTerm2 = RuleTerm(condition = Some(condition2))
    val or: Seq[RuleTerm] = List(ruleTerm1, ruleTerm2)
    val ruleTerm = RuleTerm(condition = Some(condition), and = Seq.empty, or = or)
    val isRuleTermValid = RuleEngine.isRuleTermValid(ruleTerm)
    isRuleTermValid shouldBe false
  }

  "RuleEngine isRuleTermValid: AND and OR conditions" should "return false" in {
    val condition1 = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")
    val condition2 = Condition("REFERRER_URL", Operator.ENDS_WITH, "hello.com/")
    val ruleTerm1 = RuleTerm(condition = Some(condition1))
    val ruleTerm2 = RuleTerm(condition = Some(condition2))
    val and: Seq[RuleTerm] = List(ruleTerm1, ruleTerm2)

    val conditionA = Condition("REFERRER_URL", Operator.STARTS_WITH, "http://")
    val conditionB = Condition("REFERRER_URL", Operator.ENDS_WITH, "cnn.com/")
    val ruleTermA = RuleTerm(condition = Some(conditionA))
    val ruleTermB = RuleTerm(condition = Some(conditionB))
    val or: Seq[RuleTerm] = List(ruleTermA, ruleTermB)

    val ruleTerm = RuleTerm(condition = None, and = and, or = or)
    val isRuleTermValid = RuleEngine.isRuleTermValid(ruleTerm)
    isRuleTermValid shouldBe false
  }

  "RuleEngine isRuleTermValid: condition and AND and OR conditions" should "return false" in {
    val conditionSimple = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")

    val condition1 = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")
    val condition2 = Condition("REFERRER_URL", Operator.ENDS_WITH, "hello.com/")
    val ruleTerm1 = RuleTerm(condition = Some(condition1))
    val ruleTerm2 = RuleTerm(condition = Some(condition2))
    val and: Seq[RuleTerm] = List(ruleTerm1, ruleTerm2)

    val conditionA = Condition("REFERRER_URL", Operator.STARTS_WITH, "http://")
    val conditionB = Condition("REFERRER_URL", Operator.ENDS_WITH, "cnn.com/")
    val ruleTermA = RuleTerm(condition = Some(conditionA))
    val ruleTermB = RuleTerm(condition = Some(conditionB))
    val or: Seq[RuleTerm] = List(ruleTermA, ruleTermB)

    val ruleTerm = RuleTerm(condition = Some(conditionSimple), and = and, or = or)
    val isRuleTermValid = RuleEngine.isRuleTermValid(ruleTerm)
    isRuleTermValid shouldBe false
  }

  "RuleEngine isRuleTermValid: AND conditions recursive fails" should "return false" in {
    val conditionSimple = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")

    val condition1 = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")
    val condition2 = Condition("REFERRER_URL", Operator.ENDS_WITH, "hello.com/")
    val ruleTerm1 = RuleTerm(condition = Some(condition1))
    val ruleTerm2 = RuleTerm(condition = Some(condition2))
    val andInner: Seq[RuleTerm] = List(ruleTerm1, ruleTerm2)

    val conditionA = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")
    val conditionB = Condition("REFERRER_URL", Operator.ENDS_WITH, "hello.com/")
    val ruleTermA = RuleTerm(condition = Some(conditionA), and = andInner)
    val ruleTermB = RuleTerm(condition = Some(conditionB))
    val and: Seq[RuleTerm] = List(ruleTermA, ruleTermB)

    val ruleTerm = RuleTerm(condition = Some(conditionSimple), and = and, or = Seq.empty)
    val isRuleTermValid = RuleEngine.isRuleTermValid(ruleTerm)
    isRuleTermValid shouldBe false
  }


  "RuleEngine isRuleTermValid: OR conditions recursive fails" should "return false" in {
    val conditionSimple = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")

    val condition1 = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")
    val condition2 = Condition("REFERRER_URL", Operator.ENDS_WITH, "hello.com/")
    val ruleTerm1 = RuleTerm(condition = Some(condition1))
    val ruleTerm2 = RuleTerm(condition = Some(condition2))
    val orInner: Seq[RuleTerm] = List(ruleTerm1, ruleTerm2)

    val conditionA = Condition("REFERRER_URL", Operator.STARTS_WITH, "https://")
    val conditionB = Condition("REFERRER_URL", Operator.ENDS_WITH, "hello.com/")
    val ruleTermA = RuleTerm(condition = Some(conditionA), or = orInner)
    val ruleTermB = RuleTerm(condition = Some(conditionB))
    val or: Seq[RuleTerm] = List(ruleTermA, ruleTermB)

    val ruleTerm = RuleTerm(condition = Some(conditionSimple), and = Seq.empty, or = or)
    val isRuleTermValid = RuleEngine.isRuleTermValid(ruleTerm)
    isRuleTermValid shouldBe false
  }

  "RuleEngine isRuleTermValid: a rule with matchAll is true and a condition" should "return false" in {
    val condition = Condition("REFERRER_URL", Operator.EQUALS, "http://www.hello.com/")
    val ruleTerm = RuleTerm(matchAll = true, condition = Some(condition), and = Seq.empty, or = Seq.empty)
    val isConditionMatched = RuleEngine.isRuleTermValid(ruleTerm)
    isConditionMatched shouldBe false
  }

}


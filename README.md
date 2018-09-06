# Rule Engine

The intent for Rule Engine is to separate the definition of rules from the application code.  This repository contains
only the code for the Rule Engine.  And you can build the UI and storage of the rules, so that rules can be added, 
modified, and deleted, without the need to change the application code.

## Usage

Please look at our unit tests in RuleEngineUnitTests.scala for usage example.

      val rule1 = MyCustomRule(
        id = "rule1",
        name = "rule 1",
        definition = RuleDefinition(
          schemaVersion = "1.0",
          logic = AndRuleTerm(
            terms = Seq(
              OrRuleTerm(terms = Seq(
                ConditionRuleTerm(condition = Condition(variable = REFERRER_URL, operator = Operator.ENDS_WITH, value = "google.com/")),
                ConditionRuleTerm(condition = Condition(variable = REQUEST_URL, operator = Operator.CONTAINS, value = "/pro"))
              )),
              ConditionRuleTerm(condition = Condition(variable = REQUEST_URL, operator = Operator.CONTAINS, value = "utm_campaign=october_fest"))
            )
          )
        ),
        executionOrder = 1)
        
      val rule2 = TestConcreteRule(
        id = "rule2",
        name = "rule 2",
        definition = RuleDefinition(
          schemaVersion = "1.0",
          logic = AndRuleTerm(
            terms = Seq(
              OrRuleTerm(terms = Seq(
                ConditionRuleTerm(condition = Condition(variable = REFERRER_URL, operator = Operator.ENDS_WITH, value = "facebook.com/")),
                ConditionRuleTerm(condition = Condition(variable = REQUEST_URL, operator = Operator.CONTAINS, value = "/project"))
              )),
              ConditionRuleTerm(condition = Condition(variable = REQUEST_URL, operator = Operator.CONTAINS, value = "utm_campaign=christmas"))
            )
          )
        ),
        executionOrder = 2)
        
      val rules = List(rule1, rule2)
      val ruleEngine = new RuleEngine(rules)
      val variables = Map(REFERRER_URL -> "http://www.google.com/", REQUEST_URL -> "https://porch.com/pro?utm_campaign=october_fest")
      val matchingRule = ruleEngine.getFirstMatchingRule(variables)

## Make Changes and Build

Run this in command line:

    mvn clean install    

### Unit Test

Run this in command line:
  
    mvn test 

## Licensing

BSD 3-Clause License: http://opensource.org/licenses/BSD-3-Clause

## Contributing

Create fork and create a pull request for merging back to the main repo. 

## Support

File an issue: https://github.com/porchdotcom/rule-engine/issues

## Contact

David Long <davidlong@porch.com>

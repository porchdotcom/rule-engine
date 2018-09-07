# Rule Engine

The intent for Rule Engine is to separate the definition of rules from the application code.  This repository contains
only the code for the Rule Engine in Scala.  And you can build the UI and storage of the rules, so that rules can be added, 
modified, and deleted, without the need to change the application code.

## Usage

Please look at our unit tests in RuleEngineUnitTests.scala for more usage examples.

    val rule1 = TestConcreteRule(
      id = "rule1",
      name = "rule 1",
      definition = RuleDefinition(
        schemaVersion = "1.0",
        logic = RuleTerm(
          and = Seq(
            RuleTerm(condition = Some(Condition(variable = REFERRER_URL, operator = Operator.ENDS_WITH, value = "google.com/"))),
            RuleTerm(or = Seq(
              RuleTerm(condition = Some(Condition(variable = REQUEST_URL, operator = Operator.CONTAINS, value = "/cp"))),
              RuleTerm(condition = Some(Condition(variable = REQUEST_URL, operator = Operator.CONTAINS, value = "/cs")))
            ))
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
            RuleTerm(condition = Some(Condition(variable = REFERRER_URL, operator = Operator.ENDS_WITH, value = "facebook.com/"))),
            RuleTerm(or = Seq(
              RuleTerm(condition = Some(Condition(variable = REQUEST_URL, operator = Operator.CONTAINS, value = "/project"))),
              RuleTerm(condition = Some(Condition(variable = REQUEST_URL, operator = Operator.CONTAINS, value = "utm_campaign=christmas")))
            ))
          )
        )
      ),
      executionOrder = 2)
        
      val rules = List(rule1, rule2)
      val ruleEngine = new RuleEngine(rules)
      val variables = Map(REFERRER_URL -> "http://www.google.com/", REQUEST_URL -> "https://porch.com/pro?utm_campaign=october_fest")
      val matchingRule = ruleEngine.getFirstMatchingRule(variables)

If you build a RESTful APIs to create, read, update, and delete build rules, you can define the rules in JSON and transform them into Scala on the server side.  Here is a JSON example of a rule

      {
         "schemaVersion": "1.0",
         "logic": {
             "and": [
                 {
                    "condition": {
                        "variable": "REFERRER_URL",
                        "operator": "ENDS_WITH",
                        "value": "google.com/”
                     }
                 },
                 {
                  or: [
                      {
                        "condition": {
                            "variable": "REQUEST_URL",
                            "operator": "CONTAINS",
                            "value": "/cp”
                         }
                      },
                      {
                        "condition": {
                            "variable": "REQUEST_URL",
                            "operator": "CONTAINS",
                            "value": "/cs”
                         }
                      }
                  ]
                 }
             ]
         }
      }

## Make Changes and Build

You will need to instal Maven (https://maven.apache.org/), and run this in command line:

    mvn clean install    

Or you can just build the Scala code into a .jar file using Scala compiler scalac.

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

{
  "name": "simple",
  "lslVersion": "1.0.0",
  "types": [
    {
      "name": "Int",
      "realName": "int"
    }
  ],
  "automata": [
    {
      "name": "A",
      "constructorVariables": [],
      "variables": [
        {
          "name": "b",
          "type": "Int"
        }
      ],
      "states": [],
      "shifts": [],
      "functions": [
        {
          "name": "foo",
          "automaton": "A",
          "args": [],
          "contracts": [],
          "statements": [
            {
              "kind": "assignment",
              "variableName": "b.i",
              "variableAutomaton": "A",
              "value": {
                "kind": "integer",
                "value": 1
              }
            }
          ]
        }
      ]
    },
    {
      "name": "B",
      "constructorVariables": [],
      "variables": [
        {
          "name": "i",
          "type": "Int"
        }
      ],
      "states": [
        {
          "name": "s",
          "kind": "SIMPLE"
        }
      ],
      "shifts": [],
      "functions": []
    }
  ]
}
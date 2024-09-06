# Nice things

## Boilerplate

1. dto, creation, toDomain, fromDomain
1. toString of data classes
1. builder pattern
1. generating test cases

1. generating tests with copilot references

So I'm only missing tests for buildSpecialBuilding in #file:Kingdom.java, see how I have implemented tests for other functions related to special buildings in #file:KingdomSpecialBuildingActionTest.java, there is no need to mock anything as my test setup involves stubbing of the relevant dependencies. The tests ou generate should also be placed in that file, make sure to cover all the code branches

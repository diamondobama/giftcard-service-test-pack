---
title: "Test Client"
menu:
  main:
    weight: 30
---

The Giftcard Service Interface Test Client is a collection of [Postman](https://www.getpostman.com) scripts which are designed to send specific requests to the Giftcard Service Interface server implementation under test. The [Postman](https://www.getpostman.com) scripts also test the responses received from the server and examine them for expected values. The [Postman](https://www.getpostman.com) scripts can be downloaded from [Electrum's Git repo](https://github.com/electrumpayments/giftcard-service-test-pack/tree/master/test/postman).

## Operation

At an individual message level requests are built dynamically with cross-referenced values (e.g. the request ID of an original request within a reversal advice) kept in Postman environment variables. For example, an activation request script will generate the necessary values and store them, as well as the necessary response fields, which are then used in the next advice message. The tester should ensure that the correct request script is run before an advice script otherwise unintended field values may be submitted.

Certain functionality requires a specific flow of messages be executed in order to test the functionality. Test messages are therefore placed in folders and messages within these folders must be sent in order to ensure the tests all pass. Furthermore all collections of scripts in folders expect the gift card used in the test to start in an inactive state. Therefore the tester must ensure that between running each collection the server under test is reset back to a starting state.

### Giftcard Test Server Dependency

The Giftcard Service Interface Test Client was developed and tested against the Giftcard Service Interface Test Client. This dependency leads to the following properties of the Test Client:
- All tests in the Test Client pass when run against the Test Server.
- The Test Client uses the cards and products configured in the Test Server as part of the messages sent by the Test Client. When using the test client the server under test should be loaded with cards and products which match those configured in the Test Server. Alternatively the cards and products used by the Test Client can be specified in the Postman scripts.
- The Test Client uses the specific endpoints extensions exposed by the Test Server to reset the Test Server between test runs. If the server under test does not provide similar functionality the tester should replace the Test Client's calls to reset the server with appropriate calls or select a different card and product to use during each run. More information about the Test Server's endpoint extensions can be found [here](/testing/specification/operations).

### Specifying Test Card And Product

The Postman Activation scripts specify the card and product used during the test. To change the card and product used edit the card and product variables in the test's Pre-request Script:

![An Activation Script](/images/activation_card.png "An Activation Script")

## Validation

The test client makes use of Postman’s test checks to validate responses from the server. The tester should inspect the result of the test checks upon receipt of a response message to ensure the server being tested responded with the correct values.

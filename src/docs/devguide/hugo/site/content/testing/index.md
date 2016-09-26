---
title: "Testing"
menu:
  main:
    weight: 10
---

When developing a server or client implementation of the [Giftcard Service Interface](https://electrumpayments.github.io/giftcard-service-interface-docs/) it is important to verify that the implementation conforms to the [Giftcard Service Interface](https://electrumpayments.github.io/giftcard-service-interface-docs/) specification. To test client implementations a test server is available at https://giftcard-service-test-pack.herokuapp.com. To test server implementations a test pack containing Postman scripts is available [here](https://github.com/electrumpayments/giftcard-service-test-pack/tree/master/test/postman).

Note that the test pack is made available simply as a tool to help test and investigate common implementation issues. However, the test pack is not intended to imply certification of the tested implementation's conformance to the [Giftcard Service Interface](https://electrumpayments.github.io/giftcard-service-interface-docs/).

## Important Security Considerations

The test pack does not claim to implement any security features and should never be considered safe to use in a production environment or with real, sensitive data.

Users of the test pack must bear in mind that any data sent from the test client or to the Test Server may be logged in plain text and without sufficient access control restrictions. Furthermore, the manner in which the Test Server handles requests does not guarantee resources created by one entity cannot be viewed or modified by another. While the Postman scripts submit basic HTTP Authentication and the Test Server reads the Authentication header, these credentials are not mandatory in the test environment, are never verified and do not safely segregate test clients' messages from one another in all instances.

## Test Client

The test client is provided as a collection of Postman scripts and environments which submit gift card messages to a specified end point.

More information regarding the Giftcard Service Interface Test Client can be found in the [Test Client](/testing/test_client) section.

## Test Server

The Test Server is provided as a web service constantly listening at https://giftcard-service-test-pack.herokuapp.com.

More information regarding the Giftcard Service Interface Test Server can be found in the [Test Server](/testing/test_server) section.

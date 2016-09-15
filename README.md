# giftcard-service-test-pack [![CircleCI](https://circleci.com/gh/electrumpayments/giftcard-service-test-pack/tree/master.svg?style=shield)](https://circleci.com/gh/electrumpayments/giftcard-service-test-pack/tree/master)
REST server for testing a client implementation of the [giftcard-service-interface](https://github.com/electrumpayments/giftcard-service-interface).

##Testing a Client
To test a client implementation an instance of this test server is provided at: https://giftcard-service-test-pack.herokuapp.com.
Messages sent to this server via the urls described in the [giftcard-service-interface](https://github.com/electrumpayments/giftcard-service-interface) will be
validated and responded to with mocked up data.

### Testing message correctness
Messages will be validated for correctness against the service interface and in the event that a field is missing something similar to the following can be expected:

```json
{
  "errorType": "FORMAT_ERROR",
  "errorMessage": "Bad formatting",
  "detailMessage": {
    "formatErrors": [
      {
        "field": "product",
        "msg": "may not be null",
        "value": "null"
      }
    ]
  }
}
```

An errorType of `FORMAT_ERROR` is returned followed by an explanation of the format errors as follows:

* The "field"  attribute containing the field that has been formatted incorrectly
* The "msg" field contains information on what violation has occurred
* The "value" field contains the incorrectly formatted value that was used

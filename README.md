# giftcard-service-test-pack [![CircleCI](https://circleci.com/gh/electrumpayments/giftcard-service-test-pack/tree/master.svg?style=shield)](https://circleci.com/gh/electrumpayments/giftcard-service-test-pack/tree/master)
REST server for testing an implementation of the [giftcard-service-interface](https://github.com/electrumpayments/giftcard-service-interface).

## Testing a Client
To test a client implementation an instance of this test server is provided at: https://giftcard-service-test-pack.herokuapp.com.

Or you can run a containerized version of this test server locally using Docker:
```bash
docker pull electrum/giftcard-test-server:3
# Run the test server listening on localhost:8080
docker run -d -p 8080:8080 electrum/giftcard-test-server:3
```

Messages sent to this server via the urls described in the [giftcard-service-interface](https://github.com/electrumpayments/giftcard-service-interface) will be
validated and responded to with mocked up data.

### Testing message correctness
Messages will be validated for correctness against the service interface, in the event that a field is missing something similar to the following can be expected:

```json
{
  "errorType": "FORMAT_ERROR",
  "errorMessage": "See error detail for format errors.",
  "detailMessage": [
    {
      "messageProperty": "message",
      "field": "time",
      "error": "may not be null"
    },
    {
      "messageProperty": "institution",
      "field": "id",
      "error": "must match \"[0-9]{1,11}\"",
      "invalidValue": "hjg77"
    },
    {
      "messageProperty": "merchant",
      "field": "merchantId",
      "error": "may not be null"
    }
  ]
}
```

An errorType of `FORMAT_ERROR` is returned followed by an explanation of the format errors as follows:

* The `messageProperty` attribute containing the element in which the error occurs
* The `field` attribute containing the field that has been formatted incorrectly
* The `error` field contains information on what violation has occurred
* The `invalidValue` field contains the incorrectly formatted value that was used

## Testing a Server
Testing a server implementation can be achieved using [this](https://github.com/electrumpayments/giftcard-service-test-pack/tree/master/test/postman) Postman (Collection v2) REST test pack.
These tests consist of correctly formatted JSON messages that validate server responses. Tests may also consist of a message flow in which multiple related messages are sent sequentially to the server to test handling of state-full interactions (such as requests and confirmations).

The test pack is comprised of three JSON files: `Gifcard.postman_collection.json` , `heroku.postman_environment.json` and `localhost.postman_environment.json`.
The first file is a collection of JSON tests that will be run, herein one will find JSON request messages and response validation scripts. These tests are dependant on variables contained in the the preceding two files, these being identical save for the server endpoint they point to:

```json
{
  "enabled": true,
  "key": "url",
  "type": "text",
  "value": "https://giftcard-service-test-pack.herokuapp.com"
}
```

Changing the above property within an environment will change the endpoint to which messages are sent.

### Running tests

There are two possible ways to run this test pack: either via the Postman desktop client or via Newman, the command line interface for Postman.

#### Postman
1. Download Postman at: https://www.getpostman.com/apps
2. Import the test collection and environments via the Import option
3. Open the Collection Runner and select the Runs tab
4. Select a test collection and environment and hit Start Test. Note that individual test subsections may be selected.

Note that that tests may be run individually from the main Postman view where test conditions and structures may be modified.

#### Newman
1. Install newman (make sure `npm` is installed first):
```
	npm install newman -g
```
2. Run the tests (from the root directory of this reop):
```
	newman run test/postman/Giftcard.postman_collection.json -e test/postman/localhost.postman_environment.json
```
This will run all tests and provide a basic breakdown of which tests passed and failed.

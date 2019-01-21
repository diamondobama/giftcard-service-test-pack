---
title: "Test Server"
menu:
  main:
    weight: 20
---

The Giftcard Service Interface Test Server is an online tool available for testing client implementations of the Giftcard Service Interface. In order to facilitate accurate testing of gift card operations the Test Server exposes specific end points to help a tester understand the data used by the Test Server as well as certain limited operations which may be performed against the Test Server itself. Furthermore, the Test Server expands upon the Giftcard Service Interface to help describe errors which may be encountered while using the Test Server.

## User Identification

To ensure one user of the Test Server does not impact upon another user of the Test Server, particularly in terms of data manipulation, users' test data is segregated into separate logical databases held in memory. The specific database used for a user is determined by examining the username/password combination submitted in the HTTP Basic Authentication header. This identification is performed for all requests submitted to the Test Server - both administrative and conventional requests alike. Thus a user can safely make use of the pre-configured cards within the Test Server without concern for the actions of other users provided two different users do not use the same username/password combination.

Note that a common mistake a user of the Test Server may make is to not submit a username and password. Should another user also not submit a username and password then the two users will both manipulate the same set of data unexpectedly.

## Data Persistence

The Giftcard Service Interface Test Server keeps user data in a database held in memory which is populated from static files upon start up. This has the effect that should the Test Server be restarted at any the state of data in the Test Server will be lost. Data in the Test Server will be reverted back to initial values when the Test Server is restarted. The user can also choose to manually reset just the user's own data back to initial values by making use of the administrative operations described below.

## Administrative operations

The Test Server exposes new endpoints which may be used for administrative tasks on the Test Server. These are:

- [/giftcard/v2/testServerAdmin/reset](/testing/specification/operations/#reset)
- [/giftcard/v2/testServerAdmin/data](/testing/specification/operations/#data)
- [/giftcard/v2/testServerAdmin/data/card/{cardNumber}](/testing/specification/operations/#singlecarddata)
- [/giftcard/v2/testServerAdmin/data/product/{productId}](/testing/specification/operations/#singleproductdata)

The above endpoints are only available as part of the Test Server and do not form part of the Giftcard Service Interface. These endpoints are described in more detail on the [Operations](/testing/specification/operations) page.

## Models

The Test Server defines new models intended to carry data used exclusively by the Test Server. An example is the [CardRecord](/testing/specification/definitions/#cardRecord) model used to contain information about a gift card and its state within the Test Server. These models are exclusive to the Test Server and do not form part of the Giftcard Service Interface. These models are described in more detail on the [Definitions](/testing/specification/definitions/) page.

Of particular note is the [DetailMessage](/testing/specification/definitions/#detailMessage) model used to describe specific errors encountered by the Test Server. This is described in more detail below.

### Validation

The Test Server defines a [DetailMessage model](/testing/specification/definitions/#DetailMessage). The Test Server returns a [DetailMessage model](/testing/specification/definitions/#DetailMessage) object in the detailMessage field of the [ErrorDetail](/testing/specification/operations/#errorDetail) object returned in error responses. The [DetailMessage model](/testing/specification/definitions/#DetailMessage) model's fields are all of the models defined in the Giftcard Service Interface as well as an extra field called formatErrors. The formatErrors field is an array of [FormatError models](/testing/specification/definitions/#FormatError). A [FormatError models](/testing/specification/definitions/#FormatError) has three fields:

- `field` - The name of the field which failed validation.
- `msg` - A description of the expected format of the field.
- `value` - The invalid value received in the field.

The Test Server will validate the format of received messages. If the received message fails validation the Test Server will return a list of fields which failed validation.

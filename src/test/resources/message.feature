Feature: Plivo assignment - Message api


  @demo
  Scenario: To verify the cash credit for a particular account

    Given Basic auth
    When user hits "/" get api
    Then the status code is 200 else "accounts api failed"
    And get the cash_credits from the response
    And store the resp value

    Given Basic auth
    And Request params
      |key|value|
      |limit|2  |
    When user hits "/Number" get api
    Then the status code is 200 else "get account numbers failed"
    And objects response length should be 2

    Given Basic auth
    And Request body as json
      |key|value|
      |jsonFileName|post-message.json|
      |src|response:objects[0].number|
      |dst|response:objects[1].number|
      |text|assignment text for message api|
    When user hits "/Message/" post api
    Then the status code is 202 else "message api failed"
    And Returned json schema is "post-message-schema.json"
    And get the message_uuid[0] from the response

    Given Basic auth
    When user hits "/Message" get api with id "response"
    Then the status code is 200 else "details api failed"
    And get the total_rate from the response

    Given Basic auth
    And Request params
      |key|value|
      |country_iso|US|
    When user hits "/Pricing" get api
    Then the status code is 200 else "Pricing api failed"
    And Response should contain
      |key|value|
      |message.outbound.rate|previousrespvalue|

    Given Basic auth
    When user hits "/" get api
    Then the status code is 200 else "accounts api failed"
    And verify the cash deducted is equal to "cash_credits"







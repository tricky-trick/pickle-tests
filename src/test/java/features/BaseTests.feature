@Frontend
Feature: Verify basic flows

  Background:
    Given User navigates to the 'https://demoqa.com/' site

  @TEST_11
  Scenario: Verify that user can search the book
    When User opens the 'Book Store Application' page
    And User search the book 'Git'
    Then The 'Git Pocket Guide' book is displayed

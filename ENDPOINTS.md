# Endpoints

#### POST: /subscription/basic
Description: upgrades user's subscription to basic.   
Authorized: USER,BASIC,PREMIUM.   
Response Content-Type: text/plain.   
Responses:
1. 200(OK) successful upgrade. The response contains new JWT to be used.
2. 401(Unauthorized) - user not authenticated.
3. 403(forbidden) - action not allowed by authenticated user.
4. 404(Not Found)
5. 400(Bad Request)

#### POST: /subscription/premium
Description: upgrades user's subscription to premium.   
Authorized: USER,BASIC,PREMIUM.   
Response Content-Type: text/plain.   
Responses:
1. 200(OK) successful upgrade. The response contains new JWT to be used.
2. 401(Unauthorized) - user not authenticated.
3. 403(forbidden) - action not allowed by authenticated user.
4. 404(Not Found)
5. 400(Bad Request)

#### GET: /hotel/{id}
Description: returns information of the hotel with the requested id.   
Authorized: USER,BASIC,PREMIUM.   
Response Content-Type: application/json.   
Example Response:   
```json
{
    "name": "Cn h cao cp sn bay Tn Sn Nht",
    "description": "Situated in Ho Chi Minh City in the Ho Chi Minh Municipality region, Cn h cao cp sn bay Tn Sn Nht has a balcony.",
    "location": {
        "address": "108 Hng H",
        "city": "Ho Chi Minh Municipality",
        "longitude": 106.67,
        "latitude": 10.8118
    },
    "url": "https://www.booking.com/hotel/vn/can-ho-cao-cap-san-bay-tan-son-nhat.html",
    "hotelType": "HOTEL",
    "score": "5.00",
    "hotelID": 1,
    "rooms": [
        {
            "roomID": 2,
            "description": "'not available'",
            "name": "'One-Bedroom Apartment'",
            "hotelID": 1,
            "hotelName": "'One-Bedroom Apartment'",
            "attributes": [
                "''",
                "'70 m²'",
                "'Hot tub'",
                "'Washing machine'",
                "'Free WiFi'",
                "'Balcony'",
                "'Private pool'",
                "'Kitchen'",
                "'Private kitchen'",
                "'Heating'",
                "'Air conditioning'",
                "'Flat-screen TV'",
                "'Terrace'",
                "'Kitchenette'",
                "'Garden view'"
            ]
        }
    ]
}
```
Responses:   
1. 200(OK)
2. 401(Unauthorized) - user not authenticated.
3. 403(forbidden) - action not allowed by authenticated user.
4. 404(Not Found)

#### POST: /customer/email/verification
Description: sends token to email that allows customer registration.   
Authorized: ALL.   
Request Content-Type: application/json.   
Example Request:
```json
{
  "email" : "some@email.com"
}
```
Responses:   
1. 200(OK)
2. 400(Bad Request)
3. 503(Service Unavailable)
4. 429(Too many requests)

#### POST: /customer/password/reset
Description: sends token to email that allows customer to reset their password.   
Authorized: ALL.   
Request Content-Type: application/json.   
Example Request:
```json
{
  "email" : "some@email.com"
}
```
Responses:
1. 200(OK)
2. 400(Bad Request)
3. 503(Service Unavailable)
4. 429(Too many requests)

#### PUT: /customer/password
Description: updates customer password.   
Authorized: ALL.   
Request Content-Type: application/json.   
Example Request:
```json
{
  "jwt" : "JWT",
  "password" : "password",
  "repeatedPassword": "password"
}
```
Responses:
1. 200(OK)
2. 400(Bad Request)
3. 503(Service Unavailable)
4. 401(Unauthorized) invalid JWT.

#### DELETE: /monitor_list/{id}
Description: deletes monitor list with the specified id.   
Authorized: USER,BASIC,PREMIUM.   
Responses:   
1. 200(OK)
2. 401(Unauthorized) - user not authenticated.
3. 403(forbidden) - action not allowed by authenticated user.
4. 404(Not Found)

#### PUT: /monitor_list
Description: updated monitor list.   
Authorized: USER,BASIC,PREMIUM.   
Request Content-Type: application/json.   
Example Request:

```json
{
  "rooms": [1, 2, 3],
  "name": "new_name",
  "distanceDays" : [7,30,100],
  "id": 1
}
```
Responses:   
1. 200(OK)
2. 401(Unauthorized) - user not authenticated.
3. 403(forbidden) - action not allowed by authenticated user.
4. 404(Not Found)
5. 400(Bad Request)

#### GET: /room/history/{id}
Description: returns the history of room's attributes changes.
Authorized: USER,BASIC,PREMIUM.   
Response Content-Type: application/json.   
Query Parameters:   
1. id - id of the room.
Example Response:
```json
{
    "_embedded": {
        "roomChangeList": [
            {
                "addedAttributes": [
                    "TV",
                    "Toilet",
                    "Upper floors accessible by elevator",
                    "Carbon monoxide detector",
                    "Sofa"
                ],
                "removedAttributes": [],
                "timestamp": 1704892466
            },
            {
                "addedAttributes": [
                    "More"
                ],
                "removedAttributes": [],
                "timestamp": 1704935279
            },
            {
                "addedAttributes": [],
                "removedAttributes": [
                    "More"
                ],
                "timestamp": 1704978677
            },
            {
                "addedAttributes": [
                    "More"
                ],
                "removedAttributes": [],
                "timestamp": 1704978678
            },
            {
                "addedAttributes": [],
                "removedAttributes": [
                    "More"
                ],
                "timestamp": 1704990073
            },
            {
                "addedAttributes": [
                    "More"
                ],
                "removedAttributes": [],
                "timestamp": 1704990076
            }
        ]
    },
    "_links": {
        "first": {
            "href": "http://195.251.123.174:8085/room/history/1727?page=0&size=20"
        },
        "prev": {
            "href": "http://195.251.123.174:8085/room/history/1727?page=0&size=20"
        },
        "self": {
            "href": "http://195.251.123.174:8085/room/history/1727?page=1&size=20"
        },
        "last": {
            "href": "http://195.251.123.174:8085/room/history/1727?page=1&size=20"
        }
    },
    "page": {
        "size": 20,
        "totalElements": 26,
        "totalPages": 2,
        "number": 1
    }
}
```
Responses
1. 200(OK)
2. 401(Unauthorized) - user not authenticated.
3. 403(forbidden) - action not allowed by authenticated user.

#### GET: /monitor_list/{id}
Description: returns details of requested monitor list.   
Authorized: USER,BASIC,PREMIUM.    
Response Content-Type:application/json.   
Query Parameters:
1. id - the id of the monitor list.   
2. from - ISO date indicates the range of prices used for statistic calculation(Optional).
3. until - ISO date indicates the range of prices used for statistic calculations(Optional).
4. exclude - room ids that should not be included in the statistic calculation(Optional).
Example Response:   
```json
{
  "monitorListName": "MyOlympiadaApartments",
  "monitorListID": 1,
  "monitorListStatistics": {
    "avg": 61,
    "min": 2,
    "max": 1096
  },
  "distances" : [30,40,50],
  "rooms": [
    {
      "roomID": 1,
      "description": "'not available'",
      "name": "'Apartment'",
      "statistics": {
        "sum": 233494,
        "count": 2976,
        "min": 43,
        "max": 175
      },
      "attributes": [
        "'View'",
        "'Washing machine'",
        "'Free WiFi'",
        "'Balcony'",
        "'Private pool'",
        "'Kitchen'",
        "'Private kitchen'",
        "'Entire apartment'",
        "'Ensuite bathroom'",
        "'Air conditioning'",
        "'Flat-screen TV'",
        "'Terrace'",
        "'160 m²'"
      ]
    }
  ]
}
```
Responses:
1. 200(OK)
2. 401(Unauthorized) - user not authenticated.
3. 403(forbidden) - action not allowed by authenticated user.

#### POST: /customer/login
Description: Performs customer authentication.On successful login, returns access token.     
Authorized: ALL.   
Request Content-Type: application/json.   
Example Request:
```json
{
  "email" : "test@test.com",
  "password" : "password"
}
```
Response Content-Type: text/plain
Example Response:
```text
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJQcmljZU1vbml0b3JpbmciLCJzdWIiOiIxIiwiZXhwIjoxNjc2NTU0NDY0LCJpYXQiOjE2NzY0NjgwNjQsInNjb3BlIjoiVVNFUiJ9.gZ-BCxSsHPWwkHVcEUaoogUkiS-AcFIXyWODA9xVhJTYiL-YVX3M0BsH4s9MnSr4AjgD0HBSZDfVq2uTqfnxHg
```
Responses:   
1. 200(OK) successful authentication.
2. 401(Unauthorized) invalid credentials.
3. 400(Bad Request) invalid request.

#### POST: /customer/register
Description: Performs customer registration.   
Authorized: ALL.   
Request Content-Type: application/json.
Example Request:
```json 
{
  "jwt" : "JWT",
  "name" : "new_name",
  "password" : "new_password",
  "repeatedPassword" : "new_password"
}
```
Responses:
1. 200(OK) successful registration.
2. 400(Bad Request) bad request or constraint violations.
3. 409(Conflict) user with provided email already exists.
4. 401(Unauthorized) invalid JWT.

#### POST: /hotel/info
Description: Returns hotel information including its rooms.
Authorized: USER,BASIC,PREMIUM.
Request Content-Type: application/json.   
Example Request:   
```json
{
  "link" : "https://test.com?somethingtesat=123"
}
```
Response Content-Type: application/json.   
Example Response:

```json 
{
  "name": "TEST_NAME",
  "description": "TEST_DESCRIPTION",
  "location": {
    "address": "TEST_ADDRESS",
    "city": "TEST_CITY",
    "longitude": 10.5,
    "latitude": 10.5
  },
  "url": "http://test.com/",
  "hotelType": "HOTEL",
  "score": "5.60",
  "hotelID": 229,
  "rooms": [
    {
      "roomID": 455,
      "description": "TEST_DESCRIPTION_1",
      "name": "TEST_NAME_1",
      "attributes": [
        "TEST_ATTR_1",
        "TEST_ATTR_2"
      ]
    },
    {
      "roomID": 456,
      "description": "TEST_DESCRIPTION_2",
      "name": "TEST_NAME_2",
      "attributes": [
        "TEST_ATTR_3",
        "TEST_ATTR_1"
      ]
    }
  ]
}
```
Responses:
1. 200(OK)
2. 400(Bad Request)
3. 401(Unauthorized) - user not authenticated.
4. 403(forbidden) - action not allowed by authenticated user.
5. 503(Not Found) hotel info for the provided url not found and scrapping service is too busy to answer.

#### POST: /monitor_list
Description: creates new monitor list.   
Authorized: USER,BASIC,PREMIUM.   
Request Content-Type: application/json.   
Example Request:
```json
{
  "name" : "monitor_list_name",
  "rooms": [1,2,3,4],
  "distanceDays" : [30,70,80]
}
```
Responses:
1. 200(OK) successful monitor list creation.
2. 401(Unauthorized) unauthenticated access.
3. 403(Forbidden) action not allowed by user.
4. 400(Bad Request).
5. 409(Conflict) monitor list with provided name already exists(per user).
6. 404(Not Found) one or more of the provided room ids do not exist.

#### GET: /monitor_list
Description: returns monitor lists of the authenticated user.   
Authorized: USER,BASIC,PREMIUM.   
Request Content-Type: application/json.   
Response Content-Type: application/json.   
Example Response:
```json 
[
  {
    "monitorListName": "some_name_1",
    "monitorListID": 1
  },
  {
    "monitorListName" : "some_name_2",
    "monitorListID": 4
  }
]
```
Responses:
1. 200(OK) success.
2. 401(Unauthorized) unauthenticated access.
3. 403(Forbidden) action not allowed by user.


#### GET: /prices/{monitorListID}/{roomID}?size={size}&page={page}&sort={property},{order}
Description: returns prices of the requested room and monitor list.   
Authorized: USER,BASIC,PREMIUM.   
Response Content-Type:application/json.   
Query Parameters:
1. size - the size of the page that should be returned.   
2. page - page number.   
3. sort - specifies the sorting order of the returned prices separated with comma by order, where order can be asc or desc. Multiple sort query parameters can be used.

Path Variables:
1. monitorListID - the id of the monitor list.
2. roomID - the id of the room.
Example Response:
```json
{
  "_embedded":{
    "priceInfoList":[
      {
        "id":1143,
        "sleeps":2,
        "price":451,
        "quantity":4,
        "distanceDays":9,
        "cancellationPolicy":"UNKNOWN",
        "timestamp":"2023-08-14T13:02:01.703918",
        "breakfastPolicy":"unknown",
        "attributes":[
          "TEST_ROOM_VIEW_ATTR_2",
          "TEST_ROOM_VIEW_ATTR_3"
        ]
      },
      {
        "id":1141,
        "sleeps":1,
        "price":771,
        "quantity":3,
        "distanceDays":2,
        "cancellationPolicy":"UNKNOWN",
        "timestamp":"2023-08-14T13:02:01.682616",
        "breakfastPolicy":"unknown",
        "attributes":[
          "TEST_ROOM_VIEW_ATTR_1",
          "TEST_ROOM_VIEW_ATTR_2"
        ]
      }
    ]
  },
  "_links":{
    "first":{
      "href":"http://localhost:8085/prices/570?page=0&size=2"
    },
    "self":{
      "href":"http://localhost:8085/prices/570?page=0&size=2"
    },
    "next":{
      "href":"http://localhost:8085/prices/570?page=1&size=2"
    },
    "last":{
      "href":"http://localhost:8085/prices/570?page=1&size=2"
    }
  },
  "page":{
    "size":2,
    "totalElements":4,
    "totalPages":2,
    "number":0
  }
}
```
Responses:
1. 200(OK) success
2. 401(Unauthorized) unauthenticated access.
3. 403(Forbidden) action not allowed by user.
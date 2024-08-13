How to up?
1. Docker compose build
2. docker compose up
3. Now two separate websocket servers are up listening on 8081,8082
4. make connection to both using ws://localhost:8081,ws://localhost:8082
5. Send below set of messages to interact between two client

6. {
    "action": "subscribe",
    "id": "123"
  }

7. {
    "action": "message",
    "id": "123",
    "msg": "Hello, ws clients!"
}

8. {
    "action": "unsubscribe",
    "id": "123"
}


